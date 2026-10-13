package com.cultivar.genetics;

public record Allele(Gene gene, int code, String id, float value, int dominance, boolean mutationOnly, boolean wild,
                     int mutationWeight) {
    public Allele {
        if (code < 0 || code > 255) {
            throw new IllegalArgumentException("Allele code out of byte range: " + code);
        }
    }

    public String translationKey() {
        return "allele.cultivar." + id;
    }
}
