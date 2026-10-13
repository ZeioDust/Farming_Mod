package com.cultivar.genetics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class AlleleTable {
    private final Allele[][] byCode = new Allele[Gene.COUNT][256];
    private final Map<Gene, List<Allele>> byGene = new EnumMap<>(Gene.class);
    private final Map<String, Allele> byId = new HashMap<>();
    private final float wildStrength;

    public AlleleTable(Collection<Allele> alleles, float wildStrength) {
        this.wildStrength = wildStrength;
        Map<Gene, List<Allele>> building = new EnumMap<>(Gene.class);
        for (Gene gene : Gene.VALUES) {
            building.put(gene, new ArrayList<>());
        }
        for (Allele allele : alleles) {
            Allele[] slots = byCode[allele.gene().ordinal()];
            if (slots[allele.code()] != null) {
                throw new IllegalArgumentException("Duplicate allele code " + allele.code() + " for gene " + allele.gene()
                        + ": " + slots[allele.code()].id() + " and " + allele.id());
            }
            slots[allele.code()] = allele;
            building.get(allele.gene()).add(allele);
            byId.put(allele.id(), allele);
        }
        for (Gene gene : Gene.VALUES) {
            List<Allele> list = building.get(gene);
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Gene " + gene + " has no alleles");
            }
            list.sort((a, b) -> Integer.compare(a.code(), b.code()));
            byGene.put(gene, Collections.unmodifiableList(list));
        }
    }

    public AlleleTable withWildStrength(float strength) {
        List<Allele> all = new ArrayList<>();
        byGene.values().forEach(all::addAll);
        return new AlleleTable(all, strength);
    }

    public Allele get(Gene gene, int code) {
        Allele allele = byCode[gene.ordinal()][code & 0xFF];
        return allele != null ? allele : byGene.get(gene).get(0);
    }

    public boolean has(Gene gene, int code) {
        return byCode[gene.ordinal()][code & 0xFF] != null;
    }

    public Allele byId(String id) {
        return byId.get(id);
    }

    public List<Allele> alleles(Gene gene) {
        return byGene.get(gene);
    }

    public Collection<Allele> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public float wildStrength() {
        return wildStrength;
    }

    public float effectiveValue(Allele allele) {
        if (!allele.wild() || wildStrength == 1.0f) {
            return allele.value();
        }
        Gene gene = allele.gene();
        if (gene == Gene.YIELD || gene == Gene.GROWTH_RATE || gene == Gene.MUTATION_RATE) {
            return allele.value();
        }
        float mid = (gene.min() + gene.max()) * 0.5f;
        float offset = allele.value() - mid;
        boolean favourable = gene.higherIsBetter() ? offset > 0 : offset < 0;
        float scaled = favourable ? mid + offset * wildStrength : mid + offset / wildStrength;
        return gene.clamp(scaled);
    }

    public Allele randomMutationAllele(Gene gene, RandomGenerator random, boolean allowMutationOnly) {
        int total = 0;
        for (Allele allele : byGene.get(gene)) {
            if (eligibleForMutation(allele, allowMutationOnly)) {
                total += allele.mutationWeight();
            }
        }
        if (total <= 0) {
            return byGene.get(gene).get(0);
        }
        int roll = random.nextInt(total);
        for (Allele allele : byGene.get(gene)) {
            if (eligibleForMutation(allele, allowMutationOnly)) {
                roll -= allele.mutationWeight();
                if (roll < 0) {
                    return allele;
                }
            }
        }
        return byGene.get(gene).get(0);
    }

    private static boolean eligibleForMutation(Allele allele, boolean allowMutationOnly) {
        if (allele.mutationWeight() <= 0) {
            return false;
        }
        return allowMutationOnly || !allele.mutationOnly();
    }
}
