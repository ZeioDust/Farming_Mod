package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.processing.ProcessingRecipe;
import com.cultivar.processing.RationRecipe;
import com.cultivar.recipe.CompostingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Cultivar.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Cultivar.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CompostingRecipe>> COMPOSTING = TYPES.register("composting", RecipeType::simple);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompostingRecipe>> COMPOSTING_SERIALIZER =
            SERIALIZERS.register("composting", () -> new RecipeSerializer<>(CompostingRecipe.MAP_CODEC, CompostingRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<ProcessingRecipe>> PROCESSING = TYPES.register("processing", RecipeType::simple);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProcessingRecipe>> PROCESSING_SERIALIZER =
            SERIALIZERS.register("processing", () -> new RecipeSerializer<>(ProcessingRecipe.MAP_CODEC, ProcessingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RationRecipe>> RATION_SERIALIZER =
            SERIALIZERS.register("ration", () -> new RecipeSerializer<>(RationRecipe.MAP_CODEC, RationRecipe.STREAM_CODEC));

    private ModRecipes() {
    }
}
