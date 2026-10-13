package com.cultivar.market;

import com.cultivar.Cultivar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class MarketValues {
    public record Value(float emeralds) {
        public static final Codec<Value> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.floatRange(0f, 1000f).fieldOf("emeralds").forGetter(Value::emeralds)
        ).apply(i, Value::new));
    }

    public static final DataMapType<Item, Value> MARKET_VALUE = DataMapType.builder(
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "market_value"), Registries.ITEM, Value.CODEC).synced(Value.CODEC, true).build();

    private MarketValues() {
    }

    @SubscribeEvent
    static void register(RegisterDataMapTypesEvent event) {
        event.register(MARKET_VALUE);
    }

    public static float base(ItemStack stack) {
        Value value = stack.getItem().builtInRegistryHolder().getData(MARKET_VALUE);
        return value != null ? value.emeralds() : 0f;
    }
}
