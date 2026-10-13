package com.cultivar.processing;

import com.cultivar.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record ProcessingRecipe(Ingredient input, int count, ItemStackTemplate result, int ticks) implements Recipe<SingleRecipeInput> {
    public static final MapCodec<ProcessingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ProcessingRecipe::input),
            ExtraCodecs.intRange(1, 64).optionalFieldOf("count", 1).forGetter(ProcessingRecipe::count),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ProcessingRecipe::result),
            ExtraCodecs.intRange(1, 72000).optionalFieldOf("ticks", 160).forGetter(ProcessingRecipe::ticks)
    ).apply(i, ProcessingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ProcessingRecipe::input,
            ByteBufCodecs.VAR_INT, ProcessingRecipe::count,
            ItemStackTemplate.STREAM_CODEC, ProcessingRecipe::result,
            ByteBufCodecs.VAR_INT, ProcessingRecipe::ticks,
            ProcessingRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item()) && input.item().getCount() >= count;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.PROCESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.PROCESSING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
