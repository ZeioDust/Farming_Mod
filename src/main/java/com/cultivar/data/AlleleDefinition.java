package com.cultivar.data;

import com.cultivar.genetics.Allele;
import com.cultivar.genetics.Gene;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record AlleleDefinition(Gene gene, int code, float value, int dominance, boolean mutationOnly, boolean wild,
                               int mutationWeight) {
    public static final Codec<AlleleDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            GeneticsCodecs.GENE.fieldOf("gene").forGetter(AlleleDefinition::gene),
            ExtraCodecs.intRange(0, 255).fieldOf("code").forGetter(AlleleDefinition::code),
            Codec.FLOAT.fieldOf("value").forGetter(AlleleDefinition::value),
            ExtraCodecs.intRange(0, 15).fieldOf("dominance").forGetter(AlleleDefinition::dominance),
            Codec.BOOL.optionalFieldOf("mutation_only", false).forGetter(AlleleDefinition::mutationOnly),
            Codec.BOOL.optionalFieldOf("wild", false).forGetter(AlleleDefinition::wild),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("mutation_weight", 10).forGetter(AlleleDefinition::mutationWeight)
    ).apply(i, AlleleDefinition::new));

    public static AlleleDefinition of(Allele allele) {
        return new AlleleDefinition(allele.gene(), allele.code(), allele.value(), allele.dominance(), allele.mutationOnly(),
                allele.wild(), allele.mutationWeight());
    }

    public Allele toAllele(String id) {
        return new Allele(gene, code, id, value, dominance, mutationOnly, wild, mutationWeight);
    }
}
