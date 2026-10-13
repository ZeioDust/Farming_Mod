package com.cultivar.gametest;

import static com.cultivar.gametest.CultivarGameTests.HARDY;
import static com.cultivar.gametest.CultivarGameTests.check;
import static com.cultivar.gametest.CultivarGameTests.plantCrop;

import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.GenomeLabBlockEntity;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.component.CardData;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.world.HarvestLogic;
import com.cultivar.world.PollinationSystem;
import com.cultivar.world.gen.SiteKind;
import com.cultivar.world.gen.SitePiece;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;

final class MachineGameTests {
    private MachineGameTests() {
    }

    private static ItemStack packet(CropSpecies species, Genome genome) {
        return HarvestLogic.seedStack(species, genome, 0, false);
    }

    static void genomeLabPrintsCard(net.minecraft.gametest.framework.GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.GENOME_LAB.get());
        GenomeLabBlockEntity lab = helper.getBlockEntity(pos, GenomeLabBlockEntity.class);
        lab.setItem(GenomeLabBlockEntity.SLOT_SAMPLE, packet(CropSpecies.SOYBEAN, HARDY));
        lab.setItem(GenomeLabBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 2));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        for (int i = 0; i <= CultivarConfig.LAB_ANALYZE_TICKS.get(); i++) {
            lab.serverTick(level, abs, level.getBlockState(abs));
        }
        ItemStack card = lab.getItem(GenomeLabBlockEntity.SLOT_OUTPUT);
        check(helper, card.is(ModItems.GENOME_CARD.get()), "lab must print a genome card");
        CardData data = card.get(ModDataComponents.CARD.get());
        check(helper, data != null && data.genome().isPresent() && data.genome().get().equals(HARDY), "card must carry the sample's genome");
        check(helper, lab.getItem(GenomeLabBlockEntity.SLOT_PAPER).getCount() == 1, "one paper consumed");
        check(helper, !lab.getItem(GenomeLabBlockEntity.SLOT_SAMPLE).isEmpty(), "the sample is not consumed");
        helper.succeed();
    }

    static void seedBankRoundtrip(net.minecraft.gametest.framework.GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.SEED_BANK.get());
        helper.setBlock(pos.east(), ModBlocks.SEED_VAULT_DRAWER.get());
        helper.setBlock(pos.east().east(), ModBlocks.SEED_VAULT_DRAWER.get());
        SeedBankBlockEntity bank = helper.getBlockEntity(pos, SeedBankBlockEntity.class);
        check(helper, bank.capacity() == Math.min(CultivarConfig.SEED_BANK_MAX_LINES.get(), SeedBankBlockEntity.BASE_LINES + 2 * SeedBankBlockEntity.LINES_PER_DRAWER),
                "drawers must extend capacity, got " + bank.capacity());
        Genome line = HARDY.withGene(com.cultivar.genetics.Gene.YIELD, 3, 3);
        bank.setItem(SeedBankBlockEntity.SLOT_DEPOSIT, packet(CropSpecies.TUBER, line));
        check(helper, bank.deposit(helper.makeMockPlayer(GameType.SURVIVAL)), "deposit must succeed");
        check(helper, bank.lines().size() == 1 && bank.getItem(SeedBankBlockEntity.SLOT_DEPOSIT).isEmpty(), "line stored, packet consumed");
        bank.setItem(SeedBankBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, 5));
        check(helper, bank.withdraw(bank.lines().get(0)), "withdraw must succeed");
        ItemStack copy = bank.getItem(SeedBankBlockEntity.SLOT_OUTPUT);
        check(helper, line.equals(copy.get(ModDataComponents.GENOME.get())), "copy must carry the stored genome intact");
        check(helper, bank.getItem(SeedBankBlockEntity.SLOT_PAPER).getCount() == 5 - CultivarConfig.SEED_BANK_WITHDRAW_COST.get(), "paper cost");
        check(helper, bank.lines().size() == 1, "stored line stays after a withdrawal");
        helper.succeed();
    }

    static void mutagenChamber(net.minecraft.gametest.framework.GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.MUTAGEN_CHAMBER.get());
        MutagenChamberBlockEntity chamber = helper.getBlockEntity(pos, MutagenChamberBlockEntity.class);
        ItemStack seeds = packet(CropSpecies.BRASSICA, HARDY);
        seeds.setCount(4);
        chamber.setItem(MutagenChamberBlockEntity.FIRST_INPUT, seeds);
        chamber.setItem(MutagenChamberBlockEntity.SLOT_VIAL, new ItemStack(ModItems.MUTAGEN_VIAL.get()));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        for (int i = 0; i < MutagenChamberBlockEntity.TICKS_PER_SEED * 4 + 4; i++) {
            chamber.serverTick(level, abs, level.getBlockState(abs));
        }
        check(helper, chamber.getItem(MutagenChamberBlockEntity.FIRST_INPUT).isEmpty(), "all four seeds treated");
        int outputs = 0;
        for (int i = MutagenChamberBlockEntity.FIRST_OUTPUT; i < MutagenChamberBlockEntity.SLOTS; i++) {
            ItemStack out = chamber.getItem(i);
            outputs += out.getCount();
            Genome g = out.get(ModDataComponents.GENOME.get());
            if (g != null) {
                byte[] a = g.toBytes();
                byte[] b = HARDY.toBytes();
                int differing = 0;
                for (int j = 0; j < a.length; j++) {
                    differing += a[j] != b[j] ? 1 : 0;
                }
                check(helper, differing <= 2, "induced mutation changes at most one allele (pair normalisation may move it), got " + differing);
            }
        }
        check(helper, outputs <= 4, "outputs cannot exceed inputs");
        check(helper, chamber.getItem(MutagenChamberBlockEntity.SLOT_VIAL).isEmpty(), "the vial is used up");
        helper.succeed();
    }

    static void compostBin(net.minecraft.gametest.framework.GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.COMPOST_BIN.get());
        CompostBinBlockEntity bin = helper.getBlockEntity(pos, CompostBinBlockEntity.class);
        bin.setItem(0, new ItemStack(ModItems.produce(CropSpecies.CLOVER), 4));
        helper.runAfterDelay(60, () -> {
            check(helper, bin.charge() == CompostBinBlockEntity.FULL_CHARGE, "four produce must fill the bin, charge " + bin.charge());
            ServerLevel level = helper.getLevel();
            BlockPos abs = helper.absolutePos(pos);
            for (int i = 0; i <= CultivarConfig.COMPOST_TICKS.get(); i++) {
                bin.serverTick(level, abs, level.getBlockState(abs));
            }
            check(helper, bin.getItem(CompostBinBlockEntity.SLOT_OUTPUT).is(ModItems.COMPOST.get()), "bin must produce compost");
            helper.succeed();
        });
    }

    static void unitBoostedPollination(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CropBlockEntity target = plantCrop(helper, new BlockPos(2, 1, 2), CropSpecies.FIBER_FLAX, HARDY);
        CropBlockEntity[] donors = {
                plantCrop(helper, new BlockPos(3, 1, 2), CropSpecies.FIBER_FLAX, HARDY.withGene(com.cultivar.genetics.Gene.YIELD, 2, 2)),
                plantCrop(helper, new BlockPos(1, 1, 2), CropSpecies.FIBER_FLAX, HARDY.withGene(com.cultivar.genetics.Gene.YIELD, 2, 2)),
                plantCrop(helper, new BlockPos(2, 1, 3), CropSpecies.FIBER_FLAX, HARDY.withGene(com.cultivar.genetics.Gene.YIELD, 2, 2))};
        for (CropBlockEntity crop : new CropBlockEntity[]{target, donors[0], donors[1], donors[2]}) {
            BlockPos p = crop.getBlockPos();
            level.setBlock(p, level.getBlockState(p).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.FLOWERING), Block.UPDATE_CLIENTS);
        }
        BlockPos unitPos = new BlockPos(4, 1, 4);
        helper.setBlock(unitPos, ModBlocks.POLLINATION_UNIT.get());
        PollinationUnitBlockEntity unit = helper.getBlockEntity(unitPos, PollinationUnitBlockEntity.class);
        check(helper, unit.addFuel(new ItemStack(Items.COAL)), "coal fuels the unit");
        BlockPos absUnit = helper.absolutePos(unitPos);
        unit.serverTick(level, absUnit, level.getBlockState(absUnit));
        check(helper, unit.isActive(), "fuelled unit must be active");
        helper.runAfterDelay(2, () -> {
            PollinationSystem.tryWindPollination(level, target);
            check(helper, target.isPollinated(), "a unit-boosted flowering plant must be pollinated");
            check(helper, target.donor() != null && target.donor().first(com.cultivar.genetics.Gene.YIELD) == 2, "pollen comes from a donor");
            helper.succeed();
        });
    }

    static void isolationScreenBlocks(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos a = helper.absolutePos(new BlockPos(0, 2, 1));
        BlockPos b = helper.absolutePos(new BlockPos(4, 2, 1));
        check(helper, !PollinationSystem.isolated(level, a, b), "open air must not isolate");
        helper.setBlock(new BlockPos(2, 2, 1), ModBlocks.ISOLATION_SCREEN.get());
        check(helper, PollinationSystem.isolated(level, a, b), "a screen on the line must isolate");
        helper.succeed();
    }

    static void collectorNeverSpills(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos home = new BlockPos(0, 2, 0);
        helper.setBlock(home, ModBlocks.HARVEST_COLLECTOR.get());
        com.cultivar.blockentity.HarvestCollectorBlockEntity collector = helper.getBlockEntity(home,
                com.cultivar.blockentity.HarvestCollectorBlockEntity.class);
        Item[] filler = {Items.DIRT, Items.STONE, Items.SAND, Items.GRAVEL, Items.CLAY_BALL, Items.STICK};
        for (int i = 0; i < filler.length; i++) {
            collector.setItem(i, new ItemStack(filler[i]));
        }
        int free = com.cultivar.blockentity.HarvestCollectorBlockEntity.SIZE - filler.length;
        java.util.List<BlockPos> route = new java.util.ArrayList<>();
        for (int x = 2; x < 7; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos soil = new BlockPos(x, 1, z);
                plantCrop(helper, soil, CropSpecies.MAIZE, HARDY.withGene(com.cultivar.genetics.Gene.YIELD, (x + z) % 4, (x * z) % 4));
                BlockPos crop = helper.absolutePos(soil.above());
                level.setBlock(crop, level.getBlockState(crop).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.MAX_STAGE), Block.UPDATE_CLIENTS);
                route.add(crop);
            }
        }
        com.cultivar.entity.HarvesterCartEntity cart = ModEntities.HARVESTER_CART.get().create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        check(helper, cart != null, "cart must spawn");
        cart.assign(helper.absolutePos(home), route, free);
        level.addFreshEntity(cart);
        for (int i = 0; i < 6000 && !cart.isRemoved(); i++) {
            cart.tick();
        }
        check(helper, cart.isRemoved(), "the cart must finish its trip and unload");
        check(helper, helper.getEntities(EntityType.ITEM, new BlockPos(3, 2, 2), 12).isEmpty(), "nothing may be dropped on the ground");
        int stored = 0;
        for (int i = 0; i < collector.getContainerSize(); i++) {
            stored += collector.getItem(i).isEmpty() ? 0 : 1;
        }
        check(helper, stored > filler.length && stored <= collector.getContainerSize(), "the harvest went into the store, got " + stored);
        int stillMature = 0;
        for (BlockPos crop : route) {
            BlockState state = level.getBlockState(crop);
            if (state.getBlock() instanceof CultivarCropBlock && CultivarCropBlock.stage(state) == CultivarCropBlock.MAX_STAGE) {
                stillMature++;
            }
        }
        check(helper, stillMature > 0 && stillMature < route.size(), "the cart harvests until full and leaves the rest, mature left " + stillMature);
        helper.succeed();
    }

    static void cropBreakDrops(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        plantCrop(helper, new BlockPos(1, 1, 1), CropSpecies.GOURD, HARDY);
        BlockPos crop = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockState mature = level.getBlockState(crop).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.MAX_STAGE);
        level.setBlock(crop, mature, Block.UPDATE_CLIENTS);
        level.destroyBlock(crop, true);

        BlockPos wildPos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.setBlock(new BlockPos(3, 1, 1), ModBlocks.MANAGED_SOIL.get());
        helper.setBlock(new BlockPos(3, 2, 1), ModBlocks.crop(CropSpecies.MAIZE).defaultBlockState().setValue(CultivarCropBlock.STAGE, CultivarCropBlock.MAX_STAGE));
        var detached = level.getBlockEntity(wildPos);
        BlockState wildState = level.getBlockState(wildPos);
        level.removeBlock(wildPos, false);
        Block.dropResources(wildState, level, wildPos, detached);
        check(helper, level.getBlockState(wildPos).isAir(), "a harvested crop must not reappear after its genome is assigned during drops");

        helper.runAfterDelay(2, () -> {
            BlockPos around = new BlockPos(1, 2, 1);
            boolean produce = !helper.getEntities(ModEntities.FALLING_PRODUCE.get(), around, 4).isEmpty();
            for (ItemEntity item : helper.getEntities(EntityType.ITEM, around, 4)) {
                produce |= item.getItem().is(ModItems.produce(CropSpecies.GOURD));
            }
            check(helper, produce, "produce must drop (as Falling Produce, or an item once landed)");
            boolean seeds = false;
            for (ItemEntity item : helper.getEntities(EntityType.ITEM, around, 4)) {
                seeds |= item.getItem().is(ModItems.seedPacket(CropSpecies.GOURD));
            }
            check(helper, seeds, "seed packets must drop");
            helper.succeed();
        });
    }

    static void siteBuilders(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int index = 0;
        for (SiteKind kind : SiteKind.values()) {
            BlockPos origin = helper.absolutePos(new BlockPos(8 + index * 13, 1, 8));
            SitePiece piece = new SitePiece(kind, origin);
            piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), piece.getBoundingBox(),
                    ChunkPos.containing(origin), origin);
            int solid = 0;
            for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(kind.width() - 1, kind.height(), kind.depth() - 1))) {
                solid += level.getBlockState(p).isAir() ? 0 : 1;
            }
            check(helper, solid > kind.width(), "site " + kind.id() + " must build blocks, found " + solid);
            index++;
        }
        helper.succeed();
    }

    static void canopyStemDecodes(net.minecraft.gametest.framework.GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var resource = server.getResourceManager().getResource(Identifier.fromNamespaceAndPath("cultivar", "dimension/canopy.json"));
        check(helper, resource.isPresent(), "the canopy dimension JSON ships in the mod's data pack");
        try (Reader reader = resource.get().openAsReader()) {
            var json = JsonParser.parseReader(reader);
            var result = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), json);
            check(helper, result.result().isPresent(), "canopy level stem must decode: " + result.error().map(Object::toString).orElse(""));
            check(helper, result.result().get().generator() instanceof com.cultivar.world.dimension.CanopyChunkGenerator, "canopy generator type");
        } catch (java.io.IOException e) {
            helper.fail("could not read canopy.json: " + e.getMessage());
        }
        helper.succeed();
    }

    static void reviewRegressions(net.minecraft.gametest.framework.GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.THERMAL_SHOCK_UNIT.get());
        var shock = helper.getBlockEntity(new BlockPos(1, 1, 1), com.cultivar.blockentity.ThermalShockUnitBlockEntity.class);
        check(helper, !shock.canPlaceItem(0, new ItemStack(Items.DIRT)), "thermal shock input must reject dirt");
        check(helper, shock.canPlaceItem(0, packet(CropSpecies.MAIZE, HARDY)), "thermal shock input accepts seed packets");
        check(helper, !shock.canPlaceItem(com.cultivar.blockentity.ThermalShockUnitBlockEntity.INPUTS, packet(CropSpecies.MAIZE, HARDY)),
                "thermal shock outputs reject insertion");
        helper.setBlock(new BlockPos(3, 1, 1), ModBlocks.GENOME_LAB.get());
        GenomeLabBlockEntity lab = helper.getBlockEntity(new BlockPos(3, 1, 1), GenomeLabBlockEntity.class);
        check(helper, !lab.canPlaceItem(GenomeLabBlockEntity.SLOT_OUTPUT, new ItemStack(Items.PAPER)), "lab output rejects insertion");

        CropBlockEntity flax = plantCrop(helper, new BlockPos(5, 1, 1), CropSpecies.FIBER_FLAX, HARDY);
        BlockPos flaxPos = flax.getBlockPos();
        level.setBlock(flaxPos, level.getBlockState(flaxPos).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.FLOWERING), Block.UPDATE_CLIENTS);
        check(helper, !flax.receivePollen(CropSpecies.MAIZE, HARDY, 0, CropBlockEntity.PollinationMethod.DRONE, null), "foreign pollen rejected");
        check(helper, flax.receivePollen(CropSpecies.FIBER_FLAX, HARDY, 0, CropBlockEntity.PollinationMethod.DRONE, null), "own-species pollen accepted");

        Genome tender = HARDY.withGene(com.cultivar.genetics.Gene.COLD_TOLERANCE, 0, 0).withGene(com.cultivar.genetics.Gene.LIGHT_NEED, 4, 4);
        CropBlockEntity fragile = plantCrop(helper, new BlockPos(7, 1, 1), CropSpecies.BRASSICA, tender);
        for (int i = 0; i < 12; i++) {
            fragile.boostGrowth(level, 800f);
        }
        check(helper, level.getBlockState(fragile.getBlockPos()).getBlock() instanceof CultivarCropBlock, "bone meal must not kill a plant");
        check(helper, fragile.stress() == 0, "bone meal must not add stress");

        helper.setBlock(new BlockPos(9, 1, 1), ModBlocks.SEED_BANK.get());
        SeedBankBlockEntity bank = helper.getBlockEntity(new BlockPos(9, 1, 1), SeedBankBlockEntity.class);
        bank.setItem(SeedBankBlockEntity.SLOT_DEPOSIT, packet(CropSpecies.SOYBEAN, HARDY));
        bank.deposit(helper.makeMockPlayer(GameType.SURVIVAL));
        var lines = bank.collectComponents().get(ModDataComponents.SEED_BANK_LINES.get());
        check(helper, lines != null && lines.size() == 1, "seed bank lines must be exported to the item");
        helper.setBlock(new BlockPos(11, 1, 1), ModBlocks.SEED_BANK.get());
        SeedBankBlockEntity restored = helper.getBlockEntity(new BlockPos(11, 1, 1), SeedBankBlockEntity.class);
        ItemStack item = new ItemStack(ModItems.SEED_BANK.get());
        item.set(ModDataComponents.SEED_BANK_LINES.get(), lines);
        restored.applyComponentsFromItemStack(item);
        check(helper, restored.lines().size() == 1 && restored.lines().get(0).genome().equals(HARDY), "lines must be restored from the item");
        helper.succeed();
    }
}
