package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.blockentity.BreedingPlotBlockEntity;
import com.cultivar.blockentity.BurnPanBlockEntity;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.blockentity.GenomeLabBlockEntity;
import com.cultivar.blockentity.HarvestCollectorBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.blockentity.NutrientSpreaderBlockEntity;
import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.blockentity.SprinklerBlockEntity;
import com.cultivar.blockentity.ThermalShockUnitBlockEntity;
import com.cultivar.blockentity.TrialMarkerBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Cultivar.MODID);

    public static final Supplier<BlockEntityType<CropBlockEntity>> CROP = BLOCK_ENTITIES.register("crop", () -> {
        List<Block> blocks = new ArrayList<>();
        ModBlocks.CROPS.values().forEach(b -> blocks.add(b.get()));
        blocks.add(ModBlocks.GRAFT_UNION.get());
        return new BlockEntityType<>(CropBlockEntity::new, blocks.toArray(Block[]::new));
    });
    public static final Supplier<BlockEntityType<BreedingPlotBlockEntity>> BREEDING_PLOT = BLOCK_ENTITIES.register("breeding_plot",
            () -> new BlockEntityType<>(BreedingPlotBlockEntity::new, ModBlocks.BREEDING_PLOT.get()));
    public static final Supplier<BlockEntityType<ManagedSoilBlockEntity>> MANAGED_SOIL = BLOCK_ENTITIES.register("managed_soil",
            () -> new BlockEntityType<>(ManagedSoilBlockEntity::new, ModBlocks.MANAGED_SOIL.get()));
    public static final Supplier<BlockEntityType<CompostBinBlockEntity>> COMPOST_BIN = BLOCK_ENTITIES.register("compost_bin",
            () -> new BlockEntityType<>(CompostBinBlockEntity::new, ModBlocks.COMPOST_BIN.get()));
    public static final Supplier<BlockEntityType<NutrientSpreaderBlockEntity>> NUTRIENT_SPREADER = BLOCK_ENTITIES.register("nutrient_spreader",
            () -> new BlockEntityType<>(NutrientSpreaderBlockEntity::new, ModBlocks.NUTRIENT_SPREADER.get()));
    public static final Supplier<BlockEntityType<PollinationUnitBlockEntity>> POLLINATION_UNIT = BLOCK_ENTITIES.register("pollination_unit",
            () -> new BlockEntityType<>(PollinationUnitBlockEntity::new, ModBlocks.POLLINATION_UNIT.get()));
    public static final Supplier<BlockEntityType<ProduceExchangeBlockEntity>> PRODUCE_EXCHANGE = BLOCK_ENTITIES.register("produce_exchange",
            () -> new BlockEntityType<>(ProduceExchangeBlockEntity::new, ModBlocks.PRODUCE_EXCHANGE.get()));
    public static final Supplier<BlockEntityType<ContractBoardBlockEntity>> CONTRACT_BOARD = BLOCK_ENTITIES.register("contract_board",
            () -> new BlockEntityType<>(ContractBoardBlockEntity::new, ModBlocks.CONTRACT_BOARD.get()));
    public static final Supplier<BlockEntityType<ProcessingStationBlockEntity>> PROCESSING_STATION = BLOCK_ENTITIES.register(
            "processing_station", () -> new BlockEntityType<>(ProcessingStationBlockEntity::new, ModBlocks.PROCESSING_STATION.get()));
    public static final Supplier<BlockEntityType<GeneEditorBlockEntity>> GENE_EDITOR = BLOCK_ENTITIES.register("gene_editor",
            () -> new BlockEntityType<>(GeneEditorBlockEntity::new, ModBlocks.GENE_EDITOR.get()));
    public static final Supplier<BlockEntityType<ShowBenchBlockEntity>> SHOW_BENCH = BLOCK_ENTITIES.register("show_bench",
            () -> new BlockEntityType<>(ShowBenchBlockEntity::new, ModBlocks.SHOW_BENCH.get()));
    public static final Supplier<BlockEntityType<GenomeLabBlockEntity>> GENOME_LAB = BLOCK_ENTITIES.register("genome_lab",
            () -> new BlockEntityType<>(GenomeLabBlockEntity::new, ModBlocks.GENOME_LAB.get()));
    public static final Supplier<BlockEntityType<SeedBankBlockEntity>> SEED_BANK = BLOCK_ENTITIES.register("seed_bank",
            () -> new BlockEntityType<>(SeedBankBlockEntity::new, ModBlocks.SEED_BANK.get()));
    public static final Supplier<BlockEntityType<MutagenChamberBlockEntity>> MUTAGEN_CHAMBER = BLOCK_ENTITIES.register("mutagen_chamber",
            () -> new BlockEntityType<>(MutagenChamberBlockEntity::new, ModBlocks.MUTAGEN_CHAMBER.get()));
    public static final Supplier<BlockEntityType<ThermalShockUnitBlockEntity>> THERMAL_SHOCK_UNIT = BLOCK_ENTITIES.register("thermal_shock_unit",
            () -> new BlockEntityType<>(ThermalShockUnitBlockEntity::new, ModBlocks.THERMAL_SHOCK_UNIT.get()));
    public static final Supplier<BlockEntityType<ClimateControllerBlockEntity>> CLIMATE_CONTROLLER = BLOCK_ENTITIES.register("climate_controller",
            () -> new BlockEntityType<>(ClimateControllerBlockEntity::new, ModBlocks.CLIMATE_CONTROLLER.get()));
    public static final Supplier<BlockEntityType<SprinklerBlockEntity>> SPRINKLER = BLOCK_ENTITIES.register("sprinkler",
            () -> new BlockEntityType<>(SprinklerBlockEntity::new, ModBlocks.SPRINKLER.get()));
    public static final Supplier<BlockEntityType<HarvestCollectorBlockEntity>> HARVEST_COLLECTOR = BLOCK_ENTITIES.register("harvest_collector",
            () -> new BlockEntityType<>(HarvestCollectorBlockEntity::new, ModBlocks.HARVEST_COLLECTOR.get()));
    public static final Supplier<BlockEntityType<TrialMarkerBlockEntity>> TRIAL_MARKER = BLOCK_ENTITIES.register("trial_marker",
            () -> new BlockEntityType<>(TrialMarkerBlockEntity::new, ModBlocks.TRIAL_MARKER.get()));
    public static final Supplier<BlockEntityType<BurnPanBlockEntity>> BURN_PAN = BLOCK_ENTITIES.register("burn_pan",
            () -> new BlockEntityType<>(BurnPanBlockEntity::new, ModBlocks.BURN_PAN.get()));

    private ModBlockEntities() {
    }
}
