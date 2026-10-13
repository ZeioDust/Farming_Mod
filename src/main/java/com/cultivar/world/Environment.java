package com.cultivar.world;

import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.block.DepletedSoilBlock;
import com.cultivar.data.Nutrient;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public record Environment(float temperature, float moisture, int light, ManagedSoilBlockEntity soil, boolean depleted,
                          ClimateControllerBlockEntity greenhouse) {
    public float ph() {
        return soil != null ? soil.ph() : 6.5f;
    }

    public float nutrient(Nutrient nutrient) {
        return soil != null ? soil.get(nutrient) : 50f;
    }

    public static Environment sample(ServerLevel level, BlockPos pos) {
        return sample(level, pos, true);
    }

    public static Environment sample(ServerLevel level, BlockPos pos, boolean useGreenhouse) {
        BlockPos below = pos.below();
        BlockState soilState = level.getBlockState(below);
        ManagedSoilBlockEntity soil = level.getBlockEntity(below) instanceof ManagedSoilBlockEntity s ? s : null;
        boolean depleted = soilState.getBlock() instanceof DepletedSoilBlock;

        float soilMoisture;
        if (soilState.hasProperty(FarmlandBlock.MOISTURE)) {
            soilMoisture = soilState.getValue(FarmlandBlock.MOISTURE) / 7f;
        } else if (depleted) {
            soilMoisture = 0.1f;
        } else {
            soilMoisture = 0.45f;
        }

        Biome biome = level.getBiome(pos).value();
        float temperature = naturalTemperature(level, pos, biome);
        float downfall = biome.getModifiedClimateSettings().downfall();
        float moisture = 0.75f * soilMoisture + 0.25f * Math.min(1f, downfall);
        if (level.isRainingAt(pos.above())) {
            moisture += 0.2f;
        }
        int light = level.getRawBrightness(pos, 0);

        ClimateControllerBlockEntity greenhouse = null;
        if (useGreenhouse) {
            greenhouse = ClimateIndex.controllerFor(level, pos);
            if (greenhouse != null) {
                temperature = greenhouse.temperature();
                moisture = 0.5f * soilMoisture + 0.5f * greenhouse.humidity();
                light = Math.max(light, greenhouse.lightFloor());
            }
        }
        return new Environment(temperature, Math.min(1f, Math.max(0f, moisture)), light, soil, depleted, greenhouse);
    }

    public static float naturalTemperature(ServerLevel level, BlockPos pos, Biome biome) {
        float base = biome.getModifiedClimateSettings().temperature();
        int snowLine = level.getSeaLevel() + 17;
        if (pos.getY() > snowLine) {
            base -= (pos.getY() - snowLine) * 0.05f / 40f;
        }
        return base;
    }
}
