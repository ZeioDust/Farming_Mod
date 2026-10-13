package com.cultivar.genetics;

import java.util.Arrays;

public final class Phenotype {
    public static final int EXPRESSED_FIRST = 0;
    public static final int EXPRESSED_SECOND = 1;
    public static final int EXPRESSED_BOTH = 2;

    private final float[] values;
    private final int[] expression;

    private Phenotype(float[] values, int[] expression) {
        this.values = values;
        this.expression = expression;
    }

    public static Phenotype resolve(Genome genome, AlleleTable table) {
        float[] values = new float[Gene.COUNT];
        int[] expression = new int[Gene.COUNT];
        for (Gene gene : Gene.VALUES) {
            Allele a = table.get(gene, genome.first(gene));
            Allele b = table.get(gene, genome.second(gene));
            int i = gene.ordinal();
            if (a.dominance() > b.dominance()) {
                values[i] = table.effectiveValue(a);
                expression[i] = EXPRESSED_FIRST;
            } else if (b.dominance() > a.dominance()) {
                values[i] = table.effectiveValue(b);
                expression[i] = EXPRESSED_SECOND;
            } else {
                values[i] = (table.effectiveValue(a) + table.effectiveValue(b)) * 0.5f;
                expression[i] = a.code() == b.code() ? EXPRESSED_FIRST : EXPRESSED_BOTH;
            }
        }
        return new Phenotype(values, expression);
    }

    public static Phenotype graft(Phenotype root, Phenotype scion) {
        float[] values = new float[Gene.COUNT];
        int[] expression = new int[Gene.COUNT];
        for (Gene gene : Gene.VALUES) {
            Phenotype source = isRootstockGene(gene) ? root : scion;
            values[gene.ordinal()] = source.values[gene.ordinal()];
            expression[gene.ordinal()] = source.expression[gene.ordinal()];
        }
        return new Phenotype(values, expression);
    }

    public static boolean isRootstockGene(Gene gene) {
        return gene == Gene.COLD_TOLERANCE || gene == Gene.DROUGHT_TOLERANCE || gene == Gene.BLIGHT_RESISTANCE
                || gene == Gene.NUTRIENT_DRAW;
    }

    public static Phenotype neutral() {
        return new Phenotype(new float[]{1.0f, 1.0f, 0.35f, 0.4f, 9f, 0.05f, 1.0f, 0.005f}, new int[Gene.COUNT]);
    }

    public float get(Gene gene) {
        return values[gene.ordinal()];
    }

    public int expression(Gene gene) {
        return expression[gene.ordinal()];
    }

    public boolean isExpressed(Gene gene, int slot) {
        int e = expression[gene.ordinal()];
        return e == EXPRESSED_BOTH || e == slot;
    }

    public static String phenotypeClass(Genome genome, Gene gene, AlleleTable table) {
        Allele a = table.get(gene, genome.first(gene));
        Allele b = table.get(gene, genome.second(gene));
        if (a.dominance() > b.dominance()) {
            return a.id();
        }
        if (b.dominance() > a.dominance()) {
            return b.id();
        }
        return a.code() == b.code() ? a.id() : a.id() + "+" + b.id();
    }

    public float[] toArray() {
        return values.clone();
    }

    @Override
    public String toString() {
        return "Phenotype" + Arrays.toString(values);
    }
}
