package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Inheritance;
import com.cultivar.item.Planting;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.item.WildSeedItem;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Rng;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class InducedMutation {
    private InducedMutation() {
    }

    public enum Outcome {
        MUTATED, UNCHANGED, DESTROYED
    }

    public record Result(Outcome outcome, ItemStack stack) {
    }

    public static Result treat(ServerLevel level, ItemStack seed, double success, double destroy) {
        ItemStack single = seed.copyWithCount(1);
        CropSpecies species = single.getItem() instanceof SeedPacketItem packet ? packet.species() : WildSeedItem.speciesOf(single);
        Planting.assignNaturalGenome(single, species, level);
        RandomSource random = level.getRandom();
        double roll = random.nextDouble();
        double destroyChance = Math.min(1.0, destroy);
        double successChance = Math.min(1.0 - destroyChance, success);
        if (roll < destroyChance) {
            return new Result(Outcome.DESTROYED, ItemStack.EMPTY);
        }
        if (roll < destroyChance + successChance) {
            Genome genome = single.get(ModDataComponents.GENOME.get());
            if (genome != null) {
                Genome mutated = Inheritance.induce(genome, Catalog.get(level).table(), Rng.of(random),
                        CultivarConfig.MUTATION_ONLY_ALLELES_ENABLED.get());
                single.set(ModDataComponents.GENOME.get(), mutated);
                long parent = single.getOrDefault(ModDataComponents.LINEAGE.get(), 0L);
                long lineage = PedigreeData.get(level.getServer()).create(species.key(), parent, parent, PedigreeData.Kind.MUTANT,
                        level.getGameTime(), mutated.toHex());
                single.set(ModDataComponents.LINEAGE.get(), lineage);
                return new Result(Outcome.MUTATED, single);
            }
        }
        return new Result(Outcome.UNCHANGED, single);
    }

    public static boolean isTreatable(ItemStack stack) {
        return stack.getItem() instanceof SeedPacketItem || stack.getItem() instanceof WildSeedItem;
    }
}
