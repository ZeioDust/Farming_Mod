package com.cultivar.market;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class MarketData extends SavedData {
    public static final Codec<MarketData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).fieldOf("recent").forGetter(d -> d.recent),
            Codec.LONG.fieldOf("day").forGetter(d -> d.day)
    ).apply(i, MarketData::new));
    public static final SavedDataType<MarketData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "market"), MarketData::new, CODEC);
    public static final float DAILY_DECAY = 0.75f;

    private final Map<Identifier, Float> recent;
    private long day;

    public MarketData() {
        this(Map.of(), 0L);
    }

    private MarketData(Map<Identifier, Float> recent, long day) {
        this.recent = new HashMap<>(recent);
        this.day = day;
    }

    public static MarketData get(MinecraftServer server) {
        MarketData data = server.overworld().getDataStorage().computeIfAbsent(TYPE);
        data.advanceTo(server.overworld().getOverworldClockTime() / 24000L);
        return data;
    }

    private void advanceTo(long today) {
        if (today <= day) {
            return;
        }
        float factor = (float) Math.pow(DAILY_DECAY, Math.min(30, today - day));
        recent.replaceAll((k, v) -> v * factor);
        recent.values().removeIf(v -> v < 0.5f);
        day = today;
        setDirty();
    }

    public float recent(Identifier item) {
        return recent.getOrDefault(item, 0f);
    }

    public float demand(Identifier item) {
        int saturation = CultivarConfig.isLoaded() ? CultivarConfig.EXCHANGE_SATURATION.get() : 96;
        return 1f / (1f + recent(item) / saturation);
    }

    public float unitPrice(ItemStack stack) {
        float base = MarketValues.base(stack);
        if (base <= 0) {
            return 0f;
        }
        double multiplier = CultivarConfig.isLoaded() ? CultivarConfig.EXCHANGE_PRICE_MULTIPLIER.get() : 1.0;
        return (float) (base * GradeLogic.of(stack).priceMultiplier() * demand(BuiltInRegistries.ITEM.getKey(stack.getItem())) * multiplier);
    }

    public float sell(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        float total = 0;
        for (int i = 0; i < stack.getCount(); i++) {
            total += unitPrice(stack);
            recent.merge(id, 1f, Float::sum);
        }
        setDirty();
        return total;
    }
}
