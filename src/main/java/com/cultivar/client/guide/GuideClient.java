package com.cultivar.client.guide;

import com.cultivar.Cultivar;
import com.cultivar.guide.Hint;
import com.cultivar.network.GuideSettingPayload;
import com.cultivar.network.GuideStatePayload;
import com.cultivar.network.HintPayload;
import com.cultivar.network.RecipeReplyPayload;
import com.cultivar.network.RecipeRequestPayload;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class GuideClient {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Cultivar.MODID, "cultivar"));
    public static final KeyMapping OPEN_MANUAL = new KeyMapping("key.cultivar.field_manual", InputConstants.Type.KEYSYM, InputConstants.KEY_J,
            CATEGORY);

    private static final Set<String> MILESTONES = new HashSet<>();
    private static boolean hintsOff;
    private static Hint.@Nullable Chapter pendingChapter;
    private static Hint.Chapter lastChapter = Hint.Chapter.START;
    private static final Map<Hint.Chapter, Integer> LAST_PAGE = new HashMap<>();
    private static final Map<Identifier, List<RecipeDisplay>> RECIPES = new HashMap<>();
    private static final Set<Identifier> REQUESTED = new HashSet<>();

    private GuideClient() {
    }

    @SubscribeEvent
    static void keys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_MANUAL);
    }

    @SubscribeEvent
    static void payloads(RegisterClientPayloadHandlersEvent event) {
        event.register(GuideStatePayload.TYPE, (payload, context) -> {
            MILESTONES.clear();
            MILESTONES.addAll(payload.milestones());
            hintsOff = payload.hintsOff();
        });
        event.register(HintPayload.TYPE, (payload, context) -> {
            Hint hint = Hint.byId(payload.hint());
            if (hint != null) {
                pendingChapter = hint.chapter();
                Minecraft.getInstance().getToastManager().addToast(new HintToast(hint));
            }
        });
        event.register(RecipeReplyPayload.TYPE, (payload, context) -> RECIPES.put(payload.item(), payload.displays()));
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_MANUAL.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                open(null);
            }
        }
    }

    @SubscribeEvent
    static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        MILESTONES.clear();
        RECIPES.clear();
        REQUESTED.clear();
        pendingChapter = null;
        hintsOff = false;
    }

    public static void open(Hint.@Nullable Chapter chapter) {
        Hint.Chapter target = chapter != null ? chapter : pendingChapter != null ? pendingChapter : lastChapter;
        int page = chapter == null && pendingChapter == null ? LAST_PAGE.getOrDefault(target, 0) : 0;
        pendingChapter = null;
        Minecraft.getInstance().setScreen(new ManualScreen(target, page));
    }

    static void remember(Hint.Chapter chapter, int page) {
        lastChapter = chapter;
        LAST_PAGE.put(chapter, page);
    }

    public static boolean done(String milestone) {
        return MILESTONES.contains(milestone);
    }

    public static boolean hintsOff() {
        return hintsOff;
    }

    public static void setHintsOff(boolean off) {
        hintsOff = off;
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPacketDistributor.sendToServer(new GuideSettingPayload(off));
        }
    }

    public static @Nullable Component keyName() {
        if (OPEN_MANUAL.isUnbound()) {
            return null;
        }
        String id = OPEN_MANUAL.getKey().getName();
        String prefix = "key.keyboard.";
        String label = id.startsWith(prefix) && id.length() == prefix.length() + 1
                ? id.substring(prefix.length()).toUpperCase(java.util.Locale.ROOT)
                : OPEN_MANUAL.getTranslatedKeyMessage().getString();
        return Component.literal("[" + label + "]");
    }

    public static @Nullable List<RecipeDisplay> recipes(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        List<RecipeDisplay> known = RECIPES.get(id);
        if (known != null) {
            return known;
        }
        if (Minecraft.getInstance().getConnection() != null && REQUESTED.add(id)) {
            ClientPacketDistributor.sendToServer(new RecipeRequestPayload(id));
        }
        return null;
    }
}
