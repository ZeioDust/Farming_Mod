package com.cultivar.world.gen;

import com.cultivar.CultivarConfig;
import com.cultivar.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record ConfigGatedFeaturesModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step,
                                          String gate) implements BiomeModifier {
    public static final MapCodec<ConfigGatedFeaturesModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigGatedFeaturesModifier::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConfigGatedFeaturesModifier::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigGatedFeaturesModifier::step),
            Codec.STRING.fieldOf("gate").forGetter(ConfigGatedFeaturesModifier::gate)
    ).apply(i, ConfigGatedFeaturesModifier::new));

    public static boolean gateOpen(String gate) {
        if (!CultivarConfig.isLoaded()) {
            return true;
        }
        return switch (gate) {
            case "geneticsEnabled" -> CultivarConfig.GENETICS_ENABLED.get();
            case "canopyEnabled" -> CultivarConfig.CANOPY_ENABLED.get();
            case "blightEnabled" -> CultivarConfig.BLIGHT_ENABLED.get();
            case "soilChemistryEnabled" -> CultivarConfig.SOIL_CHEMISTRY_ENABLED.get();
            default -> true;
        };
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome) && gateOpen(gate)) {
            features.forEach(feature -> builder.getGenerationSettings().addFeature(step, feature));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModWorldgen.CONFIG_GATED_FEATURES.get();
    }
}
