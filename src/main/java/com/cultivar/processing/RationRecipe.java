package com.cultivar.processing;

import com.cultivar.data.CropSpecies;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RationRecipe extends CustomRecipe {
    public static final RationRecipe INSTANCE = new RationRecipe();
    public static final MapCodec<RationRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RationRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return build(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = build(input);
        return result != null ? result : ItemStack.EMPTY;
    }

    public static @Nullable ItemStack build(CraftingInput input) {
        int canvas = 0;
        List<ItemStack> foods = new ArrayList<>();
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.LINEN_CANVAS.get())) {
                canvas++;
            } else if (RationIngredients.species(stack) != null) {
                foods.add(stack);
            } else {
                return null;
            }
        }
        if (canvas != 1 || foods.size() < 3) {
            return null;
        }
        Set<Identifier> species = new LinkedHashSet<>();
        Grade lowest = Grade.PRIME;
        int nutrition = 0;
        float saturation = 0;
        for (ItemStack food : foods) {
            CropSpecies crop = RationIngredients.species(food);
            species.add(crop.key());
            Grade grade = GradeLogic.of(food);
            if (!grade.atLeast(lowest)) {
                lowest = grade;
            }
            FoodProperties props = food.get(DataComponents.FOOD);
            if (props != null) {
                nutrition += props.nutrition();
                saturation += props.saturation();
            } else {
                nutrition += 2;
                saturation += 1f;
            }
        }
        ItemStack pack = new ItemStack(ModItems.RATION_PACK.get());
        pack.set(ModDataComponents.RATION.get(), new RationData(List.copyOf(species), lowest));
        pack.set(DataComponents.FOOD, new FoodProperties(Math.max(4, Math.min(20, Math.round(nutrition * 0.6f))),
                Math.min(20f, saturation * 0.6f), true));
        return pack;
    }

    @Override
    public RecipeSerializer<RationRecipe> getSerializer() {
        return ModRecipes.RATION_SERIALIZER.get();
    }
}
