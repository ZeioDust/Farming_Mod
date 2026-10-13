package com.cultivar.client.dev;

import com.cultivar.Cultivar;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.client.ClientSession;
import com.cultivar.client.guide.GuideClient;
import com.cultivar.client.screen.ContractBoardScreen;
import com.cultivar.client.screen.ProcessingStationScreen;
import com.cultivar.client.screen.ProduceExchangeScreen;
import com.cultivar.compat.jei.CultivarJeiPlugin;
import com.cultivar.data.CropSpecies;
import com.cultivar.guide.Hint;
import com.cultivar.market.MarketValues;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModRecipes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class MultiplayerCheck {
    private static final boolean ENABLED = Boolean.getBoolean("cultivar.mpCheck");
    private static final boolean PERF = Boolean.getBoolean("cultivar.mpPerf");
    private static final String SERVER = System.getProperty("cultivar.mpServer", "127.0.0.1:25565");

    private record Step(int waitTicks, Runnable action) {
    }

    private static final List<Step> STEPS = new ArrayList<>();
    private static boolean started;
    private static int joinedTicks;
    private static int connectDelay;
    private static int wait;
    private static int index;
    private static int passed;
    private static int failed;
    private static BlockPos base = BlockPos.ZERO;

    private MultiplayerCheck() {
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!started) {
            mc.options.pauseOnLostFocus = false;
            if (mc.player == null && (mc.screen instanceof TitleScreen || mc.screen instanceof DisconnectedScreen) && ++connectDelay >= 40) {
                connectDelay = 0;
                if (mc.screen instanceof DisconnectedScreen) {
                    Cultivar.LOGGER.warn("MP CHECK: connection attempt failed, retrying");
                }
                ConnectScreen.startConnecting(new TitleScreen(), mc, ServerAddress.parseString(SERVER), new ServerData("Cultivar check", SERVER,
                        ServerData.Type.OTHER), false, null);
            }
            if (mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
                Cultivar.LOGGER.error("MP CHECK RESULT: aborted, a singleplayer world was opened instead of the server");
                started = true;
                mc.stop();
                return;
            }
            if (mc.player != null && mc.level != null && mc.getSingleplayerServer() == null && ++joinedTicks >= 100) {
                started = true;
                if (PERF) {
                    buildPerf(mc);
                } else {
                    build(mc);
                }
            }
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        if (index >= STEPS.size()) {
            return;
        }
        Step step = STEPS.get(index++);
        try {
            step.action().run();
        } catch (RuntimeException e) {
            failed++;
            Cultivar.LOGGER.error("MP CHECK step {} threw", index, e);
        }
        wait = step.waitTicks();
    }

    private static void check(String name, BooleanSupplier condition) {
        boolean ok;
        try {
            ok = condition.getAsBoolean();
        } catch (RuntimeException e) {
            Cultivar.LOGGER.error("MP CHECK {} threw", name, e);
            ok = false;
        }
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        Cultivar.LOGGER.info("MP CHECK {}: {}", ok ? "PASS" : "FAIL", name);
    }

    private static void command(Minecraft mc, String command) {
        if (mc.player != null) {
            mc.player.connection.sendCommand(command);
        }
    }

    private static String at(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static void use(Minecraft mc, BlockPos pos) {
        if (mc.player != null && mc.gameMode != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        }
    }

    private static void lookAt(Minecraft mc, BlockPos pos) {
        if (mc.player == null) {
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        Vec3 d = Vec3.atCenterOf(pos).subtract(eye);
        double flat = Math.sqrt(d.x * d.x + d.z * d.z);
        mc.player.setYRot((float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(d.y, flat)));
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), 1,
                message -> Cultivar.LOGGER.info("MP CHECK screenshot: {}", message.getString()));
    }

    private static <T> T be(Minecraft mc, BlockPos pos, Class<T> type) {
        var entity = mc.level != null ? mc.level.getBlockEntity(pos) : null;
        return type.isInstance(entity) ? type.cast(entity) : null;
    }

    private static void build(Minecraft mc) {
        base = mc.player.blockPosition();
        BlockPos exchange = base.offset(-2, 0, 2);
        BlockPos station = base.offset(0, 0, 2);
        BlockPos board = base.offset(2, 0, 2);
        STEPS.add(new Step(20, () -> {
            command(mc, "gamemode creative @s");
            command(mc, "clear @s");
            command(mc, "time set 1000");
            command(mc, "setblock " + at(exchange) + " cultivar:produce_exchange[facing=north]");
            command(mc, "setblock " + at(station) + " cultivar:processing_station[facing=north]");
            command(mc, "setblock " + at(board) + " cultivar:contract_board[facing=north]");
            mc.player.setYRot(0f);
            mc.player.setXRot(30f);
        }));
        STEPS.add(new Step(40, () -> {
            command(mc, "item replace block " + at(exchange) + " container.0 with cultivar:gourd 32");
            command(mc, "item replace block " + at(station) + " container.0 with cultivar:soybean_pod 9");
        }));
        STEPS.add(new Step(5, () -> {
            check("processing recipes synced to the client (9)", () -> ClientSession.recipes().byType(ModRecipes.PROCESSING.get()).size() == 9);
            check("composting recipes synced to the client (5)", () -> ClientSession.recipes().byType(ModRecipes.COMPOSTING.get()).size() == 5);
            check("market value data map synced", () -> MarketValues.base(new ItemStack(ModItems.produce(CropSpecies.GOURD))) > 0);
            check("contract board posted 3 contracts, visible on the client", () -> {
                ContractBoardBlockEntity b = be(mc, board, ContractBoardBlockEntity.class);
                return b != null && b.contracts().stream().filter(java.util.Optional::isPresent).count() == 3;
            });
            check("processing station preview synced", () -> {
                ProcessingStationBlockEntity s = be(mc, station, ProcessingStationBlockEntity.class);
                return s != null && s.preview().is(ModItems.SOY_OIL.get()) && s.batch() == 3;
            });
        }));
        STEPS.add(new Step(40, () -> shot(mc, "mp_machines")));
        BlockPos crop = base.offset(0, 0, -2);
        STEPS.add(new Step(10, () -> {
            command(mc, "setblock " + at(crop.below().east()) + " minecraft:water");
            command(mc, "setblock " + at(crop.below()) + " cultivar:managed_soil[moisture=7]");
            command(mc, "setblock " + at(crop) + " cultivar:maize_crop[stage=4]");
        }));
        STEPS.add(new Step(40, () -> use(mc, station)));
        STEPS.add(new Step(5, () -> {
            check("processing menu opened from the server", () -> mc.screen instanceof ProcessingStationScreen);
            check("menu slots synced", () -> mc.screen instanceof ProcessingStationScreen screen
                    && screen.getMenu().getSlot(ProcessingStationBlockEntity.SLOT_INPUT).getItem().is(ModItems.produce(CropSpecies.SOYBEAN)));
            shot(mc, "mp_processing");
        }));
        STEPS.add(new Step(220, () -> {
        }));
        STEPS.add(new Step(5, () -> {
            check("server processed a batch; output slot synced", () -> mc.screen instanceof ProcessingStationScreen screen
                    && screen.getMenu().getSlot(ProcessingStationBlockEntity.SLOT_OUTPUT).getItem().is(ModItems.SOY_OIL.get()));
            shot(mc, "mp_processing_done");
            mc.setScreen(null);
        }));
        STEPS.add(new Step(60, () -> command(mc, "time add 24000")));
        STEPS.add(new Step(5, () -> check("exchange sold the crate at dawn; sale synced", () -> {
            ProduceExchangeBlockEntity e = be(mc, exchange, ProduceExchangeBlockEntity.class);
            return e != null && e.lastSaleItems() == 32;
        })));
        STEPS.add(new Step(40, () -> use(mc, exchange)));
        STEPS.add(new Step(5, () -> {
            check("exchange menu opened", () -> mc.screen instanceof ProduceExchangeScreen);
            check("market quotes payload received, gourd demand lowered", () -> {
                Float d = ProduceExchangeScreen.DEMAND.get(BuiltInRegistries.ITEM.getKey(ModItems.produce(CropSpecies.GOURD)));
                return d != null && d < 1f;
            });
            check("payout emeralds synced to the output slot", () -> mc.screen instanceof ProduceExchangeScreen screen
                    && screen.getMenu().getSlot(ProduceExchangeBlockEntity.SLOT_OUTPUT).getItem().is(net.minecraft.world.item.Items.EMERALD));
            shot(mc, "mp_exchange");
            mc.setScreen(null);
        }));
        STEPS.add(new Step(40, () -> use(mc, board)));
        STEPS.add(new Step(5, () -> {
            check("contract board menu opened", () -> mc.screen instanceof ContractBoardScreen);
            shot(mc, "mp_contracts");
            mc.setScreen(null);
        }));
        if (ModList.get().isLoaded("jade")) {
            for (var target : List.of(java.util.Map.entry("station", station), java.util.Map.entry("exchange", exchange),
                    java.util.Map.entry("board", board), java.util.Map.entry("crop", crop))) {
                STEPS.add(new Step(30, () -> lookAt(mc, target.getValue())));
                STEPS.add(new Step(5, () -> shot(mc, "mp_jade_" + target.getKey())));
            }
        }
        STEPS.add(new Step(10, () -> check("guide state synced from the server", () -> GuideClient.done(Hint.PROCESSED.id()))));
        STEPS.add(new Step(30, () -> GuideClient.open(Hint.Chapter.MARKET)));
        STEPS.add(new Step(5, () -> {
            shot(mc, "mp_manual");
            mc.setScreen(null);
        }));
        if (ModList.get().isLoaded("jei")) {
            STEPS.add(new Step(20, () -> {
                check("JEI holds 9 processing recipes", () -> CultivarJeiPlugin.count("processing") == 9);
                check("JEI holds 5 composting recipes", () -> CultivarJeiPlugin.count("composting") == 5);
                check("JEI market page per sellable item", () -> CultivarJeiPlugin.count("produce_exchange") >= 20);
                check("JEI ration examples", () -> CultivarJeiPlugin.count("ration") == 3);
            }));
            for (String page : List.of("processing", "composting", "ration", "genome_lab", "mutagen_chamber", "show_bench", "produce_exchange",
                    "contract_supplies")) {
                STEPS.add(new Step(30, () -> check("JEI opens the " + page + " page", () -> CultivarJeiPlugin.show(page))));
                STEPS.add(new Step(5, () -> shot(mc, "mp_jei_" + page)));
            }
            STEPS.add(new Step(10, () -> mc.setScreen(null)));
        }
        STEPS.add(new Step(20, () -> {
            Cultivar.LOGGER.info("MP CHECK RESULT: {} passed, {} failed", passed, failed);
            command(mc, "stop");
        }));
        STEPS.add(new Step(60, mc::stop));
    }

    private static final String[] SPECIES = {"wheatline", "maize", "soybean", "clover", "tuber", "brassica", "gourd", "fiber_flax"};
    private static final int FARM = 64;
    private static final List<Integer> FPS = new ArrayList<>();

    private static void buildPerf(Minecraft mc) {
        base = mc.player.blockPosition();
        BlockPos origin = base.offset(4, 0, 4);
        STEPS.add(new Step(20, () -> {
            command(mc, "gamemode creative @s");
            command(mc, "time set 1000");
            command(mc, "gamerule advance_time false");
            command(mc, "tp @s " + (origin.getX() - 8) + " " + (origin.getY() + 12) + " " + (origin.getZ() + FARM / 2) + " -90 35");
            mc.options.inactivityFpsLimit().set(net.minecraft.client.InactivityFpsLimit.MINIMIZED);
        }));
        STEPS.add(new Step(200, () -> command(mc, "tick query")));
        STEPS.add(new Step(5, () -> Cultivar.LOGGER.info("MP PERF baseline (empty world) recorded above; frame limit {}, vsync {}",
                mc.options.framerateLimit().get(), mc.options.enableVsync().get())));
        List<Integer> emptyFps = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            STEPS.add(new Step(20, () -> emptyFps.add(mc.getFps())));
        }
        STEPS.add(new Step(5, () -> Cultivar.LOGGER.info("MP PERF client FPS over empty land: average {}, minimum {}",
                Math.round(emptyFps.stream().mapToInt(Integer::intValue).average().orElse(0)),
                emptyFps.stream().mapToInt(Integer::intValue).min().orElse(0))));
        int crops = 0;
        for (int row = 0; row < FARM; row++) {
            int z = origin.getZ() + row;
            BlockPos a = new BlockPos(origin.getX(), origin.getY() - 1, z);
            BlockPos b = new BlockPos(origin.getX() + FARM - 1, origin.getY() - 1, z);
            if (row % 9 == 4) {
                STEPS.add(new Step(1, () -> command(mc, "fill " + at(a) + " " + at(b) + " minecraft:water")));
                continue;
            }
            String species = SPECIES[row % SPECIES.length];
            int stage = row % 3 == 0 ? 6 : row % 3 == 1 ? 4 : 2;
            STEPS.add(new Step(1, () -> {
                command(mc, "fill " + at(a) + " " + at(b) + " cultivar:managed_soil[moisture=7]");
                command(mc, "fill " + at(a.above()) + " " + at(b.above()) + " cultivar:" + species + "_crop[stage=" + stage + "]");
            }));
            crops += FARM;
        }
        int cropCount = crops;
        STEPS.add(new Step(20, () -> Cultivar.LOGGER.info("MP PERF farm placed: {} crops", cropCount)));
        STEPS.add(new Step(10, () -> {
            for (int i = 0; i < 8; i++) {
                BlockPos unit = origin.offset(8 + i * 7, 0, 31);
                command(mc, "setblock " + at(unit) + " cultivar:pollination_unit[facing=north]");
                command(mc, "setblock " + at(unit.above()) + " minecraft:redstone_block");
            }
            for (int i = 0; i < 4; i++) {
                command(mc, "setblock " + at(origin.offset(-2, 0, 8 + i * 16)) + " cultivar:harvest_collector[facing=east]");
            }
            for (int i = 0; i < 6; i++) {
                command(mc, "setblock " + at(origin.offset(5 + i * 10, 0, 13)) + " cultivar:sprinkler");
            }
            for (int i = 0; i < 8; i++) {
                BlockPos bin = origin.offset(-4, 0, i * 4);
                command(mc, "setblock " + at(bin) + " cultivar:compost_bin[facing=east]");
                command(mc, "item replace block " + at(bin) + " container.0 with minecraft:wheat 64");
                BlockPos station = origin.offset(-6, 0, i * 4);
                command(mc, "setblock " + at(station) + " cultivar:processing_station[facing=east]");
                command(mc, "item replace block " + at(station) + " container.0 with cultivar:soybean_pod 64");
            }
        }));
        STEPS.add(new Step(40, () -> {
            command(mc, "tp @s " + (origin.getX() - 8) + " " + (origin.getY() + 12) + " " + (origin.getZ() + FARM / 2));
            mc.player.setYRot(-90f);
            mc.player.setXRot(35f);
        }));
        STEPS.add(new Step(600, () -> command(mc, "perf start")));
        for (int i = 0; i < 60; i++) {
            STEPS.add(new Step(20, () -> FPS.add(mc.getFps())));
        }
        STEPS.add(new Step(10, () -> {
            command(mc, "perf stop");
            command(mc, "tick query");
            command(mc, "cultivar stats");
            Cultivar.LOGGER.info("MP PERF entities near the farm: {}", mc.level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
                    new net.minecraft.world.phys.AABB(origin).inflate(FARM)).stream().map(e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath())
                    .collect(java.util.stream.Collectors.groupingBy(n -> n, java.util.stream.Collectors.counting())));
            int min = FPS.stream().mapToInt(Integer::intValue).min().orElse(0);
            double avg = FPS.stream().mapToInt(Integer::intValue).average().orElse(0);
            Cultivar.LOGGER.info("MP PERF client FPS over the farm: average {}, minimum {}", Math.round(avg), min);
            shot(mc, "mp_perf_farm");
        }));
        STEPS.add(new Step(40, () -> {
            Cultivar.LOGGER.info("MP CHECK RESULT: performance run finished");
            command(mc, "stop");
        }));
        STEPS.add(new Step(60, mc::stop));
    }
}
