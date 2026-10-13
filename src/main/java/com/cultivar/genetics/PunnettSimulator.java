package com.cultivar.genetics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.random.RandomGenerator;

public final class PunnettSimulator {
    private static final double[] CHI2_CRIT_05 = {3.841, 5.991, 7.815, 9.488, 11.070, 12.592, 14.067, 15.507};
    private static final double[] CHI2_CRIT_001 = {10.828, 13.816, 16.266, 18.467, 20.515, 22.458, 24.322, 26.124};

    private PunnettSimulator() {
    }

    public record ClassTable(String label, Map<String, Integer> observed, Map<String, Double> expectedFraction, int samples) {
        public double chiSquare() {
            double chi = 0;
            for (Map.Entry<String, Double> e : expectedFraction.entrySet()) {
                double expected = e.getValue() * samples;
                if (expected <= 0) {
                    continue;
                }
                double diff = observed.getOrDefault(e.getKey(), 0) - expected;
                chi += diff * diff / expected;
            }
            return chi;
        }

        public int degreesOfFreedom() {
            return (int) expectedFraction.values().stream().filter(v -> v > 0).count() - 1;
        }

        public boolean consistentAt05() {
            int df = degreesOfFreedom();
            return df <= 0 || chiSquare() < CHI2_CRIT_05[Math.min(df, CHI2_CRIT_05.length) - 1];
        }

        public boolean consistentAt001() {
            int df = degreesOfFreedom();
            return df <= 0 || chiSquare() < CHI2_CRIT_001[Math.min(df, CHI2_CRIT_001.length) - 1];
        }

        public List<String> describe() {
            List<String> lines = new ArrayList<>();
            lines.add(label + String.format(Locale.ROOT, "  (chi2=%.2f, df=%d, %s)", chiSquare(), degreesOfFreedom(),
                    consistentAt05() ? "consistent with Mendel at p=0.05" : "DEVIATES at p=0.05"));
            for (Map.Entry<String, Double> e : expectedFraction.entrySet()) {
                int obs = observed.getOrDefault(e.getKey(), 0);
                lines.add(String.format(Locale.ROOT, "  %-40s observed %6d (%.4f)  expected %.4f", e.getKey(), obs,
                        obs / (double) samples, e.getValue()));
            }
            return lines;
        }
    }

    public record Result(int samples, List<ClassTable> genotypeTables, List<ClassTable> phenotypeTables,
                         ClassTable dihybridTable, int mutations) {
        public List<String> describe() {
            List<String> lines = new ArrayList<>();
            lines.add("Simulated " + samples + " crosses, " + mutations + " spontaneous mutations.");
            if (genotypeTables.isEmpty()) {
                lines.add("Both parents are homozygous and identical at every segregating locus: no variation expected.");
            }
            for (int i = 0; i < genotypeTables.size(); i++) {
                lines.addAll(genotypeTables.get(i).describe());
                lines.addAll(phenotypeTables.get(i).describe());
            }
            if (dihybridTable != null) {
                lines.addAll(dihybridTable.describe());
            }
            return lines;
        }

        public boolean allConsistentAt001() {
            for (ClassTable table : genotypeTables) {
                if (!table.consistentAt001()) {
                    return false;
                }
            }
            for (ClassTable table : phenotypeTables) {
                if (!table.consistentAt001()) {
                    return false;
                }
            }
            return dihybridTable == null || dihybridTable.consistentAt001();
        }
    }

