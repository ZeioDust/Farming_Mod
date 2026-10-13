package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModTags;
import com.cultivar.world.dimension.CultivarDimensions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public final class CultivarTagProviders {
    private CultivarTagProviders() {
    }

    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, lookup, Cultivar.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Blocks.PLANTABLE_SOIL).add(net.minecraft.world.level.block.Blocks.FARMLAND, ModBlocks.MANAGED_SOIL.get(),
                    ModBlocks.BREEDING_PLOT.get());
            tag(ModTags.Blocks.CROP_SOIL).add(net.minecraft.world.level.block.Blocks.FARMLAND, ModBlocks.MANAGED_SOIL.get(),
                    ModBlocks.BREEDING_PLOT.get(), ModBlocks.DEPLETED_SOIL.get(), net.minecraft.world.level.block.Blocks.GRASS_BLOCK,
                    net.minecraft.world.level.block.Blocks.DIRT, net.minecraft.world.level.block.Blocks.COARSE_DIRT,
                    net.minecraft.world.level.block.Blocks.PODZOL, net.minecraft.world.level.block.Blocks.ROOTED_DIRT,
                    net.minecraft.world.level.block.Blocks.MOSS_BLOCK, ModBlocks.CANOPY_LEAFMASS.get(), ModBlocks.CANOPY_BARK.get());
            var crops = tag(ModTags.Blocks.CROPS);
            for (CropSpecies species : CropSpecies.VALUES) {
                crops.add(ModBlocks.crop(species));
            }
            crops.add(ModBlocks.GRAFT_UNION.get());
            tag(BlockTags.CLIMBABLE).add(ModBlocks.WILD_VINE_BLOCK.get());
            tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.MANAGED_SOIL.get(), ModBlocks.BREEDING_PLOT.get(), ModBlocks.DEPLETED_SOIL.get());
            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.COMPOST_BIN.get(), ModBlocks.HARVEST_COLLECTOR.get(), ModBlocks.TRIAL_MARKER.get(),
                    ModBlocks.CANOPY_BARK.get());
            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.CANOPY_LEAFMASS.get(), ModBlocks.WILD_VINE_BLOCK.get(), ModBlocks.ISOLATION_SCREEN.get());
            Block[] metal = {ModBlocks.LIME_HOPPER.get(), ModBlocks.SULFUR_HOPPER.get(), ModBlocks.NUTRIENT_SPREADER.get(),
                    ModBlocks.POLLINATION_UNIT.get(), ModBlocks.BURN_PAN.get(), ModBlocks.GENOME_LAB.get(), ModBlocks.SEED_BANK.get(),
                    ModBlocks.SEED_VAULT_DRAWER.get(), ModBlocks.MUTAGEN_CHAMBER.get(), ModBlocks.THERMAL_SHOCK_UNIT.get(),
                    ModBlocks.GROW_LIGHT.get(), ModBlocks.HUMIDIFIER.get(), ModBlocks.DEHUMIDIFIER.get(), ModBlocks.CLIMATE_CONTROLLER.get(),
                    ModBlocks.IRRIGATION_LINE.get(), ModBlocks.SPRINKLER.get()};
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(metal);
            tag(BlockTags.IMPERMEABLE).add(ModBlocks.GREENHOUSE_PANE.get());
        }
    }

    public static class Items extends BlockTagCopyingItemTagProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
            super(output, lookup, blockTags, Cultivar.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            var packets = tag(ModTags.Items.SEED_PACKETS);
            var produce = tag(ModTags.Items.PRODUCE);
            for (CropSpecies species : CropSpecies.VALUES) {
                packets.add(ModItems.seedPacket(species));
                produce.add(ModItems.produce(species));
            }
            packets.add(ModItems.WILD_SEED.get());
            tag(ModTags.Items.AMENDMENTS).add(ModItems.COMPOST.get(), ModItems.LIME_POWDER.get(), ModItems.SULFUR_POWDER.get(),
                    ModItems.NUTRIENT_BLEND_N.get(), ModItems.NUTRIENT_BLEND_P.get(), ModItems.NUTRIENT_BLEND_K.get(), ModItems.GREEN_MANURE.get());
            tag(ModTags.Items.PLANT_WASTE).add(net.minecraft.world.item.Items.WHEAT, net.minecraft.world.item.Items.WHEAT_SEEDS,
                    net.minecraft.world.item.Items.SHORT_GRASS, net.minecraft.world.item.Items.FERN, net.minecraft.world.item.Items.KELP,
                    net.minecraft.world.item.Items.DRIED_KELP, net.minecraft.world.item.Items.SUGAR_CANE, net.minecraft.world.item.Items.VINE,
                    net.minecraft.world.item.Items.CARROT, net.minecraft.world.item.Items.POTATO, net.minecraft.world.item.Items.BEETROOT,
                    net.minecraft.world.item.Items.MELON_SLICE, net.minecraft.world.item.Items.PUMPKIN, net.minecraft.world.item.Items.APPLE,
                    net.minecraft.world.item.Items.HAY_BLOCK, ModItems.WILD_VINE_BLOCK.get());
            tag(ModTags.Items.BURN_FUEL).add(net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL,
                    net.minecraft.world.item.Items.BLAZE_ROD, net.minecraft.world.item.Items.DRIED_KELP_BLOCK, ModItems.BIOFUEL_CAKE.get());
            tag(ModTags.Items.COOLANTS).add(net.minecraft.world.item.Items.ICE, net.minecraft.world.item.Items.PACKED_ICE,
                    net.minecraft.world.item.Items.BLUE_ICE, net.minecraft.world.item.Items.SNOW_BLOCK);
        }
    }

    public static class Biomes extends BiomeTagsProvider {
        public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, lookup, Cultivar.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Biomes.FERAL_CROPS).add(net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS,
                    net.minecraft.world.level.biome.Biomes.MEADOW, net.minecraft.world.level.biome.Biomes.SAVANNA,
                    net.minecraft.world.level.biome.Biomes.FLOWER_FOREST).addTag(BiomeTags.IS_FOREST);
            tag(ModTags.Biomes.HAS_TRIAL_FIELD).add(net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS,
                    net.minecraft.world.level.biome.Biomes.MEADOW, net.minecraft.world.level.biome.Biomes.SAVANNA);
            tag(ModTags.Biomes.HAS_RUINED_GREENHOUSE).add(net.minecraft.world.level.biome.Biomes.PLAINS,
                    net.minecraft.world.level.biome.Biomes.FOREST, net.minecraft.world.level.biome.Biomes.BIRCH_FOREST,
                    net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, net.minecraft.world.level.biome.Biomes.TAIGA);
            tag(ModTags.Biomes.HAS_SEED_STORE).add(net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.TAIGA,
                    net.minecraft.world.level.biome.Biomes.FOREST, net.minecraft.world.level.biome.Biomes.MEADOW);
            tag(ModTags.Biomes.HAS_COMPOST_YARD).add(net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.FOREST,
                    net.minecraft.world.level.biome.Biomes.SWAMP, net.minecraft.world.level.biome.Biomes.SAVANNA);
            tag(ModTags.Biomes.HAS_BLIGHT_SCAR).add(net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.SAVANNA,
                    net.minecraft.world.level.biome.Biomes.FOREST, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS);
            tag(ModTags.Biomes.HAS_CANOPY_PLATFORM).add(CultivarDimensions.CANOPY_GROVE).addTag(BiomeTags.IS_JUNGLE)
                    .add(net.minecraft.world.level.biome.Biomes.DARK_FOREST);
            tag(ModTags.Biomes.HAS_WILD_STAND).add(CultivarDimensions.CANOPY_GROVE);
            tag(ModTags.Biomes.HAS_CANOPY_BREACH).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_FOREST)
                    .add(net.minecraft.world.level.biome.Biomes.DARK_FOREST);
        }
    }
}
