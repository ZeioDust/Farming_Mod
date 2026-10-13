package com.cultivar.genetics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class PunnettReportTest {
    private static final AlleleTable TABLE = DefaultGenetics.table();

    @Test
    void printsMonohybridAndDihybridReports() {
        Genome base = Genome.homozygous(1, 1, 1, 1, 2, 0, 2, 1);
        Genome monohybrid = base.withGene(Gene.YIELD, 1, 3);
        Genome dihybrid = monohybrid.withGene(Gene.GROWTH_RATE, 1, 3);

        PunnettSimulator.Result mono = PunnettSimulator.simulate(monohybrid, monohybrid, TABLE, 10_000, new SplittableRandom(2026L),
                Inheritance.MutationSettings.NONE);
        System.out.println("=== /cultivar punnett " + monohybrid.toHex() + " " + monohybrid.toHex() + " 10000");
        mono.describe().forEach(System.out::println);

        PunnettSimulator.Result di = PunnettSimulator.simulate(dihybrid, dihybrid, TABLE, 10_000, new SplittableRandom(2027L),
                Inheritance.MutationSettings.NONE);
        System.out.println("=== /cultivar punnett " + dihybrid.toHex() + " " + dihybrid.toHex() + " 10000");
        di.describe().forEach(System.out::println);

        assertTrue(mono.allConsistentAt001());
        assertTrue(di.allConsistentAt001());
    }

    @Test
    void replicatedChiSquareRejectionRateIsNominal() {
        Genome monohybrid = Genome.homozygous(1, 1, 1, 1, 2, 0, 2, 1).withGene(Gene.YIELD, 1, 3);
        int replicates = 400;
        int rejected = 0;
        SplittableRandom seeds = new SplittableRandom(99L);
        for (int i = 0; i < replicates; i++) {
            PunnettSimulator.Result result = PunnettSimulator.simulate(monohybrid, monohybrid, TABLE, 10_000, new SplittableRandom(seeds.nextLong()),
                    Inheritance.MutationSettings.NONE);
            if (!result.genotypeTables().get(0).consistentAt05()) {
                rejected++;
            }
        }
        double rate = rejected / (double) replicates;
        System.out.printf("Replicated 1:2:1 test: %d of %d runs of 10000 crosses rejected at p=0.05 (rate %.3f, nominal 0.050)%n",
                rejected, replicates, rate);
        assertTrue(rate > 0.005 && rate < 0.095, "rejection rate " + rate + " is far from the nominal 5%");
    }
}
