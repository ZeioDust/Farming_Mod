package com.cultivar.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Certificate(String breeder, int score, long day, String ribbon, boolean record) {
    public static final Codec<Certificate> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("breeder").forGetter(Certificate::breeder),
            Codec.INT.fieldOf("score").forGetter(Certificate::score),
            Codec.LONG.fieldOf("day").forGetter(Certificate::day),
            Codec.STRING.fieldOf("ribbon").forGetter(Certificate::ribbon),
            Codec.BOOL.optionalFieldOf("record", false).forGetter(Certificate::record)
    ).apply(i, Certificate::new));
    public static final StreamCodec<ByteBuf, Certificate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), Certificate::breeder,
            ByteBufCodecs.VAR_INT, Certificate::score,
            ByteBufCodecs.VAR_LONG, Certificate::day,
            ByteBufCodecs.stringUtf8(8), Certificate::ribbon,
            ByteBufCodecs.BOOL, Certificate::record,
            Certificate::new);
}
