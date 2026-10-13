package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.world.dimension.CanopyChunkGenerator;
import com.cultivar.world.gen.AddItemChanceModifier;
import com.cultivar.world.gen.ConfigGatedFeaturesModifier;
import com.cultivar.world.gen.CropPatchFeature;
import com.cultivar.world.gen.CultivarSiteStructure;
import com.cultivar.world.gen.SitePiece;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModWorldgen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Cultivar.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Cultivar.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Cultivar.MODID);
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Cultivar.MODID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Cultivar.MODID);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Cultivar.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<CultivarSiteStructure>> SITE =
            STRUCTURE_TYPES.register("site", () -> () -> CultivarSiteStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SITE_PIECE =
            STRUCTURE_PIECES.register("site_piece", () -> (StructurePieceType.ContextlessType) SitePiece::new);
    public static final DeferredHolder<Feature<?>, CropPatchFeature> CROP_PATCH =
            FEATURES.register("crop_patch", () -> new CropPatchFeature(CropPatchFeature.Config.CODEC));
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<CanopyChunkGenerator>> CANOPY_GENERATOR =
            CHUNK_GENERATORS.register("canopy", () -> CanopyChunkGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigGatedFeaturesModifier>> CONFIG_GATED_FEATURES =
            BIOME_MODIFIERS.register("config_gated_features", () -> ConfigGatedFeaturesModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemChanceModifier>> ADD_ITEM_CHANCE =
            LOOT_MODIFIERS.register("add_item_chance", () -> AddItemChanceModifier.CODEC);

    private ModWorldgen() {
    }
}
