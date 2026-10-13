package com.cultivar.data;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum Nutrient implements StringRepresentable {
    NITROGEN("N"),
    PHOSPHORUS("P"),
    POTASSIUM("K");

    public static final Codec<Nutrient> CODEC = StringRepresentable.fromEnum(Nutrient::values);

    private final String symbol;

    Nutrient(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
