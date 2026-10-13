package com.cultivar.network;

import com.cultivar.Cultivar;
import com.cultivar.guide.Guide;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class CultivarNetwork {
    public static final String VERSION = "3";

    private CultivarNetwork() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(SeedBankLinesPayload.TYPE, SeedBankLinesPayload.STREAM_CODEC);
        registrar.playToClient(GuideStatePayload.TYPE, GuideStatePayload.STREAM_CODEC);
        registrar.playToClient(HintPayload.TYPE, HintPayload.STREAM_CODEC);
        registrar.playToClient(RecipeReplyPayload.TYPE, RecipeReplyPayload.STREAM_CODEC);
        registrar.playToClient(MarketQuotesPayload.TYPE, MarketQuotesPayload.STREAM_CODEC);
        registrar.playToServer(GuideSettingPayload.TYPE, GuideSettingPayload.STREAM_CODEC, Guide::handleSetting);
        registrar.playToServer(RecipeRequestPayload.TYPE, RecipeRequestPayload.STREAM_CODEC, Guide::handleRecipeRequest);
    }
}
