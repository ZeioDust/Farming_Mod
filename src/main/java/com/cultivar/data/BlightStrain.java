package com.cultivar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record BlightStrain(List<Identifier> species, float transmission, int lethalTicks, float spontaneousChance,
                           List<String> defeatedBy, int color) {
    public static final Codec<BlightStrain> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.listOf().fieldOf("species").forGetter(BlightStrain::species),
            Codec.FLOAT.fieldOf("transmission").forGetter(BlightStrain::transmission),
            ExtraCodecs.POSITIVE_INT.fieldOf("lethal_ticks").forGetter(BlightStrain::lethalTicks),
            Codec.FLOAT.fieldOf("spontaneous_chance").forGetter(BlightStrain::spontaneousChance),
            Codec.STRING.listOf().fieldOf("defeated_by").forGetter(BlightStrain::defeatedBy),
            Codec.INT.optionalFieldOf("color", 0xFF8A7A50).forGetter(BlightStrain::color)
    ).apply(i, BlightStrain::new));

    public boolean infects(Identifier speciesId) {
        return species.contains(speciesId);
    }
}
