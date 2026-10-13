package com.cultivar.world.gen;

import com.cultivar.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemChanceModifier extends LootModifier {
    public static final MapCodec<AddItemChanceModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i)
            .and(BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(m -> m.items))
            .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
            .apply(i, AddItemChanceModifier::new));

    private final List<Item> items;
    private final float chance;

    public AddItemChanceModifier(LootItemCondition[] conditions, int priority, List<Item> items, float chance) {
        super(conditions, priority);
        this.items = List.copyOf(items);
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!items.isEmpty() && context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(items.get(context.getRandom().nextInt(items.size()))));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModWorldgen.ADD_ITEM_CHANCE.get();
    }
}
