package com.cultivar.market;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Grade implements StringRepresentable {
    C(0xFFB0B0A8, 1.0f, 1.0f),
    B(0xFF7ED07A, 1.25f, 1.6f),
    A(0xFF5AB8F2, 1.5f, 2.4f),
    PRIME(0xFFF2C442, 2.0f, 4.0f);

    public static final Codec<Grade> CODEC = StringRepresentable.fromEnum(Grade::values);
    public static final StreamCodec<ByteBuf, Grade> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> values()[Math.max(0, Math.min(3, i))],
            Grade::ordinal);

    private final int color;
    private final float food;
    private final float price;

    Grade(int color, float food, float price) {
        this.color = color;
        this.food = food;
        this.price = price;
    }

    public int color() {
        return color;
    }

    public float foodMultiplier() {
        return food;
    }

    public float priceMultiplier() {
        return price;
    }

    public boolean atLeast(Grade other) {
        return ordinal() >= other.ordinal();
    }

    public String translationKey() {
        return "grade.cultivar." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
