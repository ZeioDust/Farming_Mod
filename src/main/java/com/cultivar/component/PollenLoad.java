package com.cultivar.component;

import com.cultivar.data.GeneticsCodecs;
import com.cultivar.genetics.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PollenLoad(Identifier dimension, BlockPos source, Identifier species, Genome genome, long lineage) {
    public static final Codec<PollenLoad> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(PollenLoad::dimension),
            BlockPos.CODEC.fieldOf("source").forGetter(PollenLoad::source),
            Identifier.CODEC.fieldOf("species").forGetter(PollenLoad::species),
            GeneticsCodecs.GENOME.fieldOf("genome").forGetter(PollenLoad::genome),
            Codec.LONG.fieldOf("lineage").forGetter(PollenLoad::lineage)
    ).apply(i, PollenLoad::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PollenLoad> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, PollenLoad::dimension,
            BlockPos.STREAM_CODEC, PollenLoad::source,
            Identifier.STREAM_CODEC, PollenLoad::species,
            GeneticsCodecs.GENOME_STREAM, PollenLoad::genome,
            ByteBufCodecs.VAR_LONG, PollenLoad::lineage,
            PollenLoad::new);
}
