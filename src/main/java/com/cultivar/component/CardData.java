package com.cultivar.component;

import com.cultivar.data.GeneticsCodecs;
import com.cultivar.genetics.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CardData(Optional<Identifier> species, Optional<Genome> genome, Optional<Identifier> strain, long lineage) {
    public static final Codec<CardData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("species").forGetter(CardData::species),
            GeneticsCodecs.GENOME.optionalFieldOf("genome").forGetter(CardData::genome),
            Identifier.CODEC.optionalFieldOf("strain").forGetter(CardData::strain),
            Codec.LONG.optionalFieldOf("lineage", 0L).forGetter(CardData::lineage)
    ).apply(i, CardData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), CardData::species,
            ByteBufCodecs.optional(GeneticsCodecs.GENOME_STREAM), CardData::genome,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), CardData::strain,
            ByteBufCodecs.VAR_LONG, CardData::lineage,
            CardData::new);

    public static CardData ofGenome(Identifier species, Genome genome, long lineage) {
        return new CardData(Optional.of(species), Optional.of(genome), Optional.empty(), lineage);
    }

    public static CardData ofStrain(Identifier strain) {
        return new CardData(Optional.empty(), Optional.empty(), Optional.of(strain), 0L);
    }
}
