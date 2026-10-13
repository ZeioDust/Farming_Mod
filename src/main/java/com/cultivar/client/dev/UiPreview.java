package com.cultivar.client.dev;

import com.cultivar.Cultivar;
import com.cultivar.block.AmendmentHopperBlock;
import com.cultivar.block.BurnPanBlock;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.Facing;
import com.cultivar.block.IrrigationLineBlock;
import com.cultivar.block.PollinationUnitBlock;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.blockentity.GenomeLabBlockEntity;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.client.guide.GuideClient;
import com.cultivar.client.guide.ManualScreen;
import com.cultivar.client.screen.ClimateControllerScreen;
import com.cultivar.client.screen.CompostBinScreen;
import com.cultivar.client.screen.ContractBoardScreen;
import com.cultivar.client.screen.GeneEditorScreen;
import com.cultivar.client.screen.GenomeCardScreen;
import com.cultivar.client.screen.GenomeLabScreen;
import com.cultivar.client.screen.MutagenChamberScreen;
import com.cultivar.client.screen.ProcessingStationScreen;
import com.cultivar.client.screen.ProduceExchangeScreen;
import com.cultivar.client.screen.SeedBankScreen;
import com.cultivar.client.screen.ShowBenchScreen;
import com.cultivar.component.CardData;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.guide.Milestones;
import com.cultivar.market.Contract;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.menu.ClimateControllerMenu;
import com.cultivar.menu.CompostBinMenu;
import com.cultivar.menu.ContractBoardMenu;
import com.cultivar.menu.GeneEditorMenu;
import com.cultivar.menu.GenomeCardMenu;
import com.cultivar.menu.GenomeLabMenu;
import com.cultivar.menu.MutagenChamberMenu;
import com.cultivar.menu.ProcessingStationMenu;
import com.cultivar.menu.ProduceExchangeMenu;
import com.cultivar.menu.SeedBankLine;
import com.cultivar.menu.SeedBankMenu;
import com.cultivar.menu.ShowBenchMenu;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FileUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class UiPreview {
    private static final boolean ENABLED = Boolean.getBoolean("cultivar.uiPreview");
    private static final int CONTAINER_ID = 120;

    private enum State { START, LOADING, RUNNING, DONE }

    private record Step(int waitTicks, Runnable action) {
    }

    private static State state = State.START;
    private static int wait;
    private static int index;
    private static final List<Step> STEPS = new ArrayList<>();

    private UiPreview() {
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        switch (state) {
            case START -> {
                if (mc.screen instanceof TitleScreen) {
                    createWorld(mc);
                    state = State.LOADING;
                }
            }
            case LOADING -> {
                if (mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
                    if (++wait >= 100) {
                        wait = 0;
                        buildSteps(mc);
                        state = State.RUNNING;
                    }
                }
            }
            case RUNNING -> {
                if (pendingShot != null) {
                    if (--pendingDelay <= 0) {
                        grab(mc, pendingShot);
                        pendingShot = null;
                    }
                    return;
                }
                if (wait > 0) {
                    wait--;
                    return;
                }
                if (index >= STEPS.size()) {
                    state = State.DONE;
                    Cultivar.LOGGER.info("Cultivar UI preview finished: {} steps", STEPS.size());
                    mc.stop();
                    return;
                }
                Step step = STEPS.get(index++);
                try {
                    step.action().run();
                } catch (RuntimeException e) {
                    Cultivar.LOGGER.error("Cultivar UI preview step {} failed", index, e);
                }
                wait = step.waitTicks();
            }
            case DONE -> {
            }
        }
    }

    private static void createWorld(Minecraft mc) {
        try {
            String folder = FileUtil.findAvailableName(mc.getLevelSource().getBaseDir(), "cultivar_ui_preview", "");
            LevelSettings settings = new LevelSettings(folder, GameType.CREATIVE, LevelSettings.DifficultySettings.DEFAULT, true,
                    WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel(folder, settings, new WorldOptions(20261013L, false, false),
                    WorldPresets::createFlatWorldDimensions, new TitleScreen());
        } catch (java.io.IOException e) {
            Cultivar.LOGGER.error("Cultivar UI preview could not create its world", e);
            state = State.DONE;
        }
    }

    private static Genome genome(int... pairs) {
        byte[] bytes = new byte[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            bytes[i] = (byte) pairs[i];
        }
        return Genome.of(bytes);
    }

    private static final Genome MOTHER = genome(1, 3, 1, 3, 1, 2, 2, 3, 2, 2, 0, 3, 2, 3, 1, 5);
    private static final Genome FATHER = genome(3, 3, 1, 3, 2, 2, 1, 3, 2, 3, 3, 3, 2, 2, 1, 1);

    private static ItemStack packet(Genome genome, long line) {
        ItemStack stack = new ItemStack(ModItems.seedPacket(CropSpecies.MAIZE));
        stack.set(ModDataComponents.GENOME.get(), genome);
        stack.set(ModDataComponents.LINEAGE.get(), line);
        return stack;
    }

    private static ItemStack card(Genome genome, long line) {
        ItemStack stack = new ItemStack(ModItems.GENOME_CARD.get());
        stack.set(ModDataComponents.CARD.get(), CardData.ofGenome(CropSpecies.MAIZE.key(), genome, line));
        return stack;
    }

    private static void buildSteps(Minecraft mc) {
        STEPS.add(new Step(40, () -> onServer(mc, player -> {
            ServerLevel level = player.level();
            BlockPos feet = player.blockPosition();
            BlockPos soil = feet.offset(2, -1, 0);
            level.setBlockAndUpdate(soil, ModBlocks.MANAGED_SOIL.get().defaultBlockState());
            level.setBlockAndUpdate(soil.above(), ModBlocks.crop(CropSpecies.MAIZE).defaultBlockState().setValue(CultivarCropBlock.STAGE,
                    CultivarCropBlock.FLOWERING));
            if (level.getBlockEntity(soil.above()) instanceof CropBlockEntity crop) {
                crop.setSpecies(CropSpecies.MAIZE);
                crop.plant(MOTHER, 1042L, false);
            }
            player.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HAND_LENS.get()));
        })));
        STEPS.add(new Step(30, () -> {
            if (mc.player != null) {
                mc.player.setYRot(-90f);
                mc.player.setXRot(28f);
            }
        }));
        STEPS.add(new Step(20, () -> shot(mc, "cultivar_hud_lens")));
        STEPS.add(new Step(30, () -> onServer(mc, player -> player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.SOIL_PROBE.get())))));
        STEPS.add(new Step(10, () -> shot(mc, "cultivar_hud_probe")));
        STEPS.add(new Step(40, () -> onServer(mc, player -> buildShowcase(player))));
        STEPS.add(new Step(40, () -> {
            if (mc.player != null) {
                mc.player.setYRot(0f);
                mc.player.setXRot(18f);
            }
        }));
        STEPS.add(new Step(10, () -> shot(mc, "cultivar_blocks_machines")));
        STEPS.add(new Step(30, () -> onServer(mc, player -> player.teleportTo(player.getX() + 40, player.getY(), player.getZ()))));
        STEPS.add(new Step(40, () -> {
            if (mc.player != null) {
                mc.player.setYRot(0f);
                mc.player.setXRot(22f);
            }
        }));
        STEPS.add(new Step(10, () -> shot(mc, "cultivar_blocks_world")));
        marketSteps(mc);
        STEPS.add(new Step(20, () -> onServer(mc, player -> player.teleportTo(player.getX() - 40, player.getY(), player.getZ()))));
        STEPS.add(new Step(20, () -> {
            if (mc.player != null) {
                mc.player.setYRot(-90f);
                mc.player.setXRot(28f);
            }
        }));

        STEPS.add(new Step(10, () -> onServer(mc, player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, card(MOTHER, 1042L));
            player.setItemInHand(InteractionHand.OFF_HAND, card(FATHER, 977L));
        })));

        open(mc, "cultivar_genome_lab", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(GenomeLabBlockEntity.SLOTS);
            container.setItem(GenomeLabBlockEntity.SLOT_SAMPLE, packet(MOTHER, 1042L));
            container.setItem(GenomeLabBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 23));
            SimpleContainerData data = new SimpleContainerData(GenomeLabBlockEntity.DATA);
            data.set(0, 560);
            return new GenomeLabScreen(new GenomeLabMenu(CONTAINER_ID, inv, container, data), inv, Component.translatable("block.cultivar.genome_lab"));
        });
        open(mc, "cultivar_mutagen_chamber", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(MutagenChamberBlockEntity.SLOTS);
            container.setItem(MutagenChamberBlockEntity.FIRST_INPUT, packet(MOTHER, 1042L).copyWithCount(6));
            container.setItem(MutagenChamberBlockEntity.FIRST_INPUT + 1, packet(FATHER, 977L).copyWithCount(3));
            container.setItem(MutagenChamberBlockEntity.SLOT_VIAL, new ItemStack(ModItems.MUTAGEN_VIAL.get(), 2));
            container.setItem(MutagenChamberBlockEntity.FIRST_OUTPUT, packet(MOTHER, 1043L).copyWithCount(2));
            SimpleContainerData data = new SimpleContainerData(MutagenChamberBlockEntity.DATA);
            data.set(0, 430);
            data.set(1, 5);
            return new MutagenChamberScreen(new MutagenChamberMenu(CONTAINER_ID, inv, container, data), inv,
                    Component.translatable("block.cultivar.mutagen_chamber"));
        });
        open(mc, "cultivar_compost_bin", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(CompostBinBlockEntity.SLOTS);
            container.setItem(0, new ItemStack(ModItems.PRODUCE.get(CropSpecies.MAIZE).get(), 12));
            container.setItem(1, new ItemStack(Items.WHEAT, 30));
            container.setItem(CompostBinBlockEntity.SLOT_OUTPUT, new ItemStack(ModItems.COMPOST.get(), 4));
            SimpleContainerData data = new SimpleContainerData(CompostBinBlockEntity.DATA);
            data.set(0, 100);
            data.set(1, 380);
            return new CompostBinScreen(new CompostBinMenu(CONTAINER_ID, inv, container, data), inv, Component.translatable("block.cultivar.compost_bin"));
        });
        open(mc, "cultivar_climate_controller", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainerData data = new SimpleContainerData(ClimateControllerBlockEntity.DATA_COUNT);
            data.set(ClimateControllerBlockEntity.D_SEALED, 1);
            data.set(ClimateControllerBlockEntity.D_VOLUME_LOW, 312);
            data.set(ClimateControllerBlockEntity.D_TEMP, 82);
            data.set(ClimateControllerBlockEntity.D_SETPOINT, 120);
            data.set(ClimateControllerBlockEntity.D_HUMIDITY, 64);
            data.set(ClimateControllerBlockEntity.D_LIGHT, 14);
            data.set(ClimateControllerBlockEntity.D_INSULATION, 87);
            data.set(ClimateControllerBlockEntity.D_GROW_LIGHTS, 3);
            data.set(ClimateControllerBlockEntity.D_HUMIDIFIERS, 2);
            return new ClimateControllerScreen(new ClimateControllerMenu(CONTAINER_ID, inv, data, null, BlockPos.ZERO), inv,
                    Component.translatable("block.cultivar.climate_controller"));
        });
        open(mc, "cultivar_climate_breach", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainerData data = new SimpleContainerData(ClimateControllerBlockEntity.DATA_COUNT);
            data.set(ClimateControllerBlockEntity.D_TEMP, -12);
            data.set(ClimateControllerBlockEntity.D_SETPOINT, 90);
            data.set(ClimateControllerBlockEntity.D_HUMIDITY, 30);
            data.set(ClimateControllerBlockEntity.D_LIGHT, 6);
            data.set(ClimateControllerBlockEntity.D_INSULATION, 20);
            return new ClimateControllerScreen(new ClimateControllerMenu(CONTAINER_ID, inv, data, null, new BlockPos(1, 2, 3)), inv,
                    Component.translatable("block.cultivar.climate_controller"));
        });
        open(mc, "cultivar_seed_bank", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(SeedBankBlockEntity.SLOTS);
            container.setItem(SeedBankBlockEntity.SLOT_DEPOSIT, packet(MOTHER, 1042L));
            container.setItem(SeedBankBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 9));
            SimpleContainerData data = new SimpleContainerData(SeedBankBlockEntity.DATA);
            SeedBankMenu menu = new SeedBankMenu(CONTAINER_ID, inv, container, data, null);
            List<SeedBankLine> lines = new ArrayList<>();
            Identifier maize = CropSpecies.MAIZE.key();
            lines.add(new SeedBankLine("Golden Heavy F3", maize, MOTHER, 1042L, 24000L * 12, false));
            lines.add(new SeedBankLine("Sentinel Resist", CropSpecies.WHEATLINE.key(), FATHER, 977L, 24000L * 15, false));
            lines.add(new SeedBankLine("Canopy Clover", CropSpecies.CLOVER.key(), FATHER, 31L, 24000L * 19, true));
            lines.add(new SeedBankLine("Soy Rotation A", CropSpecies.SOYBEAN.key(), MOTHER, 1201L, 24000L * 21, false));
            lines.add(new SeedBankLine("Frostproof Brassica", CropSpecies.BRASSICA.key(), MOTHER, 1388L, 24000L * 26, false));
            lines.add(new SeedBankLine("Gourd Giant", CropSpecies.GOURD.key(), FATHER, 1402L, 24000L * 30, false));
            lines.add(new SeedBankLine("Flax Fine Fibre", CropSpecies.FIBER_FLAX.key(), MOTHER, 1500L, 24000L * 33, false));
            menu.setClientLines(lines, 24);
            return new SeedBankScreen(menu, inv, Component.translatable("block.cultivar.seed_bank"));
        });
        STEPS.add(new Step(50, () -> mc.setScreen(cardScreen(mc, CardData.ofGenome(CropSpecies.MAIZE.key(), MOTHER, 1042L)))));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_card_genotype")));
        STEPS.add(new Step(40, () -> clickTab(mc, 1)));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_card_profile")));
        STEPS.add(new Step(40, () -> clickTab(mc, 2)));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_card_cross")));
        STEPS.add(new Step(50, () -> mc.setScreen(cardScreen(mc, CardData.ofStrain(Identifier.fromNamespaceAndPath(Cultivar.MODID, "leaf_spot"))))));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_card_strain")));
        STEPS.add(new Step(5, () -> mc.setScreen(null)));

        STEPS.add(new Step(30, () -> onServer(mc, player -> {
            Guide.milestone(player, Milestones.PLANTED);
            Guide.milestone(player, Milestones.HARVESTED);
            Guide.milestone(player, Milestones.READ_THE_CODE);
            Guide.milestone(player, Milestones.FLOWERING);
            Guide.hint(player, Hint.FLOWERING);
        })));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_hint_toast")));
        Object[][] pages = {
                {Hint.Chapter.START, 0}, {Hint.Chapter.START, 1}, {Hint.Chapter.GENES, 0}, {Hint.Chapter.GENES, 1},
                {Hint.Chapter.POLLINATION, 0}, {Hint.Chapter.POLLINATION, 1}, {Hint.Chapter.POLLINATION, 2}, {Hint.Chapter.SOIL, 0},
                {Hint.Chapter.SOIL, 1}, {Hint.Chapter.BLIGHT, 0}, {Hint.Chapter.LAB, 0}, {Hint.Chapter.GREENHOUSE, 0},
                {Hint.Chapter.MUTATION, 1}, {Hint.Chapter.CANOPY, 0}, {Hint.Chapter.CANOPY, 1}, {Hint.Chapter.MARKET, 0},
                {Hint.Chapter.MARKET, 1}, {Hint.Chapter.MARKET, 2}, {Hint.Chapter.PROCESSING, 0}, {Hint.Chapter.PROCESSING, 1},
                {Hint.Chapter.PROCESSING, 2}, {Hint.Chapter.MASTERY, 0}, {Hint.Chapter.MASTERY, 1}, {Hint.Chapter.MASTERY, 2}};
        for (Object[] entry : pages) {
            Hint.Chapter chapter = (Hint.Chapter) entry[0];
            int page = (Integer) entry[1];
            STEPS.add(new Step(45, () -> {
                if (!(mc.screen instanceof ManualScreen)) {
                    GuideClient.open(chapter);
                }
                if (mc.screen instanceof ManualScreen manual) {
                    manual.show(chapter, page);
                }
            }));
            STEPS.add(new Step(5, () -> shot(mc, "cultivar_manual_" + chapter.name().toLowerCase(java.util.Locale.ROOT) + "_" + (page + 1))));
        }
        STEPS.add(new Step(30, () -> mc.setScreen(new ItemGalleryScreen(false))));
        STEPS.add(new Step(7, () -> shot(mc, "cultivar_items_a")));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_items_b")));
        STEPS.add(new Step(5, () -> mc.setScreen(null)));
    }

    private static BlockPos market = BlockPos.ZERO;

    private static ItemStack graded(CropSpecies species, int count, Grade grade) {
        return GradeLogic.apply(new ItemStack(ModItems.produce(species), count), grade);
    }

    private static void marketSteps(Minecraft mc) {
        STEPS.add(new Step(20, () -> onServer(mc, player -> {
            player.teleportTo(player.getX() + 40, player.getY(), player.getZ());
            ServerLevel level = player.level();
            market = player.blockPosition().offset(-2, 0, 4);
            place(level, market, List.of(facingNorth(ModBlocks.PRODUCE_EXCHANGE.get()), facingNorth(ModBlocks.CONTRACT_BOARD.get()),
                    facingNorth(ModBlocks.PROCESSING_STATION.get()), facingNorth(ModBlocks.GENE_EDITOR.get()), facingNorth(ModBlocks.SHOW_BENCH.get())));
        })));
        STEPS.add(new Step(40, () -> {
            if (mc.player != null) {
                mc.player.setYRot(0f);
                mc.player.setXRot(24f);
            }
        }));
        STEPS.add(new Step(10, () -> shot(mc, "cultivar_blocks_market")));
        STEPS.add(new Step(10, () -> onServer(mc, player -> {
            ServerLevel level = player.level();
            if (level.getBlockEntity(market) instanceof ProduceExchangeBlockEntity exchange) {
                exchange.setItem(0, graded(CropSpecies.GOURD, 24, Grade.PRIME));
                exchange.setItem(1, graded(CropSpecies.MAIZE, 40, Grade.B));
                exchange.sell(level, market);
            }
            if (level.getBlockEntity(market.east()) instanceof ContractBoardBlockEntity board) {
                board.refill(level);
                board.post(0, Contract.of(Contract.Kind.PRODUCE, CropSpecies.MAIZE.key(), 16, Grade.A, java.util.Optional.empty(), 0f,
                        java.util.Optional.empty(), false, 9, java.util.Optional.of(new net.minecraft.world.item.ItemStackTemplate(ModItems.SOY_OIL.get(), 2)))
                        .withProgress(6));
            }
            BlockPos stationPos = market.east(2);
            if (level.getBlockEntity(stationPos) instanceof ProcessingStationBlockEntity station) {
                station.setItem(ProcessingStationBlockEntity.SLOT_INPUT, graded(CropSpecies.SOYBEAN, 9, Grade.PRIME));
                for (int i = 0; i < 90; i++) {
                    station.serverTick(level, stationPos, level.getBlockState(stationPos));
                }
            }
            if (level.getBlockEntity(market.east(4)) instanceof ShowBenchBlockEntity bench) {
                bench.setItem(ShowBenchBlockEntity.SLOT_PACKET, packet(MOTHER, 1042L));
                bench.setItem(ShowBenchBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 8));
                bench.judge(player);
            }
            AlleleAtlas.record(player, MOTHER, Catalog.get(level).table());
            AlleleAtlas.record(player, FATHER, Catalog.get(level).table());
        })));
        STEPS.add(new Step(10, () -> {
            ProduceExchangeScreen.DEMAND.put(BuiltInRegistries.ITEM.getKey(ModItems.produce(CropSpecies.GOURD)), 0.8f);
            ProduceExchangeScreen.DEMAND.put(BuiltInRegistries.ITEM.getKey(ModItems.produce(CropSpecies.MAIZE)), 0.71f);
            ProduceExchangeScreen.DEMAND.put(BuiltInRegistries.ITEM.getKey(ModItems.produce(CropSpecies.WHEATLINE)), 0.46f);
        }));
        open(mc, "cultivar_produce_exchange", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(ProduceExchangeBlockEntity.SLOTS);
            container.setItem(0, graded(CropSpecies.GOURD, 32, Grade.PRIME));
            container.setItem(1, graded(CropSpecies.BRASSICA, 18, Grade.A));
            container.setItem(3, graded(CropSpecies.WHEATLINE, 64, Grade.C));
            container.setItem(4, new ItemStack(ModItems.SOY_OIL.get(), 6));
            container.setItem(ProduceExchangeBlockEntity.SLOT_OUTPUT, new ItemStack(Items.EMERALD, 14));
            return new ProduceExchangeScreen(new ProduceExchangeMenu(CONTAINER_ID, inv, container, market), inv,
                    Component.translatable("block.cultivar.produce_exchange"));
        });
        open(mc, "cultivar_contract_board", () -> {
            Inventory inv = mc.player.getInventory();
            ContractBoardBlockEntity board = (ContractBoardBlockEntity) mc.level.getBlockEntity(market.east());
            board.setItem(ContractBoardBlockEntity.SLOT_DELIVERY, graded(CropSpecies.MAIZE, 10, Grade.A));
            board.setItem(ContractBoardBlockEntity.SLOT_PAYMENT, new ItemStack(Items.EMERALD, 7));
            return new ContractBoardScreen(new ContractBoardMenu(CONTAINER_ID, inv, board, market.east()), inv,
                    Component.translatable("block.cultivar.contract_board"));
        });
        STEPS.add(new Step(5, () -> {
            if (mc.screen != null) {
                mc.screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(
                        ((mc.screen.width - ContractBoardMenu.WIDTH) / 2.0) + 40 + 64 + 30, ((mc.screen.height - ContractBoardMenu.HEIGHT) / 2.0) + 25,
                        new net.minecraft.client.input.MouseButtonInfo(0, 0)), false);
            }
        }));
        STEPS.add(new Step(5, () -> shot(mc, "cultivar_contract_supplies")));
        open(mc, "cultivar_processing_station", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(ProcessingStationBlockEntity.SLOTS);
            container.setItem(ProcessingStationBlockEntity.SLOT_INPUT, graded(CropSpecies.SOYBEAN, 9, Grade.PRIME));
            container.setItem(ProcessingStationBlockEntity.SLOT_OUTPUT, GradeLogic.apply(new ItemStack(ModItems.SOY_OIL.get(), 2), Grade.PRIME));
            SimpleContainerData data = new SimpleContainerData(ProcessingStationBlockEntity.DATA);
            data.set(0, 470);
            data.set(1, 84);
            return new ProcessingStationScreen(new ProcessingStationMenu(CONTAINER_ID, inv, container, data, market.east(2)), inv,
                    Component.translatable("block.cultivar.processing_station"));
        });
        open(mc, "cultivar_gene_editor", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(GeneEditorBlockEntity.SLOTS);
            container.setItem(GeneEditorBlockEntity.SLOT_PACKET, packet(MOTHER, 1042L));
            container.setItem(GeneEditorBlockEntity.SLOT_REAGENT, new ItemStack(ModItems.EDIT_REAGENT.get(), 3));
            SimpleContainerData data = new SimpleContainerData(GeneEditorBlockEntity.DATA);
            data.set(0, 0);
            data.set(1, 1);
            data.set(2, FATHER.allele(com.cultivar.genetics.Gene.YIELD, 0));
            data.set(3, 380);
            data.set(4, 1);
            return new GeneEditorScreen(new GeneEditorMenu(CONTAINER_ID, inv, container, data), inv, Component.translatable("block.cultivar.gene_editor"));
        });
        open(mc, "cultivar_show_bench", () -> {
            Inventory inv = mc.player.getInventory();
            SimpleContainer container = new SimpleContainer(ShowBenchBlockEntity.SLOTS);
            container.setItem(ShowBenchBlockEntity.SLOT_PACKET, packet(FATHER, 977L));
            container.setItem(ShowBenchBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 12));
            return new ShowBenchScreen(new ShowBenchMenu(CONTAINER_ID, inv, container, market.east(4)), inv, Component.translatable("block.cultivar.show_bench"));
        });
        STEPS.add(new Step(5, () -> mc.setScreen(null)));
        STEPS.add(new Step(10, () -> onServer(mc, player -> player.teleportTo(player.getX() - 40, player.getY(), player.getZ()))));
    }

    private static void buildShowcase(ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos feet = player.blockPosition();
        List<BlockState> machines = List.of(
                facingNorth(ModBlocks.GENOME_LAB.get()), facingNorth(ModBlocks.SEED_BANK.get()), facingNorth(ModBlocks.SEED_VAULT_DRAWER.get()),
                facingNorth(ModBlocks.MUTAGEN_CHAMBER.get()), facingNorth(ModBlocks.THERMAL_SHOCK_UNIT.get()),
                facingNorth(ModBlocks.CLIMATE_CONTROLLER.get()), facingNorth(ModBlocks.COMPOST_BIN.get()),
                facingNorth(ModBlocks.HARVEST_COLLECTOR.get()), facingNorth(ModBlocks.NUTRIENT_SPREADER.get()),
                facingNorth(ModBlocks.POLLINATION_UNIT.get()).setValue(PollinationUnitBlock.ACTIVE, true), facingNorth(ModBlocks.HUMIDIFIER.get()),
                facingNorth(ModBlocks.DEHUMIDIFIER.get()), facingNorth(ModBlocks.LIME_HOPPER.get()).setValue(AmendmentHopperBlock.LEVEL, 5),
                facingNorth(ModBlocks.SULFUR_HOPPER.get()).setValue(AmendmentHopperBlock.LEVEL, 8));
        place(level, feet.offset(-machines.size() / 2, 0, 6), machines);
        level.setBlock(feet.offset(0, 3, 5), ModBlocks.GROW_LIGHT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(feet.offset(1, 3, 5), ModBlocks.GROW_LIGHT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        List<BlockState> world = List.of(
                ModBlocks.MANAGED_SOIL.get().defaultBlockState(),
                ModBlocks.MANAGED_SOIL.get().defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7),
                ModBlocks.BREEDING_PLOT.get().defaultBlockState(), ModBlocks.DEPLETED_SOIL.get().defaultBlockState(),
                ModBlocks.GREENHOUSE_PANE.get().defaultBlockState(), ModBlocks.ISOLATION_SCREEN.get().defaultBlockState(),
                ModBlocks.BURN_PAN.get().defaultBlockState().setValue(BurnPanBlock.LIT, true),
                ModBlocks.IRRIGATION_LINE.get().defaultBlockState().setValue(IrrigationLineBlock.FED, true),
                ModBlocks.SPRINKLER.get().defaultBlockState(), ModBlocks.TRIAL_MARKER.get().defaultBlockState(),
                ModBlocks.CANOPY_BARK.get().defaultBlockState(), ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState(),
                ModBlocks.CANOPY_PORTAL_FRAME.get().defaultBlockState(), ModBlocks.CANOPY_PORTAL.get().defaultBlockState());
        BlockPos worldRow = feet.offset(40 - world.size() / 2, 0, 6);
        place(level, worldRow, world);
        level.setBlock(worldRow.offset(11, 1, 0), ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(worldRow.offset(11, 0, -1), ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static BlockState facingNorth(Block block) {
        return block.defaultBlockState().setValue(Facing.FACING, Direction.NORTH);
    }

    private static void place(ServerLevel level, BlockPos start, List<BlockState> states) {
        for (int i = 0; i < states.size(); i++) {
            level.setBlock(start.offset(i, 0, 0), states.get(i), Block.UPDATE_CLIENTS);
        }
    }

    private static Screen cardScreen(Minecraft mc, CardData card) {
        Inventory inv = mc.player.getInventory();
        return new GenomeCardScreen(new GenomeCardMenu(CONTAINER_ID, inv, card), inv, Component.translatable("item.cultivar.genome_card"));
    }

    private static void clickTab(Minecraft mc, int tab) {
        if (mc.screen instanceof GenomeCardScreen screen) {
            double x = screen.getLeftPos() + 344 - 8 - 52 * 3 + tab * 52 + 20;
            double y = screen.getTopPos() + 27;
            screen.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)), false);
        }
    }

    private static void open(Minecraft mc, String name, Supplier<Screen> screen) {
        STEPS.add(new Step(45, () -> mc.setScreen(screen.get())));
        STEPS.add(new Step(5, () -> shot(mc, name)));
    }

    private static @org.jspecify.annotations.Nullable String pendingShot;
    private static int pendingDelay;

    private static void shot(Minecraft mc, String name) {
        if (!name.equals("cultivar_hint_toast")) {
            mc.getToastManager().clear();
            mc.gui.getChat().clearMessages(false);
            pendingShot = name;
            pendingDelay = 2;
            return;
        }
        grab(mc, name);
    }

    private static void grab(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), 1,
                message -> Cultivar.LOGGER.info("Cultivar UI preview: {}", message.getString()));
    }

    private static void onServer(Minecraft mc, java.util.function.Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        var id = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                action.accept(player);
            }
        });
    }
}
