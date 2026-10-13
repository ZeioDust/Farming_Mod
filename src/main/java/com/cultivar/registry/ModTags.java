package com.cultivar.registry;

import com.cultivar.Cultivar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, path);
    }

    public static final class Blocks {
        public static final TagKey<Block> CROP_SOIL = TagKey.create(Registries.BLOCK, id("crop_soil"));
        public static final TagKey<Block> PLANTABLE_SOIL = TagKey.create(Registries.BLOCK, id("plantable_soil"));
        public static final TagKey<Block> CROPS = TagKey.create(Registries.BLOCK, id("crops"));

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> SEED_PACKETS = TagKey.create(Registries.ITEM, id("seed_packets"));
        public static final TagKey<Item> PRODUCE = TagKey.create(Registries.ITEM, id("produce"));
        public static final TagKey<Item> AMENDMENTS = TagKey.create(Registries.ITEM, id("amendments"));
        public static final TagKey<Item> PLANT_WASTE = TagKey.create(Registries.ITEM, id("plant_waste"));
        public static final TagKey<Item> BURN_FUEL = TagKey.create(Registries.ITEM, id("burn_fuel"));
        public static final TagKey<Item> COOLANTS = TagKey.create(Registries.ITEM, id("coolants"));

        private Items() {
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> HAS_TRIAL_FIELD = TagKey.create(Registries.BIOME, id("has_structure/abandoned_trial_field"));
        public static final TagKey<Biome> HAS_RUINED_GREENHOUSE = TagKey.create(Registries.BIOME, id("has_structure/ruined_greenhouse"));
        public static final TagKey<Biome> HAS_SEED_STORE = TagKey.create(Registries.BIOME, id("has_structure/old_seed_store"));
        public static final TagKey<Biome> HAS_COMPOST_YARD = TagKey.create(Registries.BIOME, id("has_structure/compost_yard"));
        public static final TagKey<Biome> HAS_BLIGHT_SCAR = TagKey.create(Registries.BIOME, id("has_structure/blight_scar"));
        public static final TagKey<Biome> HAS_CANOPY_PLATFORM = TagKey.create(Registries.BIOME, id("has_structure/canopy_platform"));
        public static final TagKey<Biome> HAS_WILD_STAND = TagKey.create(Registries.BIOME, id("has_structure/wild_stand"));
        public static final TagKey<Biome> HAS_CANOPY_BREACH = TagKey.create(Registries.BIOME, id("has_structure/canopy_breach"));
        public static final TagKey<Biome> FERAL_CROPS = TagKey.create(Registries.BIOME, id("feral_crops"));

        private Biomes() {
        }
    }
}
