package com.cultivar.data;

import com.cultivar.genetics.Gene;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ExtraCodecs;

public record SpeciesDefinition(int ticksPerStage, int baseYield, int seedsPerHarvest, Nutrient preferredNutrient,
                                float nutrientUse, float maxTemperature, Map<Gene, List<String>> naturalAlleles) {
    public static final Codec<SpeciesDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("ticks_per_stage").forGetter(SpeciesDefinition::ticksPerStage),
            ExtraCodecs.POSITIVE_INT.fieldOf("base_yield").forGetter(SpeciesDefinition::baseYield),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("seeds_per_harvest").forGetter(SpeciesDefinition::seedsPerHarvest),
            Nutrient.CODEC.fieldOf("preferred_nutrient").forGetter(SpeciesDefinition::preferredNutrient),
            Codec.FLOAT.fieldOf("nutrient_use").forGetter(SpeciesDefinition::nutrientUse),
            Codec.FLOAT.fieldOf("max_temperature").forGetter(SpeciesDefinition::maxTemperature),
            Codec.unboundedMap(GeneticsCodecs.GENE, Codec.STRING.listOf()).fieldOf("natural_alleles").forGetter(SpeciesDefinition::naturalAlleles)
    ).apply(i, SpeciesDefinition::new));
}
