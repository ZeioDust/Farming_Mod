package com.cultivar.menu;

import com.cultivar.data.GeneticsCodecs;
import com.cultivar.genetics.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SeedBankLine(String name, Identifier species, Genome genome, long lineage, long storedAt, boolean wild) {
    public static final Codec<SeedBankLine> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(SeedBankLine::name),
            Identifier.CODEC.fieldOf("species").forGetter(SeedBankLine::species),
            GeneticsCodecs.GENOME.fieldOf("genome").forGetter(SeedBankLine::genome),
            Codec.LONG.fieldOf("lineage").forGetter(SeedBankLine::lineage),
            Codec.LONG.fieldOf("stored_at").forGetter(SeedBankLine::storedAt),
            Codec.BOOL.optionalFieldOf("wild", false).forGetter(SeedBankLine::wild)
    ).apply(i, SeedBankLine::new));

    public static final StreamCodec<ByteBuf, SeedBankLine> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), SeedBankLine::name,
            Identifier.STREAM_CODEC, SeedBankLine::species,
            GeneticsCodecs.GENOME_STREAM, SeedBankLine::genome,
            ByteBufCodecs.VAR_LONG, SeedBankLine::lineage,
            ByteBufCodecs.VAR_LONG, SeedBankLine::storedAt,
            ByteBufCodecs.BOOL, SeedBankLine::wild,
            SeedBankLine::new);
}