    public static Result simulate(Genome mother, Genome father, AlleleTable table, int samples, RandomGenerator random,
                                  Inheritance.MutationSettings mutation) {
        List<Gene> segregating = new ArrayList<>();
        for (Gene gene : Gene.VALUES) {
            boolean variable = !mother.isHomozygous(gene) || !father.isHomozygous(gene) || mother.first(gene) != father.first(gene);
            if (variable && expectedGenotypes(mother, father, gene).size() > 1) {
                segregating.add(gene);
            }
        }

        Map<Gene, Map<String, Integer>> genotypeCounts = new LinkedHashMap<>();
        Map<Gene, Map<String, Integer>> phenotypeCounts = new LinkedHashMap<>();
        for (Gene gene : segregating) {
            genotypeCounts.put(gene, new TreeMap<>());
            phenotypeCounts.put(gene, new TreeMap<>());
        }
        Map<String, Integer> joint = new TreeMap<>();
        int mutations = 0;

        for (int i = 0; i < samples; i++) {
            Inheritance.Offspring offspring = Inheritance.cross(mother, father, table, random, mutation);
            mutations += offspring.mutations();
            Genome child = offspring.genome();
            for (Gene gene : segregating) {
                genotypeCounts.get(gene).merge(genotypeKey(child.first(gene), child.second(gene), gene, table), 1, Integer::sum);
                phenotypeCounts.get(gene).merge(Phenotype.phenotypeClass(child, gene, table), 1, Integer::sum);
            }
            if (segregating.size() >= 2) {
                Gene g1 = segregating.get(0);
                Gene g2 = segregating.get(1);
                joint.merge(Phenotype.phenotypeClass(child, g1, table) + " & " + Phenotype.phenotypeClass(child, g2, table), 1, Integer::sum);
            }
        }

        List<ClassTable> genotypeTables = new ArrayList<>();
        List<ClassTable> phenotypeTables = new ArrayList<>();
        for (Gene gene : segregating) {
            Map<String, Double> expectedGenotype = new TreeMap<>();
            Map<String, Double> expectedPhenotype = new TreeMap<>();
            for (Map.Entry<int[], Double> e : expectedGenotypes(mother, father, gene).entrySet()) {
                int[] pair = e.getKey();
                expectedGenotype.merge(genotypeKey(pair[0], pair[1], gene, table), e.getValue(), Double::sum);
                Genome probe = mother.withGene(gene, pair[0], pair[1]);
                expectedPhenotype.merge(Phenotype.phenotypeClass(probe, gene, table), e.getValue(), Double::sum);
            }
            genotypeTables.add(new ClassTable("Genotype " + gene.id(), genotypeCounts.get(gene), expectedGenotype, samples));
            phenotypeTables.add(new ClassTable("Phenotype " + gene.id(), phenotypeCounts.get(gene), expectedPhenotype, samples));
        }

        ClassTable dihybrid = null;
        if (segregating.size() >= 2) {
            Map<String, Double> p1 = phenotypeTables.get(0).expectedFraction();
            Map<String, Double> p2 = phenotypeTables.get(1).expectedFraction();
            Map<String, Double> expectedJoint = new TreeMap<>();
            for (Map.Entry<String, Double> a : p1.entrySet()) {
                for (Map.Entry<String, Double> b : p2.entrySet()) {
                    expectedJoint.put(a.getKey() + " & " + b.getKey(), a.getValue() * b.getValue());
                }
            }
            dihybrid = new ClassTable("Dihybrid phenotype " + segregating.get(0).id() + " x " + segregating.get(1).id(),
                    joint, expectedJoint, samples);
        }
        return new Result(samples, genotypeTables, phenotypeTables, dihybrid, mutations);
    }

    public static Map<int[], Double> expectedGenotypes(Genome mother, Genome father, Gene gene) {
        Map<String, Double> byKey = new LinkedHashMap<>();
        Map<String, int[]> pairs = new LinkedHashMap<>();
        for (int m = 0; m < 2; m++) {
            for (int f = 0; f < 2; f++) {
                int a = mother.allele(gene, m);
                int b = father.allele(gene, f);
                int lo = Math.min(a, b);
                int hi = Math.max(a, b);
                String key = lo + "/" + hi;
                byKey.merge(key, 0.25, Double::sum);
                pairs.putIfAbsent(key, new int[]{lo, hi});
            }
        }
        Map<int[], Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : byKey.entrySet()) {
            result.put(pairs.get(e.getKey()), e.getValue());
        }
        return result;
    }

    private static String genotypeKey(int a, int b, Gene gene, AlleleTable table) {
        int lo = Math.min(a, b);
        int hi = Math.max(a, b);
        return table.get(gene, lo).id() + "/" + table.get(gene, hi).id();
    }
}
