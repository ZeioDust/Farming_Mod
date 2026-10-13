package com.cultivar.guide;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.cultivar.network.GuideSettingPayload;
import com.cultivar.network.GuideStatePayload;
import com.cultivar.network.HintPayload;
import com.cultivar.network.RecipeReplyPayload;
import com.cultivar.network.RecipeRequestPayload;
import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModItems;
import com.cultivar.world.dimension.CultivarDimensions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class Guide {
    private static final int MAX_RECIPES_PER_ITEM = 4;
    private static @Nullable RecipeManager indexedFor;
    private static Map<Item, List<RecipeDisplay>> recipeIndex = Map.of();

    private Guide() {
    }

    public static void hint(ServerPlayer player, Hint hint) {
        GuideState state = player.getData(ModAttachments.GUIDE);
        GuideState next = state.withMilestone(hint.id());
        boolean show = !state.seen(hint.id()) && !state.hintsOff() && CultivarConfig.TUTORIAL_HINTS.get();
        if (show) {
            next = next.withHintSeen(hint.id());
        }
        if (!next.equals(state)) {
            player.setData(ModAttachments.GUIDE, next);
            sync(player);
        }
        if (show) {
            send(player, new HintPayload(hint.id()));
        }
    }

    public static void hintNear(ServerLevel level, BlockPos pos, double radius, Hint hint) {
        double r2 = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= r2) {
                hint(player, hint);
            }
        }
    }

    public static void milestone(ServerPlayer player, String milestone) {
        GuideState state = player.getData(ModAttachments.GUIDE);
        GuideState next = state.withMilestone(milestone);
        if (!next.equals(state)) {
            player.setData(ModAttachments.GUIDE, next);
            sync(player);
        }
    }

    public static void sync(ServerPlayer player) {
        GuideState state = player.getData(ModAttachments.GUIDE);
        send(player, new GuideStatePayload(state.milestones(), state.hintsSeen(), state.hintsOff()));
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        GuideState state = player.getData(ModAttachments.GUIDE);
        if (!state.manualGiven() && CultivarConfig.GIVE_FIELD_MANUAL.get()) {
            ItemStack manual = new ItemStack(ModItems.FIELD_MANUAL.get());
            if (!player.getInventory().add(manual)) {
                player.drop(manual, false);
            }
            player.setData(ModAttachments.GUIDE, state.withManualGiven());
        }
        sync(player);
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo().equals(CultivarDimensions.CANOPY_LEVEL)) {
            milestone(player, Milestones.CANOPY);
            hint(player, Hint.WILD);
        }
    }

    public static void handleSetting(GuideSettingPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            player.setData(ModAttachments.GUIDE, player.getData(ModAttachments.GUIDE).withHintsOff(payload.hintsOff()));
            sync(player);
        }
    }

    public static void handleRecipeRequest(RecipeRequestPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(payload.item()).orElse(null);
        if (item == null) {
            return;
        }
        List<RecipeDisplay> displays = recipesFor(player.level(), item);
        send(player, new RecipeReplyPayload(payload.item(), displays));
    }

    static synchronized List<RecipeDisplay> recipesFor(ServerLevel level, Item item) {
        RecipeManager recipes = level.getServer().getRecipeManager();
        if (recipes != indexedFor) {
            ContextMap context = SlotDisplayContext.fromLevel(level);
            Map<Item, List<RecipeDisplay>> index = new HashMap<>();
            for (RecipeHolder<?> holder : recipes.getRecipes()) {
                for (RecipeDisplay display : holder.value().display()) {
                    if (!(display instanceof ShapedCraftingRecipeDisplay) && !(display instanceof ShapelessCraftingRecipeDisplay)) {
                        continue;
                    }
                    ItemStack result = display.result().resolveForFirstStack(context);
                    if (!result.isEmpty()) {
                        List<RecipeDisplay> list = index.computeIfAbsent(result.getItem(), k -> new ArrayList<>());
                        if (list.size() < MAX_RECIPES_PER_ITEM) {
                            list.add(display);
                        }
                    }
                }
            }
            recipeIndex = index;
            indexedFor = recipes;
        }
        return recipeIndex.getOrDefault(item, List.of());
    }
}
