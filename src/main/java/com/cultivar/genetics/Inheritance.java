package com.cultivar.genetics;

import java.util.random.RandomGenerator;

public final class Inheritance {
    private Inheritance() {
    }

    public record MutationSettings(boolean enabled, float multiplier, boolean allowMutationOnly) {
        public static final MutationSettings NONE = new MutationSettings(false, 0f, false);
    }

    public record Offspring(Genome genome, int mutations) {
    }

    public static Offspring cross(Genome mother, Genome father, AlleleTable table, RandomGenerator random,
                                  MutationSettings mutation) {
        float motherRate = mutationRate(mother, table, mutation);
        float fatherRate = mutationRate(father, table, mutation);
        byte[] child = new byte[Genome.BYTES];
        int mutations = 0;
        for (Gene gene : Gene.VALUES) {
            int fromMother = mother.allele(gene, random.nextInt(2));
            int fromFather = father.allele(gene, random.nextInt(2));
            if (motherRate > 0 && random.nextFloat() < motherRate) {
                fromMother = table.randomMutationAllele(gene, random, mutation.allowMutationOnly()).code();
                mutations++;
            }
            if (fatherRate > 0 && random.nextFloat() < fatherRate) {
                fromFather = table.randomMutationAllele(gene, random, mutation.allowMutationOnly()).code();
                mutations++;
            }
            child[gene.ordinal() * 2] = (byte) fromMother;
            child[gene.ordinal() * 2 + 1] = (byte) fromFather;
        }
        return new Offspring(Genome.of(child), mutations);
    }

    public static Offspring self(Genome parent, AlleleTable table, RandomGenerator random, MutationSettings mutation) {
        float rate = mutationRate(parent, table, mutation);
        if (rate <= 0) {
            return new Offspring(parent, 0);
        }
        byte[] child = parent.toBytes();
        int mutations = 0;
        for (Gene gene : Gene.VALUES) {
            for (int slot = 0; slot < 2; slot++) {
                if (random.nextFloat() < rate) {
                    child[gene.ordinal() * 2 + slot] = (byte) table.randomMutationAllele(gene, random, mutation.allowMutationOnly()).code();
                    mutations++;
                }
            }
        }
        return new Offspring(Genome.of(child), mutations);
    }

    public static Genome induce(Genome genome, AlleleTable table, RandomGenerator random, boolean allowMutationOnly) {
        Gene gene = Gene.VALUES[random.nextInt(Gene.COUNT)];
        int slot = random.nextInt(2);
        int current = genome.allele(gene, slot);
        Allele replacement = null;
        for (int attempt = 0; attempt < 16; attempt++) {
            Allele candidate = table.randomMutationAllele(gene, random, allowMutationOnly);
            if (candidate.code() != current) {
                replacement = candidate;
                break;
            }
        }
        if (replacement == null) {
            for (Allele allele : table.alleles(gene)) {
                if (allele.code() != current && allele.mutationWeight() > 0 && (allowMutationOnly || !allele.mutationOnly())) {
                    replacement = allele;
                    break;
                }
            }
        }
        if (replacement == null) {
            return genome;
        }
        byte[] bytes = genome.toBytes();
        bytes[gene.ordinal() * 2 + slot] = (byte) replacement.code();
        return Genome.of(bytes);
    }

    private static float mutationRate(Genome genome, AlleleTable table, MutationSettings mutation) {
        if (!mutation.enabled() || mutation.multiplier() <= 0) {
            return 0f;
        }
        float rate = Phenotype.resolve(genome, table).get(Gene.MUTATION_RATE) * mutation.multiplier();
        return Math.min(rate, 0.5f);
    }

    public static float similarity(Genome a, Genome b) {
        if (a.equals(b)) {
            return 1f;
        }
        int shared = 0;
        for (Gene gene : Gene.VALUES) {
            int a1 = a.first(gene);
            int a2 = a.second(gene);
            int b1 = b.first(gene);
            int b2 = b.second(gene);
            if (a1 == b1) {
                shared++;
                if (a2 == b2) {
                    shared++;
                }
            } else if (a1 == b2) {
                shared++;
                if (a2 == b1) {
                    shared++;
                }
            } else if (a2 == b1 || a2 == b2) {
                shared++;
            }
        }
        return shared / (float) Genome.BYTES;
    }
}
