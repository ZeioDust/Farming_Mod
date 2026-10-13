package com.cultivar.processing;

import com.cultivar.market.Grade;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record RationData(List<Identifier> species, Grade grade) {
    public static final Codec<RationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.listOf().fieldOf("species").forGetter(RationData::species),
            Grade.CODEC.fieldOf("grade").forGetter(RationData::grade)
    ).apply(i, RationData::new));
    public static final StreamCodec<ByteBuf, RationData> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list(16)), RationData::species,
            Grade.STREAM_CODEC, RationData::grade,
            RationData::new);

    public int strength() {
        return Math.max(1, Math.min(7, species.size() - 1 + grade.ordinal() + 1));
    }
}
