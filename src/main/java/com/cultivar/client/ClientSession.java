package com.cultivar.client;

import com.cultivar.Cultivar;
import com.cultivar.client.screen.ClimateControllerScreen;
import com.cultivar.client.screen.ProduceExchangeScreen;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class ClientSession {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    private ClientSession() {
    }

    public static RecipeMap recipes() {
        return recipes;
    }

    @SubscribeEvent
    static void received(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    @SubscribeEvent
    static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = RecipeMap.EMPTY;
        ProduceExchangeScreen.DEMAND.clear();
        ClimateControllerScreen.clearHistory();
    }
}
