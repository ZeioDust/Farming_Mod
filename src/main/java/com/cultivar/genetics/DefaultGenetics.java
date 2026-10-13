package com.cultivar.genetics;

import java.util.ArrayList;
import java.util.List;

public final class DefaultGenetics {
    public static final int WEIGHT_COMMON = 10;
    public static final int WEIGHT_RARE = 2;
    public static final int WEIGHT_NONE = 0;

    private static final List<Allele> ALLELES = new ArrayList<>();

    static {
        natural(Gene.YIELD, 0, "yield_sparse", 0.6f, 2);
        natural(Gene.YIELD, 1, "yield_standard", 1.0f, 2);
        natural(Gene.YIELD, 2, "yield_ample", 1.3f, 1);
        natural(Gene.YIELD, 3, "yield_heavy", 1.6f, 0);
        wild(Gene.YIELD, 4, "yield_wild_meagre", 0.25f, 3);
        mutant(Gene.YIELD, 5, "yield_bumper", 2.1f, 0);
        mutant(Gene.YIELD, 6, "yield_clustered", 1.8f, 1);

        natural(Gene.GROWTH_RATE, 0, "growth_slow", 0.7f, 1);
        natural(Gene.GROWTH_RATE, 1, "growth_steady", 1.0f, 2);
        natural(Gene.GROWTH_RATE, 2, "growth_brisk", 1.25f, 1);
        natural(Gene.GROWTH_RATE, 3, "growth_vigorous", 1.5f, 0);
        wild(Gene.GROWTH_RATE, 4, "growth_wild_dormant", 0.45f, 3);
        mutant(Gene.GROWTH_RATE, 5, "growth_surge", 1.85f, 0);

        natural(Gene.COLD_TOLERANCE, 0, "cold_tender", 0.55f, 2);
        natural(Gene.COLD_TOLERANCE, 1, "cold_temperate", 0.35f, 2);
        natural(Gene.COLD_TOLERANCE, 2, "cold_hardy", 0.15f, 1);
        natural(Gene.COLD_TOLERANCE, 3, "cold_frostproof", -0.05f, 0);
        wild(Gene.COLD_TOLERANCE, 4, "cold_wild_boreal", -0.45f, 2);
        mutant(Gene.COLD_TOLERANCE, 5, "cold_glacial", -0.3f, 0);

        natural(Gene.DROUGHT_TOLERANCE, 0, "drought_thirsty", 0.6f, 1);
        natural(Gene.DROUGHT_TOLERANCE, 1, "drought_moderate", 0.4f, 2);
        natural(Gene.DROUGHT_TOLERANCE, 2, "drought_thrifty", 0.25f, 1);
        natural(Gene.DROUGHT_TOLERANCE, 3, "drought_xeric", 0.1f, 0);
        wild(Gene.DROUGHT_TOLERANCE, 4, "drought_wild_parched", 0.0f, 2);
        mutant(Gene.DROUGHT_TOLERANCE, 5, "drought_succulent", 0.05f, 0);

        natural(Gene.LIGHT_NEED, 0, "light_sun_hungry", 12f, 1);
        natural(Gene.LIGHT_NEED, 1, "light_bright", 10f, 2);
        natural(Gene.LIGHT_NEED, 2, "light_average", 8f, 2);
        natural(Gene.LIGHT_NEED, 3, "light_shade_tolerant", 6f, 1);
        wild(Gene.LIGHT_NEED, 4, "light_wild_understory", 3f, 2);
        mutant(Gene.LIGHT_NEED, 5, "light_low_light", 4f, 0);

        natural(Gene.BLIGHT_RESISTANCE, 0, "blight_susceptible", 0.05f, 2);
        natural(Gene.BLIGHT_RESISTANCE, 1, "blight_tolerant", 0.25f, 1);
        natural(Gene.BLIGHT_RESISTANCE, 2, "blight_partial", 0.45f, 1);
        natural(Gene.BLIGHT_RESISTANCE, 3, "blight_resistant", 0.65f, 0);
        wild(Gene.BLIGHT_RESISTANCE, 4, "blight_wild_canopy", 0.9f, 2);
        mutant(Gene.BLIGHT_RESISTANCE, 5, "blight_sentinel", 0.8f, 0);

        natural(Gene.NUTRIENT_DRAW, 0, "nutrient_greedy", 1.6f, 1);
        natural(Gene.NUTRIENT_DRAW, 1, "nutrient_hungry", 1.25f, 2);
        natural(Gene.NUTRIENT_DRAW, 2, "nutrient_balanced", 1.0f, 2);
        natural(Gene.NUTRIENT_DRAW, 3, "nutrient_frugal", 0.7f, 1);
        wild(Gene.NUTRIENT_DRAW, 4, "nutrient_wild_austere", 0.4f, 2);
        mutant(Gene.NUTRIENT_DRAW, 5, "nutrient_efficient", 0.5f, 0);

        natural(Gene.MUTATION_RATE, 0, "mutation_stable", 0.001f, 2);
        natural(Gene.MUTATION_RATE, 1, "mutation_standard", 0.005f, 2);
        natural(Gene.MUTATION_RATE, 2, "mutation_labile", 0.015f, 1);
        natural(Gene.MUTATION_RATE, 3, "mutation_volatile", 0.04f, 0);
        wild(Gene.MUTATION_RATE, 4, "mutation_wild_drift", 0.02f, 2);
        mutant(Gene.MUTATION_RATE, 5, "mutation_hypermutable", 0.08f, 0);
    }

    private DefaultGenetics() {
    }

    private static void natural(Gene gene, int code, String id, float value, int dominance) {
        ALLELES.add(new Allele(gene, code, id, value, dominance, false, false, WEIGHT_COMMON));
    }

    private static void wild(Gene gene, int code, String id, float value, int dominance) {
        ALLELES.add(new Allele(gene, code, id, value, dominance, false, true, WEIGHT_NONE));
    }

    private static void mutant(Gene gene, int code, String id, float value, int dominance) {
        ALLELES.add(new Allele(gene, code, id, value, dominance, true, false, WEIGHT_RARE));
    }

    public static List<Allele> alleles() {
        return List.copyOf(ALLELES);
    }

    public static AlleleTable table() {
        return new AlleleTable(ALLELES, 1.0f);
    }

    public static int wildCode(Gene gene) {
        for (Allele allele : ALLELES) {
            if (allele.gene() == gene && allele.wild()) {
                return allele.code();
            }
        }
        throw new IllegalStateException("No wild allele for " + gene);
    }
}
