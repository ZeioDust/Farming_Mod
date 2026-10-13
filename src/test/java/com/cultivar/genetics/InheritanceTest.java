package com.cultivar.genetics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class InheritanceTest {
    private static final AlleleTable TABLE = DefaultGenetics.table();
    private static final int SAMPLES = 200_000;
    private static final double TOLERANCE = 0.01;

    private static final int YIELD_STANDARD = 1;
    private static final int YIELD_HEAVY = 3;
    private static final int YIELD_SPARSE = 0;
    private static final int GROWTH_STEADY = 1;
    private static final int GROWTH_VIGOROUS = 3;

    private static Genome base() {
        return Genome.homozygous(YIELD_STANDARD, GROWTH_STEADY, 1, 1, 2, 0, 2, 1);
    }

    private static Genome monohybrid() {
        return base().withGene(Gene.YIELD, YIELD_STANDARD, YIELD_HEAVY);
    }

    private static Genome dihybrid() {
        return monohybrid().withGene(Gene.GROWTH_RATE, GROWTH_STEADY, GROWTH_VIGOROUS);
    }

    @Test
    void exactEnumerationGivesOneTwoOne() {
        Map<int[], Double> expected = PunnettSimulator.expectedGenotypes(monohybrid(), monohybrid(), Gene.YIELD);
        Map<String, Double> byKey = new HashMap<>();
        expected.forEach((pair, p) -> byKey.put(pair[0] + "/" + pair[1], p));
        assertEquals(0.25, byKey.get(YIELD_STANDARD + "/" + YIELD_STANDARD), 1e-12);
        assertEquals(0.50, byKey.get(YIELD_STANDARD + "/" + YIELD_HEAVY), 1e-12);
        assertEquals(0.25, byKey.get(YIELD_HEAVY + "/" + YIELD_HEAVY), 1e-12);
    }

    @Test
    void singleGeneCrossGivesMendelianRatios() {
        SplittableRandom random = new SplittableRandom(20261013L);
        int homDominant = 0;
        int het = 0;
        int homRecessive = 0;
        int dominantPhenotype = 0;
        for (int i = 0; i < SAMPLES; i++) {
            Genome child = Inheritance.cross(monohybrid(), monohybrid(), TABLE, random, Inheritance.MutationSettings.NONE).genome();
            int a = child.first(Gene.YIELD);
            int b = child.second(Gene.YIELD);
            if (a == YIELD_STANDARD && b == YIELD_STANDARD) {
                homDominant++;
            } else if (a == YIELD_HEAVY && b == YIELD_HEAVY) {
                homRecessive++;
            } else {
                het++;
            }
            if (Phenotype.resolve(child, TABLE).get(Gene.YIELD) == 1.0f) {
                dominantPhenotype++;
            }
            for (Gene gene : Gene.VALUES) {
                if (gene != Gene.YIELD) {
                    assertEquals(base().first(gene), child.first(gene));
                    assertEquals(base().second(gene), child.second(gene));
                }
            }
        }
        assertEquals(0.25, homDominant / (double) SAMPLES, TOLERANCE, "AA frequency");
        assertEquals(0.50, het / (double) SAMPLES, TOLERANCE, "Aa frequency");
        assertEquals(0.25, homRecessive / (double) SAMPLES, TOLERANCE, "aa frequency");
        assertEquals(0.75, dominantPhenotype / (double) SAMPLES, TOLERANCE, "dominant phenotype frequency");

        PunnettSimulator.Result result = PunnettSimulator.simulate(monohybrid(), monohybrid(), TABLE, SAMPLES,
                new SplittableRandom(7L), Inheritance.MutationSettings.NONE);
        assertEquals(1, result.genotypeTables().size());
        assertTrue(result.allConsistentAt001(), () -> String.join("\n", result.describe()));
    }

    @Test
    void twoGeneCrossGivesNineThreeThreeOne() {
        SplittableRandom random = new SplittableRandom(99L);
        int[] classes = new int[4];
        for (int i = 0; i < SAMPLES; i++) {
            Genome child = Inheritance.cross(dihybrid(), dihybrid(), TABLE, random, Inheritance.MutationSettings.NONE).genome();
            Phenotype phenotype = Phenotype.resolve(child, TABLE);
            boolean yieldDominant = phenotype.get(Gene.YIELD) == 1.0f;
            boolean growthDominant = phenotype.get(Gene.GROWTH_RATE) == 1.0f;
            classes[(yieldDominant ? 0 : 2) + (growthDominant ? 0 : 1)]++;
        }
        assertEquals(9 / 16.0, classes[0] / (double) SAMPLES, TOLERANCE, "A_B_");
        assertEquals(3 / 16.0, classes[1] / (double) SAMPLES, TOLERANCE, "A_bb");
        assertEquals(3 / 16.0, classes[2] / (double) SAMPLES, TOLERANCE, "aaB_");
        assertEquals(1 / 16.0, classes[3] / (double) SAMPLES, TOLERANCE, "aabb");

        PunnettSimulator.Result result = PunnettSimulator.simulate(dihybrid(), dihybrid(), TABLE, SAMPLES,
                new SplittableRandom(11L), Inheritance.MutationSettings.NONE);
        assertEquals(2, result.genotypeTables().size());
        assertEquals(4, result.dihybridTable().expectedFraction().size());
        assertTrue(result.allConsistentAt001(), () -> String.join("\n", result.describe()));
    }

    @Test
    void codominanceExpressesTheAverage() {
        Genome het = base().withGene(Gene.YIELD, YIELD_SPARSE, YIELD_STANDARD);
        Phenotype phenotype = Phenotype.resolve(het, TABLE);
        assertEquals((0.6f + 1.0f) / 2f, phenotype.get(Gene.YIELD), 1e-6);
        assertEquals(Phenotype.EXPRESSED_BOTH, phenotype.expression(Gene.YIELD));

        Phenotype hidden = Phenotype.resolve(monohybrid(), TABLE);
        assertEquals(1.0f, hidden.get(Gene.YIELD), 1e-6);
        assertTrue(hidden.isExpressed(Gene.YIELD, 0));
        assertFalse(hidden.isExpressed(Gene.YIELD, 1));
    }

    @Test
    void selfPollinationWithoutMutationIsClonal() {
        SplittableRandom random = new SplittableRandom(3L);
        Genome parent = dihybrid();
        for (int i = 0; i < 1000; i++) {
            assertEquals(parent, Inheritance.self(parent, TABLE, random, Inheritance.MutationSettings.NONE).genome());
        }
    }

    @Test
    void mutationDrawsOnlyEligibleAlleles() {
        SplittableRandom random = new SplittableRandom(5L);
        for (int i = 0; i < 5000; i++) {
            Allele allele = TABLE.randomMutationAllele(Gene.BLIGHT_RESISTANCE, random, false);
            assertFalse(allele.wild(), "wild alleles never come from mutation");
            assertFalse(allele.mutationOnly(), "mutation-only alleles excluded when disabled");
        }
        boolean sawMutationOnly = false;
        for (int i = 0; i < 5000; i++) {
            Allele allele = TABLE.randomMutationAllele(Gene.BLIGHT_RESISTANCE, random, true);
            assertFalse(allele.wild());
            sawMutationOnly |= allele.mutationOnly();
        }
        assertTrue(sawMutationOnly);

        Genome induced = Inheritance.induce(base(), TABLE, random, true);
        assertNotEquals(base(), induced, "induced mutation must change exactly one allele");
    }

    @Test
    void genomesNormaliseAndRoundTrip() {
        Genome a = base().withGene(Gene.YIELD, YIELD_HEAVY, YIELD_STANDARD);
        Genome b = base().withGene(Gene.YIELD, YIELD_STANDARD, YIELD_HEAVY);
        assertEquals(a, b);
        assertEquals(a, Genome.fromHex(a.toHex()));
        assertTrue(base().isTrueBreeding());
        assertFalse(a.isTrueBreeding());
    }

    @Test
    void similarityMeasuresSharedAlleles() {
        assertEquals(1f, Inheritance.similarity(base(), base()));
        assertEquals(15f / 16f, Inheritance.similarity(base(), monohybrid()), 1e-6);
        Genome other = Genome.homozygous(0, 0, 0, 0, 0, 1, 0, 0);
        assertTrue(Inheritance.similarity(base(), other) < 0.5f);
    }
}
