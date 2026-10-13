package com.cultivar.processing;

import com.cultivar.data.CropSpecies;
import com.cultivar.item.ProduceItem;
import com.cultivar.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class RationIngredients {
    private RationIngredients() {
    }

    public static @Nullable CropSpecies species(ItemStack stack) {
        if (stack.getItem() instanceof ProduceItem produce) {
            return produce.species();
        }
        if (stack.is(ModItems.PICKLED_BRASSICA.get())) {
            return CropSpecies.BRASSICA;
        }
        if (stack.is(ModItems.GOURD_PUREE.get())) {
            return CropSpecies.GOURD;
        }
        if (stack.is(ModItems.FIELD_MEAL.get())) {
            return CropSpecies.WHEATLINE;
        }
        return null;
    }
}
