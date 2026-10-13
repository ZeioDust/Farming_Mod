package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.block.AmendmentHopperBlock;
import com.cultivar.block.BreedingPlotBlock;
import com.cultivar.block.BurnPanBlock;
import com.cultivar.block.CanopyPortalBlock;
import com.cultivar.block.CanopyPortalFrameBlock;
import com.cultivar.block.ClimateControllerBlock;
import com.cultivar.block.CompostBinBlock;
import com.cultivar.block.ContractBoardBlock;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.DepletedSoilBlock;
import com.cultivar.block.GeneEditorBlock;
import com.cultivar.block.GenomeLabBlock;
import com.cultivar.block.GraftUnionBlock;
import com.cultivar.block.GreenhousePaneBlock;
import com.cultivar.block.GrowLightBlock;
import com.cultivar.block.HarvestCollectorBlock;
import com.cultivar.block.HumidityDeviceBlock;
import com.cultivar.block.IrrigationLineBlock;
import com.cultivar.block.IsolationScreenBlock;
import com.cultivar.block.ManagedSoilBlock;
import com.cultivar.block.MutagenChamberBlock;
import com.cultivar.block.NutrientSpreaderBlock;
import com.cultivar.block.PollinationUnitBlock;
import com.cultivar.block.ProcessingStationBlock;
import com.cultivar.block.ProduceExchangeBlock;
import com.cultivar.block.SeedBankBlock;
import com.cultivar.block.SeedVaultDrawerBlock;
import com.cultivar.block.ShowBenchBlock;
import com.cultivar.block.SprinklerBlock;
import com.cultivar.block.ThermalShockUnitBlock;
import com.cultivar.block.TrialMarkerBlock;
import com.cultivar.block.WildVineBlock;
import com.cultivar.data.CropSpecies;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Cultivar.MODID);

    public static final Map<CropSpecies, DeferredBlock<CultivarCropBlock>> CROPS = new EnumMap<>(CropSpecies.class);

    static {
        for (CropSpecies species : CropSpecies.VALUES) {
            CROPS.put(species, BLOCKS.registerBlock(species.cropBlockName(), p -> new CultivarCropBlock(species, p), ModBlocks::cropProps));
        }
    }

    public static final DeferredBlock<GraftUnionBlock> GRAFT_UNION = BLOCKS.registerBlock("graft_union", GraftUnionBlock::new, ModBlocks::cropProps);

    public static final DeferredBlock<BreedingPlotBlock> BREEDING_PLOT = BLOCKS.registerBlock("breeding_plot", BreedingPlotBlock::new,
            p -> soilProps(p).mapColor(MapColor.COLOR_BROWN));
    public static final DeferredBlock<ManagedSoilBlock> MANAGED_SOIL = BLOCKS.registerBlock("managed_soil", ManagedSoilBlock::new,
            p -> soilProps(p).mapColor(MapColor.DIRT));
    public static final DeferredBlock<DepletedSoilBlock> DEPLETED_SOIL = BLOCKS.registerBlock("depleted_soil", DepletedSoilBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.6f).sound(SoundType.GRAVEL));
    public static final DeferredBlock<CompostBinBlock> COMPOST_BIN = BLOCKS.registerBlock("compost_bin", CompostBinBlock::new,
            p -> woodMachine(p));
    public static final DeferredBlock<AmendmentHopperBlock> LIME_HOPPER = BLOCKS.registerBlock("lime_hopper",
            p -> new AmendmentHopperBlock(AmendmentHopperBlock.Kind.LIME, p), ModBlocks::metalMachine);
    public static final DeferredBlock<AmendmentHopperBlock> SULFUR_HOPPER = BLOCKS.registerBlock("sulfur_hopper",
            p -> new AmendmentHopperBlock(AmendmentHopperBlock.Kind.SULFUR, p), ModBlocks::metalMachine);
    public static final DeferredBlock<NutrientSpreaderBlock> NUTRIENT_SPREADER = BLOCKS.registerBlock("nutrient_spreader",
            NutrientSpreaderBlock::new, ModBlocks::metalMachine);
    public static final DeferredBlock<IsolationScreenBlock> ISOLATION_SCREEN = BLOCKS.registerBlock("isolation_screen",
            IsolationScreenBlock::new, p -> p.mapColor(MapColor.WOOL).strength(0.5f).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<PollinationUnitBlock> POLLINATION_UNIT = BLOCKS.registerBlock("pollination_unit",
            PollinationUnitBlock::new, ModBlocks::metalMachine);
    public static final DeferredBlock<BurnPanBlock> BURN_PAN = BLOCKS.registerBlock("burn_pan", BurnPanBlock::new,
            p -> metalMachine(p).noOcclusion().lightLevel(s -> s.getValue(BurnPanBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<HarvestCollectorBlock> HARVEST_COLLECTOR = BLOCKS.registerBlock("harvest_collector",
            HarvestCollectorBlock::new, ModBlocks::woodMachine);
    public static final DeferredBlock<TrialMarkerBlock> TRIAL_MARKER = BLOCKS.registerBlock("trial_marker", TrialMarkerBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(0.5f).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<GenomeLabBlock> GENOME_LAB = BLOCKS.registerBlock("genome_lab", GenomeLabBlock::new, ModBlocks::labMachine);
    public static final DeferredBlock<SeedBankBlock> SEED_BANK = BLOCKS.registerBlock("seed_bank", SeedBankBlock::new, ModBlocks::labMachine);
    public static final DeferredBlock<SeedVaultDrawerBlock> SEED_VAULT_DRAWER = BLOCKS.registerBlock("seed_vault_drawer",
            SeedVaultDrawerBlock::new, ModBlocks::labMachine);
    public static final DeferredBlock<MutagenChamberBlock> MUTAGEN_CHAMBER = BLOCKS.registerBlock("mutagen_chamber",
            MutagenChamberBlock::new, ModBlocks::labMachine);
    public static final DeferredBlock<ThermalShockUnitBlock> THERMAL_SHOCK_UNIT = BLOCKS.registerBlock("thermal_shock_unit",
            ThermalShockUnitBlock::new, ModBlocks::labMachine);

    public static final DeferredBlock<ProduceExchangeBlock> PRODUCE_EXCHANGE = BLOCKS.registerBlock("produce_exchange",
            ProduceExchangeBlock::new, ModBlocks::woodMachine);
    public static final DeferredBlock<ContractBoardBlock> CONTRACT_BOARD = BLOCKS.registerBlock("contract_board",
            ContractBoardBlock::new, ModBlocks::woodMachine);
    public static final DeferredBlock<ProcessingStationBlock> PROCESSING_STATION = BLOCKS.registerBlock("processing_station",
            ProcessingStationBlock::new, ModBlocks::metalMachine);
    public static final DeferredBlock<GeneEditorBlock> GENE_EDITOR = BLOCKS.registerBlock("gene_editor", GeneEditorBlock::new,
            p -> labMachine(p).lightLevel(s -> 6));
    public static final DeferredBlock<ShowBenchBlock> SHOW_BENCH = BLOCKS.registerBlock("show_bench", ShowBenchBlock::new,
            ModBlocks::woodMachine);

    public static final DeferredBlock<GreenhousePaneBlock> GREENHOUSE_PANE = BLOCKS.registerBlock("greenhouse_pane",
            GreenhousePaneBlock::new, p -> p.mapColor(MapColor.NONE).strength(0.4f).sound(SoundType.GLASS).noOcclusion()
                    .isValidSpawn((s, l, pos, e) -> false).isRedstoneConductor((s, l, pos) -> false)
                    .isSuffocating((s, l, pos) -> false).isViewBlocking((s, l, pos) -> false));
    public static final DeferredBlock<GrowLightBlock> GROW_LIGHT = BLOCKS.registerBlock("grow_light", GrowLightBlock::new,
            p -> labMachine(p).lightLevel(s -> 15));
    public static final DeferredBlock<HumidityDeviceBlock> HUMIDIFIER = BLOCKS.registerBlock("humidifier",
            p -> new HumidityDeviceBlock(true, p), p -> labMachine(p).randomTicks());
    public static final DeferredBlock<HumidityDeviceBlock> DEHUMIDIFIER = BLOCKS.registerBlock("dehumidifier",
            p -> new HumidityDeviceBlock(false, p), ModBlocks::labMachine);
    public static final DeferredBlock<ClimateControllerBlock> CLIMATE_CONTROLLER = BLOCKS.registerBlock("climate_controller",
            ClimateControllerBlock::new, ModBlocks::labMachine);
    public static final DeferredBlock<IrrigationLineBlock> IRRIGATION_LINE = BLOCKS.registerBlock("irrigation_line",
            IrrigationLineBlock::new, p -> p.mapColor(MapColor.METAL).strength(0.8f).sound(SoundType.COPPER).noOcclusion().randomTicks());
    public static final DeferredBlock<SprinklerBlock> SPRINKLER = BLOCKS.registerBlock("sprinkler", SprinklerBlock::new,
            p -> metalMachine(p).noOcclusion());

    public static final DeferredBlock<RotatedPillarBlock> CANOPY_BARK = BLOCKS.registerBlock("canopy_bark", RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredBlock<Block> CANOPY_LEAFMASS = BLOCKS.registerBlock("canopy_leafmass", Block::new,
            p -> p.mapColor(MapColor.PLANT).strength(0.6f).sound(SoundType.AZALEA_LEAVES));
    public static final DeferredBlock<WildVineBlock> WILD_VINE_BLOCK = BLOCKS.registerBlock("wild_vine_block", WildVineBlock::new,
            p -> p.mapColor(MapColor.PLANT).strength(0.3f).sound(SoundType.VINE).noCollision().noOcclusion().replaceable()
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<CanopyPortalFrameBlock> CANOPY_PORTAL_FRAME = BLOCKS.registerBlock("canopy_portal_frame",
            CanopyPortalFrameBlock::new, p -> p.mapColor(MapColor.COLOR_GREEN).strength(-1.0f, 3600000.0f).sound(SoundType.WOOD));
    public static final DeferredBlock<CanopyPortalBlock> CANOPY_PORTAL = BLOCKS.registerBlock("canopy_portal", CanopyPortalBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN).noCollision().strength(-1.0f).sound(SoundType.GLASS).lightLevel(s -> 11)
                    .noLootTable().pushReaction(PushReaction.BLOCK));

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties cropProps(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY).noOcclusion();
    }

    private static BlockBehaviour.Properties soilProps(BlockBehaviour.Properties p) {
        return p.strength(0.6f).sound(SoundType.GRAVEL).randomTicks().isViewBlocking((s, l, pos) -> true).isSuffocating((s, l, pos) -> true);
    }

    private static BlockBehaviour.Properties woodMachine(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties metalMachine(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.METAL).strength(3.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties labMachine(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.QUARTZ).strength(2.5f, 6.0f).sound(SoundType.METAL);
    }

    public static CultivarCropBlock crop(CropSpecies species) {
        return CROPS.get(species).get();
    }
}
