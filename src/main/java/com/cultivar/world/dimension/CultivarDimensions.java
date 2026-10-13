package com.cultivar.world.dimension;

import com.cultivar.Cultivar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class CultivarDimensions {
    public static final Identifier CANOPY_ID = Identifier.fromNamespaceAndPath(Cultivar.MODID, "canopy");
    public static final ResourceKey<DimensionType> CANOPY_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, CANOPY_ID);
    public static final ResourceKey<LevelStem> CANOPY_STEM = ResourceKey.create(Registries.LEVEL_STEM, CANOPY_ID);
    public static final ResourceKey<Level> CANOPY_LEVEL = ResourceKey.create(Registries.DIMENSION, CANOPY_ID);
    public static final ResourceKey<Biome> CANOPY_GROVE = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "canopy_grove"));
    public static final ResourceKey<Biome> CANOPY_HOLLOW = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "canopy_hollow"));

    private CultivarDimensions() {
    }
}
