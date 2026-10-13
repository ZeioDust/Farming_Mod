package com.cultivar.genetics;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;

public final class Genome {
    public static final int BYTES = Gene.COUNT * 2;
    private static final HexFormat HEX = HexFormat.of();

    private final byte[] codes;
    private final int hash;

    private Genome(byte[] normalisedCodes) {
        this.codes = normalisedCodes;
        this.hash = Arrays.hashCode(normalisedCodes);
    }

    public static Genome of(byte[] codes) {
        if (codes.length != BYTES) {
            throw new IllegalArgumentException("Genome must be " + BYTES + " bytes, got " + codes.length);
        }
        byte[] copy = codes.clone();
        for (int i = 0; i < BYTES; i += 2) {
            int a = copy[i] & 0xFF;
            int b = copy[i + 1] & 0xFF;
            if (a > b) {
                copy[i] = (byte) b;
                copy[i + 1] = (byte) a;
            }
        }
        return new Genome(copy);
    }

    public static Genome homozygous(int... perGene) {
        if (perGene.length != Gene.COUNT) {
            throw new IllegalArgumentException("Need one code per gene");
        }
        byte[] codes = new byte[BYTES];
        for (int g = 0; g < Gene.COUNT; g++) {
            codes[g * 2] = (byte) perGene[g];
            codes[g * 2 + 1] = (byte) perGene[g];
        }
        return of(codes);
    }

    public int allele(Gene gene, int which) {
        return codes[gene.ordinal() * 2 + (which & 1)] & 0xFF;
    }

    public int first(Gene gene) {
        return allele(gene, 0);
    }

    public int second(Gene gene) {
        return allele(gene, 1);
    }

    public boolean isHomozygous(Gene gene) {
        return first(gene) == second(gene);
    }

    public boolean isTrueBreeding() {
        for (Gene gene : Gene.VALUES) {
            if (!isHomozygous(gene)) {
                return false;
            }
        }
        return true;
    }

    public boolean carries(Gene gene, int code) {
        return first(gene) == code || second(gene) == code;
    }

    public Genome withGene(Gene gene, int a, int b) {
        byte[] copy = codes.clone();
        copy[gene.ordinal() * 2] = (byte) a;
        copy[gene.ordinal() * 2 + 1] = (byte) b;
        return of(copy);
    }

    public byte[] toBytes() {
        return codes.clone();
    }

    public String toHex() {
        return HEX.formatHex(codes);
    }

    public static Genome fromHex(String hex) {
        String clean = hex.trim().toLowerCase(Locale.ROOT);
        if (clean.length() != BYTES * 2) {
            throw new IllegalArgumentException("Genome hex must be " + (BYTES * 2) + " characters");
        }
        return of(HEX.parseHex(clean));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Genome genome && hash == genome.hash && Arrays.equals(codes, genome.codes);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Genome[" + toHex() + "]";
    }
}
