package com.cultivar.recipe;

import com.cultivar.Cultivar;
import com.cultivar.registry.ModRecipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class RecipeSync {
    private RecipeSync() {
    }

    @SubscribeEvent
    static void sync(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.PROCESSING.get(), ModRecipes.COMPOSTING.get());
    }
}
