package com.cultivar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record NitrogenFixer(Identifier species, float nitrogenPerStage) {
    public static final Codec<NitrogenFixer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("species").forGetter(NitrogenFixer::species),
            Codec.FLOAT.fieldOf("nitrogen_per_stage").forGetter(NitrogenFixer::nitrogenPerStage)
    ).apply(i, NitrogenFixer::new));
}
