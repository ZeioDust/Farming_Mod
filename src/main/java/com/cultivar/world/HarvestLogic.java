package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.SpeciesDefinition;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Inheritance;
import com.cultivar.genetics.Phenotype;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Rng;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class HarvestLogic {
    private HarvestLogic() {
    }

    public record Result(List<ItemStack> produce, List<ItemStack> seeds) {
        public static final Result EMPTY = new Result(List.of(), List.of());

        public List<ItemStack> all() {
            List<ItemStack> all = new ArrayList<>(produce);
            all.addAll(seeds);
            return all;
        }
    }

    public static Result harvest(ServerLevel level, CropBlockEntity crop, BlockState state, @Nullable ServerPlayer player) {
        crop.ensureGenome(level);
        CropSpecies species = crop.species();
        Genome genome = crop.genome();
        if (species == null || genome == null || crop.isBlighted()) {
            return Result.EMPTY;
        }
        int stage = CultivarCropBlock.stage(state);
        if (stage < CultivarCropBlock.MAX_STAGE) {
            if (stage <= 1 && !crop.isGrafted()) {
                return new Result(List.of(), List.of(seedStack(species, genome, crop.lineage(), crop.isWild())));
            }
            return Result.EMPTY;
        }

        Catalog catalog = Catalog.get(level);
        SpeciesDefinition def = catalog.species(species.key());
        Phenotype phenotype = crop.phenotype();
        RandomGenerator random = Rng.of(level.getRandom());
        boolean genetics = CultivarConfig.GENETICS_ENABLED.get();

        float expected = def.baseYield() * (genetics ? phenotype.get(Gene.YIELD) : 1f) * CultivarConfig.YIELD_MULTIPLIER.get().floatValue();
        int count = (int) Math.floor(expected);
        if (random.nextFloat() < expected - count) {
            count++;
        }
        List<ItemStack> produce = new ArrayList<>();
        float genetic = genetics ? GradeLogic.geneticScore(phenotype, catalog) : 0.5f;
        Grade grade = GradeLogic.of(GradeLogic.score(genetic, GradeLogic.growingScore(crop.growingEfficiency(), crop.stress())));
        if (count > 0) {
            produce.add(GradeLogic.apply(new ItemStack(ModItems.produce(species), count), grade));
        }

        List<ItemStack> seeds = new ArrayList<>();
        if (!crop.isGrafted()) {
            seeds.addAll(breedSeeds(level, crop, species, genome, def, catalog.table(), random, player));
        }

        if (player != null) {
            Guide.hint(player, Hint.HARVESTED);
            if (count > 0 && grade == Grade.PRIME) {
                Guide.hint(player, Hint.PRIME);
                CultivarTrigger.fire(player, CultivarTrigger.PRIME_HARVEST);
            }
            if (crop.isWild()) {
                Guide.hint(player, Hint.WILD);
            }
            if (crop.rescuedByGreenhouse()) {
                CultivarTrigger.fire(player, CultivarTrigger.OUT_OF_SEASON);
            }
            if (genetics && phenotype.get(Gene.BLIGHT_RESISTANCE) >= 0.75f) {
                CultivarTrigger.fire(player, CultivarTrigger.RESISTANT);
            }
        }
        return new Result(produce, seeds);
    }

    private static List<ItemStack> breedSeeds(ServerLevel level, CropBlockEntity crop, CropSpecies species, Genome genome,
                                              SpeciesDefinition def, AlleleTable table, RandomGenerator random,
                                              @Nullable ServerPlayer player) {
        Genome donor = crop.donor();
        if (donor == null && !CultivarConfig.SELF_POLLINATION_ENABLED.get()) {
            return List.of();
        }
        Inheritance.MutationSettings mutation = new Inheritance.MutationSettings(CultivarConfig.GENETICS_ENABLED.get(),
                CultivarConfig.MUTATION_RATE_MULTIPLIER.get().floatValue(), CultivarConfig.MUTATION_ONLY_ALLELES_ENABLED.get());
        Map<Genome, Integer> offspring = new LinkedHashMap<>();
        int mutations = 0;
        int seedCount = def.seedsPerHarvest();
        if (level.getBlockEntity(crop.getBlockPos().below()) instanceof com.cultivar.blockentity.BreedingPlotBlockEntity) {
            seedCount += com.cultivar.blockentity.BreedingPlotBlockEntity.EXTRA_SEEDS;
        }
        for (int i = 0; i < seedCount; i++) {
            Inheritance.Offspring child = donor != null
                    ? Inheritance.cross(genome, donor, table, random, mutation)
                    : Inheritance.self(genome, table, random, mutation);
            mutations += child.mutations();
            offspring.merge(child.genome(), 1, Integer::sum);
        }
        if (offspring.isEmpty()) {
            return List.of();
        }
        Genome first = offspring.keySet().iterator().next();
        PedigreeData.Kind kind = donor != null ? PedigreeData.Kind.CROSS : (mutations > 0 ? PedigreeData.Kind.MUTANT : PedigreeData.Kind.SELF);
        long father = donor != null ? crop.donorLineage() : crop.lineage();
        long batch = PedigreeData.get(level.getServer()).create(species.key(), crop.lineage(), father, kind, level.getGameTime(), first.toHex());

        boolean wildSeeds = crop.isWild() && donor == null;
        List<ItemStack> stacks = new ArrayList<>();
        for (Map.Entry<Genome, Integer> entry : offspring.entrySet()) {
            ItemStack stack = seedStack(species, entry.getKey(), batch, wildSeeds);
            stack.setCount(entry.getValue());
            stacks.add(stack);
        }

        if (player != null) {
            if (donor != null) {
                recordCross(level, player, crop, species, genome, donor, offspring);
                CultivarTrigger.fire(player, CultivarTrigger.FIRST_CROSS);
            }
            for (Genome child : offspring.keySet()) {
                if (donor != null && child.isTrueBreeding()) {
                    CultivarTrigger.fire(player, CultivarTrigger.TRUE_LINE);
                }
                if (carriesFlag(child, table, true)) {
                    CultivarTrigger.fire(player, CultivarTrigger.MUTANT);
                }
                if (!crop.isWild() && carriesFlag(child, table, false)
                        && Phenotype.resolve(child, table).get(Gene.YIELD) >= 1.0f) {
                    CultivarTrigger.fire(player, CultivarTrigger.WILD_BLOOD);
                }
            }
        }
        return stacks;
    }

    private static void recordCross(ServerLevel level, ServerPlayer player, CropBlockEntity crop, CropSpecies species, Genome mother,
                                    Genome father, Map<Genome, Integer> offspring) {
        List<String> children = new ArrayList<>();
        for (Genome child : offspring.keySet()) {
            children.add(child.toHex());
            if (children.size() >= 4) {
                break;
            }
        }
        CrossLog.Entry entry = new CrossLog.Entry(species.key(), mother.toHex(), father.toHex(), level.getGameTime() / 24000L,
                crop.pollinationMethod().getSerializedName(), children);
        player.setData(ModAttachments.CROSS_LOG, player.getData(ModAttachments.CROSS_LOG).with(entry));
    }

    public static boolean carriesFlag(Genome genome, AlleleTable table, boolean mutationOnly) {
        for (Gene gene : Gene.VALUES) {
            for (int slot = 0; slot < 2; slot++) {
                Allele allele = table.get(gene, genome.allele(gene, slot));
                if (mutationOnly ? allele.mutationOnly() : allele.wild()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack seedStack(CropSpecies species, Genome genome, long lineage, boolean wild) {
        ItemStack stack = wild ? new ItemStack(ModItems.WILD_SEED.get()) : new ItemStack(ModItems.seedPacket(species));
        if (wild) {
            stack.set(ModDataComponents.SPECIES.get(), species.key());
        }
        stack.set(ModDataComponents.GENOME.get(), genome);
        if (lineage > 0) {
            stack.set(ModDataComponents.LINEAGE.get(), lineage);
        }
        return stack;
    }
}
