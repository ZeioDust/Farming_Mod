package com.cultivar.data;

import com.cultivar.genetics.Gene;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GeneDefinition(Gene gene, float displayMin, float displayMax, boolean higherIsBetter, int color) {
    public static final Codec<GeneDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            GeneticsCodecs.GENE.fieldOf("gene").forGetter(GeneDefinition::gene),
            Codec.FLOAT.fieldOf("display_min").forGetter(GeneDefinition::displayMin),
            Codec.FLOAT.fieldOf("display_max").forGetter(GeneDefinition::displayMax),
            Codec.BOOL.fieldOf("higher_is_better").forGetter(GeneDefinition::higherIsBetter),
            Codec.INT.optionalFieldOf("color", 0xFF6FAF4F).forGetter(GeneDefinition::color)
    ).apply(i, GeneDefinition::new));

    public static GeneDefinition defaultFor(Gene gene) {
        int color = switch (gene) {
            case YIELD -> 0xFFE0B040;
            case GROWTH_RATE -> 0xFF6FBF4F;
            case COLD_TOLERANCE -> 0xFF60A8E0;
            case DROUGHT_TOLERANCE -> 0xFFD08A40;
            case LIGHT_NEED -> 0xFFF0E070;
            case BLIGHT_RESISTANCE -> 0xFFB070C0;
            case NUTRIENT_DRAW -> 0xFF8A6A40;
            case MUTATION_RATE -> 0xFFC05050;
        };
        return new GeneDefinition(gene, gene.min(), gene.max(), gene.higherIsBetter(), color);
    }

    public float favourability(float value) {
        float span = displayMax - displayMin;
        if (span <= 0) {
            return 0.5f;
        }
        float t = Math.max(0f, Math.min(1f, (value - displayMin) / span));
        return higherIsBetter ? t : 1f - t;
    }
}
