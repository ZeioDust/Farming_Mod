package com.cultivar.datagen;

import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModItems;
import com.cultivar.world.gen.SiteKind;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class CultivarLootProvider extends LootTableProvider {
    public CultivarLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(Chests::new, LootContextParamSets.CHEST)), registries);
    }

    public static class Blocks extends BlockLootSubProvider {
        public Blocks(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            for (CropSpecies species : CropSpecies.VALUES) {
                add(ModBlocks.crop(species), noDrop());
            }
            add(ModBlocks.GRAFT_UNION.get(), noDrop());
            Block bank = ModBlocks.SEED_BANK.get();
            add(bank, LootTable.lootTable().withPool(applyExplosionCondition(bank, LootPool.lootPool()
                    .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(bank).apply(net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
                            .copyComponentsFromBlockEntity(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                            .include(com.cultivar.registry.ModDataComponents.SEED_BANK_LINES.get()))))));
            for (var holder : ModBlocks.BLOCKS.getEntries()) {
                Block block = holder.get();
                if (block.asItem() != Items.AIR && block.getLootTable().isPresent() && !(block instanceof com.cultivar.block.CultivarCropBlock)
                        && block != bank) {
                    dropSelf(block);
                }
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(h -> (Block) h.get())
                    .filter(b -> b.getLootTable().isPresent()).toList();
        }
    }

    public record Chests(HolderLookup.Provider registries) implements LootTableSubProvider {
        private static LootItem.Builder<?> entry(ItemLike item, int weight, float min, float max) {
            return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        }

        private static LootPool.Builder seeds(int rolls) {
            LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(1, rolls));
            for (CropSpecies species : CropSpecies.VALUES) {
                pool.add(entry(ModItems.seedPacket(species), 10, 1, 3));
            }
            return pool;
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(SiteKind.ABANDONED_TRIAL_FIELD.lootTable(), LootTable.lootTable().withPool(seeds(4))
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(Items.PAPER, 10, 1, 4)).add(entry(ModItems.TRIAL_TAG.get(), 6, 1, 1))
                            .add(entry(ModItems.COMPOST.get(), 8, 1, 3)).add(entry(ModItems.FIELD_NOTEBOOK.get(), 2, 1, 1))
                            .add(entry(ModItems.HAND_LENS.get(), 3, 1, 1))));
            output.accept(SiteKind.RUINED_GREENHOUSE.lootTable(), LootTable.lootTable().withPool(seeds(3))
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(ModItems.GREENHOUSE_PANE.get(), 10, 2, 8)).add(entry(ModItems.IRRIGATION_LINE.get(), 8, 2, 6))
                            .add(entry(ModItems.GROW_LIGHT.get(), 2, 1, 1)).add(entry(ModItems.HUMIDIFIER.get(), 2, 1, 1))
                            .add(entry(ModItems.SOIL_PROBE.get(), 3, 1, 1))));
            output.accept(SiteKind.OLD_SEED_STORE.lootTable(), LootTable.lootTable().withPool(seeds(6))
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(ModItems.PEDIGREE_SHEET.get(), 8, 1, 3)).add(entry(Items.PAPER, 10, 2, 6))
                            .add(entry(ModItems.SEED_VAULT_DRAWER.get(), 2, 1, 1)).add(entry(ModItems.ISOLATION_BAG.get(), 6, 1, 4))));
            output.accept(SiteKind.COMPOST_YARD.lootTable(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                            .add(entry(ModItems.COMPOST.get(), 12, 2, 6)).add(entry(ModItems.LIME_POWDER.get(), 6, 1, 4))
                            .add(entry(ModItems.SULFUR_POWDER.get(), 4, 1, 3)).add(entry(ModItems.NUTRIENT_BLEND_N.get(), 4, 1, 2))
                            .add(entry(ModItems.NUTRIENT_BLEND_P.get(), 4, 1, 2)).add(entry(ModItems.NUTRIENT_BLEND_K.get(), 4, 1, 2))));
            output.accept(SiteKind.BLIGHT_SCAR.lootTable(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(ModItems.ISOLATION_SCREEN.get(), 10, 2, 6)).add(entry(ModItems.ISOLATION_BAG.get(), 8, 1, 4))
                            .add(entry(ModItems.BURN_PAN.get(), 2, 1, 1)).add(entry(ModItems.COMPOST.get(), 6, 1, 3))));
            output.accept(SiteKind.CANOPY_PLATFORM.lootTable(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2)).add(entry(ModItems.CANOPY_RESIN.get(), 1, 1, 3)))
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(ModItems.WILD_VINE_BLOCK.get(), 8, 2, 6)).add(entry(ModItems.CANOPY_LEAFMASS.get(), 8, 2, 8))
                            .add(entry(Items.PAPER, 4, 1, 3))));
            output.accept(SiteKind.WILD_STAND.lootTable(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                            .add(entry(ModItems.CANOPY_RESIN.get(), 3, 1, 1)).add(entry(ModItems.WILD_VINE_BLOCK.get(), 8, 2, 6))
                            .add(entry(ModItems.ISOLATION_BAG.get(), 4, 1, 3))));
            output.accept(SiteKind.CANOPY_BREACH.lootTable(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                            .add(entry(ModItems.CANOPY_RESIN.get(), 4, 1, 2)).add(entry(Items.PAPER, 6, 1, 4))
                            .add(entry(ModItems.WILD_VINE_BLOCK.get(), 6, 1, 4))));
        }
    }
}
