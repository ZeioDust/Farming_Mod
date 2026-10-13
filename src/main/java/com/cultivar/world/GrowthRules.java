package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.data.Nutrient;
import com.cultivar.data.PhBand;
import com.cultivar.data.SpeciesDefinition;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Phenotype;

public final class GrowthRules {
    public static final float COLD_LETHAL_MARGIN = 0.3f;
    public static final float HEAT_LETHAL_MARGIN = 0.4f;
    public static final float DROUGHT_LETHAL_MARGIN = 0.25f;
    public static final int STRESS_TO_DIE = 6;

    private GrowthRules() {
    }

    public enum Limit {
        NONE, COLD, HEAT, DROUGHT, DARK, PH, NUTRIENTS, DEPLETED
    }

    public record Verdict(boolean canGrow, float speed, boolean lethal, Limit limit) {
    }

    public static Verdict evaluate(Phenotype phenotype, SpeciesDefinition species, PhBand band, Environment env, boolean geneticsOn) {
        float minTemp = geneticsOn ? phenotype.get(Gene.COLD_TOLERANCE) : 0.35f;
        float minMoisture = geneticsOn ? phenotype.get(Gene.DROUGHT_TOLERANCE) : 0.4f;
        float minLight = geneticsOn ? phenotype.get(Gene.LIGHT_NEED) : 9f;
        float maxTemp = species.maxTemperature();

        boolean lethal = false;
        Limit limit = Limit.NONE;
        boolean canGrow = true;

        if (env.temperature() < minTemp) {
            canGrow = false;
            limit = Limit.COLD;
            lethal |= env.temperature() < minTemp - COLD_LETHAL_MARGIN;
        } else if (env.temperature() > maxTemp) {
            canGrow = false;
            limit = Limit.HEAT;
            lethal |= env.temperature() > maxTemp + HEAT_LETHAL_MARGIN;
        }
        if (env.moisture() < minMoisture) {
            if (canGrow) {
                limit = Limit.DROUGHT;
            }
            canGrow = false;
            lethal |= env.moisture() < minMoisture - DROUGHT_LETHAL_MARGIN;
        }
        if (env.light() < Math.round(minLight)) {
            if (canGrow) {
                limit = Limit.DARK;
            }
            canGrow = false;
        }

        float speed = 1f;
        if (CultivarConfig.SOIL_CHEMISTRY_ENABLED.get()) {
            if (env.depleted()) {
                if (canGrow) {
                    limit = Limit.DEPLETED;
                }
                canGrow = false;
            } else if (env.soil() != null) {
                float floor = CultivarConfig.NUTRIENT_FLOOR.get();
                for (Nutrient nutrient : Nutrient.values()) {
                    if (env.nutrient(nutrient) < floor) {
                        if (canGrow) {
                            limit = Limit.NUTRIENTS;
                        }
                        canGrow = false;
                        break;
                    }
                }
                float widthFactor = CultivarConfig.PH_BAND_WIDTH.get().floatValue();
                float outside = band.distanceOutside(env.ph(), widthFactor);
                if (outside > 0) {
                    speed = 1f / (1f + 2f * outside);
                    if (limit == Limit.NONE) {
                        limit = Limit.PH;
                    }
                    if (band.isLethal(env.ph(), widthFactor)) {
                        lethal = true;
                    }
                }
            }
        }
        return new Verdict(canGrow, speed, lethal, limit);
    }
}
