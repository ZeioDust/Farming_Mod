package com.cultivar.market;

import com.cultivar.CultivarConfig;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Phenotype;
import com.cultivar.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public final class GradeLogic {
    public static final float GENETIC_WEIGHT = 0.55f;
    private static final float[] WEIGHTS = {1.6f, 1.0f, 0.7f, 0.7f, 0.7f, 1.0f, 0.8f, 0.5f};

    private GradeLogic() {
    }

    public static float geneticScore(Phenotype phenotype, Catalog catalog) {
        float total = 0;
        float weights = 0;
        for (Gene gene : Gene.VALUES) {
            float w = WEIGHTS[gene.ordinal()];
            total += w * catalog.gene(gene).favourability(phenotype.get(gene));
            weights += w;
        }
        return total / weights;
    }

    public static float growingScore(float efficiency, int stress) {
        return Math.max(0f, Math.min(1f, efficiency - 0.08f * stress));
    }

    public static float score(float genetic, float growing) {
        return GENETIC_WEIGHT * genetic + (1 - GENETIC_WEIGHT) * growing;
    }

    public static Grade of(float score) {
        if (score >= threshold(CultivarConfig.GRADE_THRESHOLD_PRIME, 0.74)) {
            return Grade.PRIME;
        }
        if (score >= threshold(CultivarConfig.GRADE_THRESHOLD_A, 0.58)) {
            return Grade.A;
        }
        if (score >= threshold(CultivarConfig.GRADE_THRESHOLD_B, 0.42)) {
            return Grade.B;
        }
        return Grade.C;
    }

    private static float threshold(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
        return (float) (CultivarConfig.isLoaded() ? value.get() : fallback);
    }

    public static Grade ceiling(Phenotype phenotype, Catalog catalog) {
        return of(score(geneticScore(phenotype, catalog), 1f));
    }

    public static ItemStack apply(ItemStack produce, Grade grade) {
        produce.set(ModDataComponents.GRADE.get(), grade);
        FoodProperties food = produce.get(DataComponents.FOOD);
        if (food != null && grade != Grade.C) {
            produce.set(DataComponents.FOOD, new FoodProperties(Math.max(1, Math.round(food.nutrition() * grade.foodMultiplier())),
                    food.saturation() * grade.foodMultiplier(), food.canAlwaysEat()));
        }
        return produce;
    }

    public static Grade of(ItemStack stack) {
        Grade grade = stack.get(ModDataComponents.GRADE.get());
        return grade != null ? grade : Grade.C;
    }
}
