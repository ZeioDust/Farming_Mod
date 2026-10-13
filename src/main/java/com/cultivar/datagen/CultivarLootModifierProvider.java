package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModItems;
import com.cultivar.world.gen.AddItemChanceModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

public class CultivarLootModifierProvider extends GlobalLootModifierProvider {
    public CultivarLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Cultivar.MODID);
    }

    private static LootItemCondition[] table(net.minecraft.resources.Identifier id) {
        return new LootItemCondition[]{LootTableIdCondition.builder(id).build()};
    }

    @Override
    protected void start() {
        add("canopy_resin_in_jungle_temple", new AddItemChanceModifier(table(BuiltInLootTables.JUNGLE_TEMPLE.identifier()),
                IGlobalLootModifier.DEFAULT_PRIORITY, List.of(ModItems.CANOPY_RESIN.get()), 0.3f));
        add("canopy_resin_in_woodland_mansion", new AddItemChanceModifier(table(BuiltInLootTables.WOODLAND_MANSION.identifier()),
                IGlobalLootModifier.DEFAULT_PRIORITY, List.of(ModItems.CANOPY_RESIN.get()), 0.25f));
        List<Item> packets = new ArrayList<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            packets.add(ModItems.seedPacket(species));
        }
        add("seed_packets_from_short_grass", new AddItemChanceModifier(table(Blocks.SHORT_GRASS.getLootTable().orElseThrow().identifier()),
                IGlobalLootModifier.DEFAULT_PRIORITY, packets, 0.04f));
        add("seed_packets_from_tall_grass", new AddItemChanceModifier(table(Blocks.TALL_GRASS.getLootTable().orElseThrow().identifier()),
                IGlobalLootModifier.DEFAULT_PRIORITY, packets, 0.06f));
    }
}
