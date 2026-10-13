package com.cultivar.market;

import com.cultivar.CultivarConfig;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.GeneDefinition;
import com.cultivar.data.SpeciesDefinition;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public final class ContractGenerator {
    private ContractGenerator() {
    }

    public static int tier(int reputation) {
        return Math.min(3, reputation / 4);
    }

    public static Contract generate(Catalog catalog, RandomSource random, int tier) {
        CropSpecies crop = CropSpecies.VALUES[random.nextInt(CropSpecies.VALUES.length)];
        int roll = random.nextInt(100);
        int produceWeight = tier == 0 ? 55 : tier == 1 ? 40 : 30;
        int alleleWeight = tier == 0 ? 0 : tier == 1 ? 15 : 25;
        if (roll < produceWeight) {
            return produce(crop, random, tier);
        }
        if (roll < produceWeight + alleleWeight) {
            Contract allele = allele(catalog, crop, random, tier);
            if (allele != null) {
                return allele;
            }
        }
        return trait(catalog, crop, random, tier);
    }

    private static double rewardMultiplier() {
        return CultivarConfig.isLoaded() ? CultivarConfig.CONTRACT_REWARD_MULTIPLIER.get() : 1.0;
    }

    private static Contract produce(CropSpecies crop, RandomSource random, int tier) {
        int count = 8 + 8 * tier + random.nextInt(9);
        Grade grade = switch (tier) {
            case 0 -> random.nextBoolean() ? Grade.C : Grade.B;
            case 1 -> random.nextBoolean() ? Grade.B : Grade.A;
            case 2 -> Grade.A;
            default -> random.nextBoolean() ? Grade.A : Grade.PRIME;
        };
        float base = MarketValues.base(new ItemStack(ModItems.produce(crop)));
        if (base <= 0) {
            base = 0.05f;
        }
        int reward = (int) Math.max(1, Math.round(count * base * grade.priceMultiplier() * 2.0 * rewardMultiplier())) + tier;
        return Contract.of(Contract.Kind.PRODUCE, crop.key(), count, grade, Optional.empty(), 0f, Optional.empty(), false, reward, bonus(random, tier));
    }

    static List<Allele> naturalPool(Catalog catalog, CropSpecies crop, Gene gene) {
        SpeciesDefinition def = catalog.species(crop.key());
        int poolSize = CultivarConfig.isLoaded() ? CultivarConfig.ALLELES_PER_GENE_PER_SPECIES.get() : 3;
        AlleleTable table = catalog.table();
        List<Allele> pool = new ArrayList<>();
        for (String id : def.naturalAlleles().getOrDefault(gene, List.of())) {
            Allele a = table.byId(id);
            if (a != null && a.gene() == gene && !a.wild() && !a.mutationOnly()) {
                pool.add(a);
            }
            if (pool.size() >= poolSize) {
                break;
            }
        }
        return pool;
    }

    private static Contract trait(Catalog catalog, CropSpecies crop, RandomSource random, int tier) {
        AlleleTable table = catalog.table();
        Gene gene = Gene.VALUES[random.nextInt(Gene.COUNT)];
        GeneDefinition def = catalog.gene(gene);
        Comparator<Allele> best = Comparator.comparingDouble(a -> -def.favourability(table.effectiveValue(a)));
        List<Allele> pool = new ArrayList<>(naturalPool(catalog, crop, gene));
        if (tier >= 3) {
            for (Allele a : table.alleles(gene)) {
                if (!a.wild()) {
                    pool.add(a);
                }
            }
        }
        if (pool.isEmpty()) {
            pool.addAll(table.alleles(gene));
        }
        pool.sort(best);
        Allele target = tier == 0 && pool.size() > 1 ? pool.get(1) : pool.get(0);
        boolean pure = tier >= 2 && (tier == 2 || random.nextBoolean());
        int reward = (int) Math.max(1, Math.round((5 + tier * 5 + random.nextInt(4)) * rewardMultiplier()));
        return Contract.of(Contract.Kind.TRAIT, crop.key(), 1, Grade.C, Optional.of(gene), table.effectiveValue(target), Optional.empty(), pure,
                reward, bonus(random, tier));
    }

    private static Contract allele(Catalog catalog, CropSpecies crop, RandomSource random, int tier) {
        AlleleTable table = catalog.table();
        List<Allele> rare = new ArrayList<>();
        for (Allele a : table.all()) {
            if (a.mutationOnly() || (a.wild() && tier >= 2)) {
                rare.add(a);
            }
        }
        if (rare.isEmpty()) {
            return null;
        }
        Allele target = rare.get(random.nextInt(rare.size()));
        int reward = (int) Math.max(1, Math.round((10 + tier * 5 + random.nextInt(5)) * rewardMultiplier()));
        return Contract.of(Contract.Kind.ALLELE, crop.key(), 1, Grade.C, Optional.of(target.gene()), 0f, Optional.of(target.id()), false,
                reward, bonus(random, Math.max(1, tier)));
    }

    private static Optional<ItemStackTemplate> bonus(RandomSource random, int tier) {
        if (tier == 0 && random.nextInt(3) != 0) {
            return Optional.empty();
        }
        Item[] pool = tier >= 2
                ? new Item[]{ModItems.CANOPY_RESIN.get(), ModItems.MUTAGEN_VIAL.get(), ModItems.EDIT_REAGENT.get(), ModItems.SOY_OIL.get()}
                : new Item[]{Items.PAPER, ModItems.MUTAGEN_VIAL.get(), ModItems.SOY_OIL.get(), ModItems.COMPOST.get()};
        Item item = pool[random.nextInt(pool.length)];
        int count = item == Items.PAPER || item == ModItems.COMPOST.get() ? 8 : 1 + (tier >= 3 ? 1 : 0);
        return Optional.of(new ItemStackTemplate(item, count));
    }
}
