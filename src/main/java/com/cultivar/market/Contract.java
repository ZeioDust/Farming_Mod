package com.cultivar.market;

import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.item.ProduceItem;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record Contract(Kind kind, Identifier species, int count, Grade minGrade, Optional<Gene> gene, float threshold,
                       Optional<String> allele, boolean trueBreeding, int reward, Optional<ItemStackTemplate> bonus, int progress) {
    public enum Kind implements StringRepresentable {
        PRODUCE, TRAIT, ALLELE;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Codec<Gene> GENE_CODEC = Codec.STRING.xmap(Gene::byId, Gene::id);

    public static final Codec<Contract> CODEC = RecordCodecBuilder.create(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(Contract::kind),
            Identifier.CODEC.fieldOf("species").forGetter(Contract::species),
            Codec.INT.fieldOf("count").forGetter(Contract::count),
            Grade.CODEC.fieldOf("min_grade").forGetter(Contract::minGrade),
            GENE_CODEC.optionalFieldOf("gene").forGetter(Contract::gene),
            Codec.FLOAT.fieldOf("threshold").forGetter(Contract::threshold),
            Codec.STRING.optionalFieldOf("allele").forGetter(Contract::allele),
            Codec.BOOL.fieldOf("true_breeding").forGetter(Contract::trueBreeding),
            Codec.INT.fieldOf("reward").forGetter(Contract::reward),
            ItemStackTemplate.CODEC.optionalFieldOf("bonus").forGetter(Contract::bonus),
            Codec.INT.fieldOf("progress").forGetter(Contract::progress)
    ).apply(i, Contract::new));

    public Contract withProgress(int value) {
        return new Contract(kind, species, count, minGrade, gene, threshold, allele, trueBreeding, reward, bonus, value);
    }

    public int remaining() {
        return Math.max(0, count - progress);
    }

    public boolean complete() {
        return progress >= count;
    }

    public boolean accepts(ItemStack stack, Catalog catalog) {
        return switch (kind) {
            case PRODUCE -> stack.getItem() instanceof ProduceItem produce && produce.species().key().equals(species)
                    && GradeLogic.of(stack).atLeast(minGrade);
            case TRAIT -> {
                Genome genome = packetGenome(stack);
                if (genome == null || gene.isEmpty()) {
                    yield false;
                }
                float value = Phenotype.resolve(genome, catalog.table()).get(gene.get());
                boolean meets = catalog.gene(gene.get()).higherIsBetter() ? value >= threshold - 1e-4f : value <= threshold + 1e-4f;
                yield meets && (!trueBreeding || genome.isTrueBreeding());
            }
            case ALLELE -> {
                Genome genome = packetGenome(stack);
                Allele target = allele.map(id -> catalog.table().byId(id)).orElse(null);
                yield genome != null && target != null && genome.carries(target.gene(), target.code());
            }
        };
    }

    private Genome packetGenome(ItemStack stack) {
        if (!(stack.getItem() instanceof SeedPacketItem packet) || !packet.species().key().equals(species)) {
            return null;
        }
        return stack.get(ModDataComponents.GENOME.get());
    }

    public MutableComponent title(Catalog catalog) {
        Component speciesName = GeneText.species(species);
        return switch (kind) {
            case PRODUCE -> Texts.tr(Texts.CONTRACT_PRODUCE, count, speciesName,
                    Component.translatable(minGrade.translationKey()).withColor(minGrade.color() & 0xFFFFFF));
            case TRAIT -> {
                Gene g = gene.orElse(Gene.YIELD);
                Component target = Component.literal((catalog.gene(g).higherIsBetter() ? ">= " : "<= ") + g.format(threshold));
                yield Texts.tr(trueBreeding ? Texts.CONTRACT_TRAIT_PURE : Texts.CONTRACT_TRAIT, speciesName, GeneText.gene(g), target);
            }
            case ALLELE -> {
                Allele target = allele.map(id -> catalog.table().byId(id)).orElse(null);
                yield Texts.tr(Texts.CONTRACT_ALLELE, speciesName, target != null ? GeneText.allele(target) : Component.literal("?"));
            }
        };
    }

    public CropSpecies cropSpecies() {
        CropSpecies crop = CropSpecies.byKey(species);
        return crop != null ? crop : CropSpecies.WHEATLINE;
    }

    public static Contract of(Kind kind, Identifier species, int count, Grade minGrade, Optional<Gene> gene, float threshold,
                              Optional<String> allele, boolean trueBreeding, int reward, Optional<ItemStackTemplate> bonus) {
        return new Contract(kind, species, count, minGrade, gene, threshold, allele, trueBreeding, reward, bonus, 0);
    }
}
