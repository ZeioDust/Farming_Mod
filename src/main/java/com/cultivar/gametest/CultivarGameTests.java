package com.cultivar.gametest;

import com.cultivar.Cultivar;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.BlightStrain;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.Nutrient;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.guide.Guide;
import com.cultivar.guide.GuideState;
import com.cultivar.guide.Hint;
import com.cultivar.guide.Milestones;
import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.world.BlightSystem;
import com.cultivar.world.HarvestLogic;
import com.cultivar.world.PollinationSystem;
import com.cultivar.world.WindVector;
import com.cultivar.world.dimension.CultivarDimensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class CultivarGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, Cultivar.MODID);

    static final Genome HARDY = Genome.homozygous(1, 1, 4, 4, 4, 0, 2, 0);

    private static final List<String> NAMES = List.of("crop_growth_and_soil", "nitrogen_fixer", "cross_harvest", "blight_immunity",
            "greenhouse_seals", "canopy_dimension", "catalog_loaded", "wind_bias", "genome_lab_prints_card", "seed_bank_roundtrip",
            "mutagen_chamber", "compost_bin", "unit_boosted_pollination", "isolation_screen_blocks", "crop_break_drops", "site_builders",
            "canopy_stem_decodes", "review_regressions", "guide_hints", "exchange_sells", "contract_delivery", "processing_keeps_grade",
            "ration_recipe", "show_bench_judges", "gene_editor_needs_atlas", "collector_never_spills");

    static {
        FUNCTIONS.register("crop_growth_and_soil", () -> CultivarGameTests::cropGrowthAndSoil);
        FUNCTIONS.register("nitrogen_fixer", () -> CultivarGameTests::nitrogenFixer);
        FUNCTIONS.register("cross_harvest", () -> CultivarGameTests::crossHarvest);
        FUNCTIONS.register("blight_immunity", () -> CultivarGameTests::blightImmunity);
        FUNCTIONS.register("greenhouse_seals", () -> CultivarGameTests::greenhouseSeals);
        FUNCTIONS.register("canopy_dimension", () -> CultivarGameTests::canopyDimension);
        FUNCTIONS.register("catalog_loaded", () -> CultivarGameTests::catalogLoaded);
        FUNCTIONS.register("wind_bias", () -> CultivarGameTests::windBias);
        FUNCTIONS.register("genome_lab_prints_card", () -> MachineGameTests::genomeLabPrintsCard);
        FUNCTIONS.register("seed_bank_roundtrip", () -> MachineGameTests::seedBankRoundtrip);
        FUNCTIONS.register("mutagen_chamber", () -> MachineGameTests::mutagenChamber);
        FUNCTIONS.register("compost_bin", () -> MachineGameTests::compostBin);
        FUNCTIONS.register("unit_boosted_pollination", () -> MachineGameTests::unitBoostedPollination);
        FUNCTIONS.register("isolation_screen_blocks", () -> MachineGameTests::isolationScreenBlocks);
        FUNCTIONS.register("crop_break_drops", () -> MachineGameTests::cropBreakDrops);
        FUNCTIONS.register("site_builders", () -> MachineGameTests::siteBuilders);
        FUNCTIONS.register("canopy_stem_decodes", () -> MachineGameTests::canopyStemDecodes);
        FUNCTIONS.register("review_regressions", () -> MachineGameTests::reviewRegressions);
        FUNCTIONS.register("guide_hints", () -> CultivarGameTests::guideHints);
        FUNCTIONS.register("exchange_sells", () -> MarketGameTests::exchangeSells);
        FUNCTIONS.register("contract_delivery", () -> MarketGameTests::contractDelivery);
        FUNCTIONS.register("processing_keeps_grade", () -> MarketGameTests::processingKeepsGrade);
        FUNCTIONS.register("ration_recipe", () -> MarketGameTests::rationRecipe);
        FUNCTIONS.register("show_bench_judges", () -> MarketGameTests::showBenchJudges);
        FUNCTIONS.register("gene_editor_needs_atlas", () -> MarketGameTests::geneEditorNeedsAtlas);
        FUNCTIONS.register("collector_never_spills", () -> MachineGameTests::collectorNeverSpills);
    }

    private CultivarGameTests() {
    }

    @SubscribeEvent
    static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(Cultivar.MODID, "default"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        for (String name : NAMES) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(Cultivar.MODID, name));
            event.registerTest(Identifier.fromNamespaceAndPath(Cultivar.MODID, name), new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1, true, 4)));
        }
    }

    static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    static CropBlockEntity plantCrop(GameTestHelper helper, BlockPos soil, CropSpecies species, Genome genome) {
        helper.setBlock(soil, ModBlocks.MANAGED_SOIL.get().defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
        helper.setBlock(soil.above(), ModBlocks.crop(species).defaultBlockState());
        CropBlockEntity crop = helper.getBlockEntity(soil.above(), CropBlockEntity.class);
        crop.plant(genome, 0, false);
        return crop;
    }

    private static void guideHints(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Guide.hint(player, Hint.PLANTED);
        Guide.hint(player, Hint.PLANTED);
        GuideState state = player.getData(ModAttachments.GUIDE);
        check(helper, state.seen(Hint.PLANTED.id()) && state.done(Milestones.PLANTED), "planted hint must be seen and recorded");
        check(helper, state.hintsSeen().size() == 1, "a hint is shown only once, got " + state.hintsSeen());
        CultivarTrigger.fire(player, CultivarTrigger.FIRST_CROSS);
        check(helper, player.getData(ModAttachments.GUIDE).done(Milestones.FIRST_CROSS), "advancement actions must record milestones");
        player.setData(ModAttachments.GUIDE, player.getData(ModAttachments.GUIDE).withHintsOff(true));
        Guide.hint(player, Hint.BLIGHT);
        GuideState off = player.getData(ModAttachments.GUIDE);
        check(helper, off.done(Milestones.BLIGHT) && !off.seen(Hint.BLIGHT.id()), "hints off: progress recorded, hint not shown");
        var encoded = GuideState.MAP_CODEC.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, off).getOrThrow();
        GuideState decoded = GuideState.MAP_CODEC.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, encoded).getOrThrow();
        check(helper, decoded.equals(off), "guide state must survive a codec round trip");
        helper.succeed();
    }

    private static void cropGrowthAndSoil(GameTestHelper helper) {
        BlockPos soilPos = new BlockPos(1, 1, 1);
        CropBlockEntity crop = plantCrop(helper, soilPos, CropSpecies.MAIZE, HARDY);
        ManagedSoilBlockEntity soil = helper.getBlockEntity(soilPos, ManagedSoilBlockEntity.class);
        float before = soil.get(Nutrient.NITROGEN);
        for (int i = 0; i < 8; i++) {
            crop.growthUpdate(helper.getLevel(), 2000);
        }
        int stage = CultivarCropBlock.stage(helper.getLevel().getBlockState(helper.absolutePos(soilPos.above())));
        check(helper, stage == CultivarCropBlock.MAX_STAGE, "crop should mature, stage " + stage + " limit " + crop.lastLimit());
        check(helper, soil.get(Nutrient.NITROGEN) < before, "maize should draw nitrogen");
        helper.succeed();
    }

    private static void nitrogenFixer(GameTestHelper helper) {
        BlockPos soilPos = new BlockPos(1, 1, 1);
        CropBlockEntity crop = plantCrop(helper, soilPos, CropSpecies.CLOVER, HARDY);
        ManagedSoilBlockEntity soil = helper.getBlockEntity(soilPos, ManagedSoilBlockEntity.class);
        float before = soil.get(Nutrient.NITROGEN);
        float potassium = soil.get(Nutrient.POTASSIUM);
        for (int i = 0; i < 8; i++) {
            crop.growthUpdate(helper.getLevel(), 2000);
        }
        check(helper, soil.get(Nutrient.NITROGEN) > before, "clover should fix nitrogen: " + before + " -> " + soil.get(Nutrient.NITROGEN));
        check(helper, soil.get(Nutrient.POTASSIUM) < potassium, "clover still draws potassium");
        helper.succeed();
    }

    private static void crossHarvest(GameTestHelper helper) {
        Genome mother = HARDY;
        Genome father = HARDY.withGene(Gene.YIELD, 3, 3).withGene(Gene.GROWTH_RATE, 3, 3);
        CropBlockEntity crop = plantCrop(helper, new BlockPos(1, 1, 1), CropSpecies.WHEATLINE, mother);
        ServerLevel level = helper.getLevel();
        BlockPos cropPos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(cropPos, level.getBlockState(cropPos).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.FLOWERING), 2);
        check(helper, crop.receivePollen(CropSpecies.WHEATLINE, father, 0, CropBlockEntity.PollinationMethod.BRUSH, null), "flowering plant must accept pollen");
        level.setBlock(cropPos, level.getBlockState(cropPos).setValue(CultivarCropBlock.STAGE, CultivarCropBlock.MAX_STAGE), 2);
        HarvestLogic.Result result = HarvestLogic.harvest(level, crop, level.getBlockState(cropPos), null);
        int seeds = result.seeds().stream().mapToInt(ItemStack::getCount).sum();
        check(helper, seeds == Catalog.get(level).species(CropSpecies.WHEATLINE).seedsPerHarvest(), "seed count " + seeds);
        for (ItemStack stack : result.seeds()) {
            Genome child = stack.get(ModDataComponents.GENOME.get());
            check(helper, child != null, "seed packet without genome");
            check(helper, child.first(Gene.YIELD) == 1 && child.second(Gene.YIELD) == 3, "yield must be 1/3, got " + child.toHex());
            check(helper, child.first(Gene.GROWTH_RATE) == 1 && child.second(Gene.GROWTH_RATE) == 3, "growth must be 1/3");
        }
        check(helper, !result.produce().isEmpty(), "mature crop must yield produce");
        helper.succeed();
    }

    private static void blightImmunity(GameTestHelper helper) {
        Catalog catalog = Catalog.get(helper.getLevel());
        BlightStrain leafSpot = catalog.strain(Identifier.fromNamespaceAndPath(Cultivar.MODID, "leaf_spot")).orElseThrow();
        CropBlockEntity resistant = plantCrop(helper, new BlockPos(1, 1, 1), CropSpecies.WHEATLINE, HARDY.withGene(Gene.BLIGHT_RESISTANCE, 4, 4));
        CropBlockEntity susceptible = plantCrop(helper, new BlockPos(3, 1, 1), CropSpecies.WHEATLINE, HARDY);
        check(helper, BlightSystem.isImmune(resistant, leafSpot, catalog.table()), "wild canopy allele must defeat leaf spot");
        check(helper, !BlightSystem.isImmune(susceptible, leafSpot, catalog.table()), "susceptible plant must not be immune");
        float max = BlightSystem.monocultureMultiplier(1f);
        check(helper, max > BlightSystem.monocultureMultiplier(0.3f) && BlightSystem.monocultureMultiplier(0f) == 1f,
                "monoculture multiplier must grow with similarity");
        susceptible.infect(Identifier.fromNamespaceAndPath(Cultivar.MODID, "leaf_spot"));
        check(helper, susceptible.isBlighted(), "infect must mark the plant blighted");
        check(helper, helper.getLevel().getBlockState(susceptible.getBlockPos()).getValue(CultivarCropBlock.BLIGHTED), "blight overlay state");
        helper.succeed();
    }

    private static void greenhouseSeals(GameTestHelper helper) {
        for (int x = 0; x <= 4; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = 0; z <= 4; z++) {
                    boolean shell = x == 0 || x == 4 || y == 1 || y == 5 || z == 0 || z == 4;
                    helper.setBlock(x, y, z, shell ? ModBlocks.GREENHOUSE_PANE.get().defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        helper.setBlock(2, 2, 2, ModBlocks.CLIMATE_CONTROLLER.get().defaultBlockState());
        ClimateControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(2, 2, 2), ClimateControllerBlockEntity.class);
        controller.markDirty();
        helper.succeedWhen(() -> {
            check(helper, controller.isSealed(), "greenhouse should seal");
            check(helper, controller.volume() == 26, "volume should be 26, was " + controller.volume());
        });
    }

    private static void canopyDimension(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        check(helper, access.lookupOrThrow(Registries.DIMENSION_TYPE).get(CultivarDimensions.CANOPY_TYPE).isPresent(), "canopy dimension type");
        check(helper, access.lookupOrThrow(Registries.BIOME).get(CultivarDimensions.CANOPY_GROVE).isPresent(), "canopy grove biome");
        var column = com.cultivar.world.dimension.CanopyChunkGenerator.column(40, 40);
        check(helper, column[0].is(Blocks.BEDROCK), "canopy floor must be bedrock");
        check(helper, column[com.cultivar.world.dimension.CanopyChunkGenerator.FLOOR_TOP].is(ModBlocks.CANOPY_LEAFMASS.get()), "floor top is leafmass");
        int platforms = 0;
        for (int x = 0; x < 64; x++) {
            var c = com.cultivar.world.dimension.CanopyChunkGenerator.column(x, 7);
            for (int level : com.cultivar.world.dimension.CanopyChunkGenerator.PLATFORM_LEVELS) {
                if (c[level].is(ModBlocks.CANOPY_LEAFMASS.get()) || c[level].is(ModBlocks.CANOPY_BARK.get())) {
                    platforms++;
                }
            }
        }
        check(helper, platforms > 20, "platform layers must be present, found " + platforms);
        helper.succeed();
    }

    private static void catalogLoaded(GameTestHelper helper) {
        Catalog catalog = Catalog.get(helper.getLevel());
        for (Gene gene : Gene.VALUES) {
            check(helper, catalog.table().alleles(gene).size() >= 5, "gene " + gene + " needs at least five alleles");
        }
        check(helper, catalog.strains().size() >= 3, "at least three blight strains");
        int fixers = 0;
        for (CropSpecies species : CropSpecies.VALUES) {
            if (catalog.nitrogenFixer(species.key()).isPresent()) {
                fixers++;
            }
        }
        check(helper, fixers >= 3, "at least three nitrogen fixers, found " + fixers);
        helper.succeed();
    }

    private static void windBias(GameTestHelper helper) {
        WindVector east = new WindVector(1f, 0f, 1f, 0L);
        BlockPos donor = new BlockPos(0, 0, 0);
        float downwind = PollinationSystem.chance(0.35f, 4, donor, new BlockPos(2, 0, 0), east, 0.6f);
        float upwind = PollinationSystem.chance(0.35f, 4, donor, new BlockPos(-2, 0, 0), east, 0.6f);
        float far = PollinationSystem.chance(0.35f, 4, donor, new BlockPos(4, 0, 0), east, 0.6f);
        check(helper, downwind > upwind, "downwind targets must be favoured");
        check(helper, downwind > far, "chance must fall off with distance");
        helper.succeed();
    }
}
