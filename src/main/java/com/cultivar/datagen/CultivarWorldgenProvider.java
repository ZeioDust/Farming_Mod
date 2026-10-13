package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.data.CultivarRegistries;
import com.cultivar.data.DefaultData;
import com.cultivar.registry.ModTags;
import com.cultivar.registry.ModWorldgen;
import com.cultivar.world.dimension.CanopyChunkGenerator;
import com.cultivar.world.dimension.CultivarDimensions;
import com.cultivar.world.gen.ConfigGatedFeaturesModifier;
import com.cultivar.world.gen.CropPatchFeature;
import com.cultivar.world.gen.CultivarSiteStructure;
import com.cultivar.world.gen.SiteKind;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.timeline.Timeline;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CultivarWorldgenProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> FERAL_CROPS_CONFIGURED = configured("feral_crop_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CANOPY_CROPS_CONFIGURED = configured("canopy_wild_crops");
    public static final ResourceKey<PlacedFeature> FERAL_CROPS_PLACED = placed("feral_crop_patch");
    public static final ResourceKey<PlacedFeature> CANOPY_CROPS_PLACED = placed("canopy_wild_crops");
    public static final ResourceKey<BiomeModifier> FERAL_CROPS_MODIFIER = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("feral_crops"));

    private static final Map<SiteKind, int[]> SPACING = new EnumMap<>(SiteKind.class);
    private static final Map<SiteKind, TagKey<Biome>> BIOMES = new EnumMap<>(SiteKind.class);

    static {
        SPACING.put(SiteKind.ABANDONED_TRIAL_FIELD, new int[]{32, 10, 734526401});
        SPACING.put(SiteKind.RUINED_GREENHOUSE, new int[]{38, 12, 734526402});
        SPACING.put(SiteKind.OLD_SEED_STORE, new int[]{40, 14, 734526403});
        SPACING.put(SiteKind.COMPOST_YARD, new int[]{34, 10, 734526404});
        SPACING.put(SiteKind.BLIGHT_SCAR, new int[]{36, 12, 734526405});
        SPACING.put(SiteKind.CANOPY_PLATFORM, new int[]{18, 6, 734526406});
        SPACING.put(SiteKind.WILD_STAND, new int[]{10, 3, 734526407});
        SPACING.put(SiteKind.CANOPY_BREACH, new int[]{44, 14, 734526408});
        BIOMES.put(SiteKind.ABANDONED_TRIAL_FIELD, ModTags.Biomes.HAS_TRIAL_FIELD);
        BIOMES.put(SiteKind.RUINED_GREENHOUSE, ModTags.Biomes.HAS_RUINED_GREENHOUSE);
        BIOMES.put(SiteKind.OLD_SEED_STORE, ModTags.Biomes.HAS_SEED_STORE);
        BIOMES.put(SiteKind.COMPOST_YARD, ModTags.Biomes.HAS_COMPOST_YARD);
        BIOMES.put(SiteKind.BLIGHT_SCAR, ModTags.Biomes.HAS_BLIGHT_SCAR);
        BIOMES.put(SiteKind.CANOPY_PLATFORM, ModTags.Biomes.HAS_CANOPY_PLATFORM);
        BIOMES.put(SiteKind.WILD_STAND, ModTags.Biomes.HAS_WILD_STAND);
        BIOMES.put(SiteKind.CANOPY_BREACH, ModTags.Biomes.HAS_CANOPY_BREACH);
    }

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, CultivarWorldgenProvider::dimensionTypes)
            .add(Registries.CONFIGURED_FEATURE, CultivarWorldgenProvider::configuredFeatures)
            .add(Registries.PLACED_FEATURE, CultivarWorldgenProvider::placedFeatures)
            .add(Registries.STRUCTURE, CultivarWorldgenProvider::structures)
            .add(Registries.STRUCTURE_SET, CultivarWorldgenProvider::structureSets)
            .add(Registries.BIOME, CultivarWorldgenProvider::biomes)
            .add(Registries.LEVEL_STEM, CultivarWorldgenProvider::levelStems)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, CultivarWorldgenProvider::biomeModifiers)
            .add(CultivarRegistries.GENE, ctx -> DefaultData.genes().forEach((key, v) -> ctx.register(key(CultivarRegistries.GENE, key), v)))
            .add(CultivarRegistries.ALLELE, ctx -> DefaultData.alleles().forEach((key, v) -> ctx.register(key(CultivarRegistries.ALLELE, key), v)))
            .add(CultivarRegistries.SPECIES, ctx -> DefaultData.species().forEach((key, v) -> ctx.register(key(CultivarRegistries.SPECIES, key), v)))
            .add(CultivarRegistries.PH_BAND, ctx -> DefaultData.phBands().forEach((key, v) -> ctx.register(key(CultivarRegistries.PH_BAND, key), v)))
            .add(CultivarRegistries.NITROGEN_FIXER, ctx -> DefaultData.nitrogenFixers().forEach((key, v) -> ctx.register(key(CultivarRegistries.NITROGEN_FIXER, key), v)))
            .add(CultivarRegistries.BLIGHT_STRAIN, ctx -> DefaultData.blightStrains().forEach((key, v) -> ctx.register(key(CultivarRegistries.BLIGHT_STRAIN, key), v)));

    private CultivarWorldgenProvider() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, path);
    }

    private static <T> ResourceKey<T> key(ResourceKey<? extends net.minecraft.core.Registry<T>> registry, Identifier id) {
        return ResourceKey.create(registry, id);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id(path));
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(path));
    }

    public static ResourceKey<Structure> structure(SiteKind kind) {
        return ResourceKey.create(Registries.STRUCTURE, id(kind.id()));
    }

    private static ResourceKey<StructureSet> structureSet(SiteKind kind) {
        return ResourceKey.create(Registries.STRUCTURE_SET, id(kind.id()));
    }

    private static void dimensionTypes(BootstrapContext<DimensionType> context) {
        HolderGetter<Timeline> timelines = context.lookup(Registries.TIMELINE);
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF5E8A54)
                .set(EnvironmentAttributes.SKY_COLOR, 0xFF6E9E7A)
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 12.0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 128.0f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.55f)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF1A2A18)
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 240.0f)
                .build();
        context.register(CultivarDimensions.CANOPY_TYPE, new DimensionType(false, true, false, false, 1.0,
                CanopyChunkGenerator.MIN_Y, CanopyChunkGenerator.HEIGHT, CanopyChunkGenerator.HEIGHT, BlockTags.INFINIBURN_OVERWORLD, 0.08f,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0), DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT,
                attributes, timelines.getOrThrow(TimelineTags.IN_OVERWORLD), Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));
    }

    private static void levelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(CultivarDimensions.CANOPY_STEM, new LevelStem(types.getOrThrow(CultivarDimensions.CANOPY_TYPE),
                new CanopyChunkGenerator(new FixedBiomeSource(biomes.getOrThrow(CultivarDimensions.CANOPY_GROVE)))));
    }

    private static void biomes(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, context.lookup(Registries.CONFIGURED_CARVER));
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CANOPY_CROPS_PLACED);
        context.register(CultivarDimensions.CANOPY_GROVE, new Biome.BiomeBuilder()
                .hasPrecipitation(true).temperature(0.85f).downfall(0.9f)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF6E9E7A)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF5E8A54)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x3F8A5A).foliageColorOverride(0x3E7A30)
                        .grassColorOverride(0x4E8A3A).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(generation.build())
                .build());
    }

    private static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(FERAL_CROPS_CONFIGURED, new ConfiguredFeature<>(ModWorldgen.CROP_PATCH.get(), new CropPatchFeature.Config(10, 3, 2)));
        context.register(CANOPY_CROPS_CONFIGURED, new ConfiguredFeature<>(ModWorldgen.CROP_PATCH.get(), new CropPatchFeature.Config(14, 4, 3)));
    }

    private static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        PlacementUtils.register(context, FERAL_CROPS_PLACED, configured.getOrThrow(FERAL_CROPS_CONFIGURED),
                RarityFilter.onAverageOnceEvery(40), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        PlacementUtils.register(context, CANOPY_CROPS_PLACED, configured.getOrThrow(CANOPY_CROPS_CONFIGURED),
                CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        context.register(FERAL_CROPS_MODIFIER, new ConfigGatedFeaturesModifier(biomes.getOrThrow(ModTags.Biomes.FERAL_CROPS),
                HolderSet.direct(placedFeatures.getOrThrow(FERAL_CROPS_PLACED)), GenerationStep.Decoration.VEGETAL_DECORATION, "geneticsEnabled"));
    }

    private static void structures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (SiteKind kind : SiteKind.values()) {
            context.register(structure(kind), new CultivarSiteStructure(new Structure.StructureSettings.Builder(biomes.getOrThrow(BIOMES.get(kind)))
                    .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES).build(), kind));
        }
    }

    private static void structureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        for (SiteKind kind : SiteKind.values()) {
            int[] spacing = SPACING.get(kind);
            context.register(structureSet(kind), new StructureSet(structures.getOrThrow(structure(kind)),
                    new RandomSpreadStructurePlacement(spacing[0], spacing[1], RandomSpreadType.LINEAR, spacing[2])));
        }
    }
}
