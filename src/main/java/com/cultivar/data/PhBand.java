package com.cultivar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record PhBand(Identifier species, float min, float max, float lethalMargin) {
    public static final Codec<PhBand> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("species").forGetter(PhBand::species),
            Codec.FLOAT.fieldOf("min").forGetter(PhBand::min),
            Codec.FLOAT.fieldOf("max").forGetter(PhBand::max),
            Codec.FLOAT.optionalFieldOf("lethal_margin", 1.5f).forGetter(PhBand::lethalMargin)
    ).apply(i, PhBand::new));

    public float distanceOutside(float ph, float widthFactor) {
        float mid = (min + max) * 0.5f;
        float half = (max - min) * 0.5f * widthFactor;
        float d = Math.abs(ph - mid) - half;
        return Math.max(0f, d);
    }

    public boolean isLethal(float ph, float widthFactor) {
        return distanceOutside(ph, widthFactor) > lethalMargin * widthFactor;
    }
}
