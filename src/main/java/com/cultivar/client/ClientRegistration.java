package com.cultivar.client;

import com.cultivar.Cultivar;
import com.cultivar.client.model.CartModel;
import com.cultivar.client.model.DroneModel;
import com.cultivar.client.model.RoverModel;
import com.cultivar.client.particle.CultivarParticle;
import com.cultivar.client.render.ClimateControllerRenderer;
import com.cultivar.client.render.EffectRenderer;
import com.cultivar.client.render.FallingProduceRenderer;
import com.cultivar.client.render.MachineEntityRenderer;
import com.cultivar.client.render.MachineProgressRenderer;
import com.cultivar.client.render.SeedBankRenderer;
import com.cultivar.client.render.TrialMarkerRenderer;
import com.cultivar.client.render.WaterJetRenderer;
import com.cultivar.client.screen.ClimateControllerScreen;
import com.cultivar.client.screen.CompostBinScreen;
import com.cultivar.client.screen.ContractBoardScreen;
import com.cultivar.client.screen.GeneEditorScreen;
import com.cultivar.client.screen.GenomeCardScreen;
import com.cultivar.client.screen.GenomeLabScreen;
import com.cultivar.client.screen.MutagenChamberScreen;
import com.cultivar.client.screen.ProcessingStationScreen;
import com.cultivar.client.screen.ProduceExchangeScreen;
import com.cultivar.client.screen.SeedBankScreen;
import com.cultivar.client.screen.ShowBenchScreen;
import com.cultivar.client.ui.ClientGenomeTooltip;
import com.cultivar.component.GenomeTooltip;
import com.cultivar.item.CultivarTooltip;
import com.cultivar.menu.SeedBankMenu;
import com.cultivar.network.MarketQuotesPayload;
import com.cultivar.network.SeedBankLinesPayload;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModMenus;
import com.cultivar.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class ClientRegistration {
    private ClientRegistration() {
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, "textures/entity/" + name + ".png");
    }

    @SubscribeEvent
    static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DroneModel.LAYER, DroneModel::createLayer);
        event.registerLayerDefinition(CartModel.LAYER, CartModel::createLayer);
        event.registerLayerDefinition(RoverModel.LAYER, RoverModel::createLayer);
    }

    @SubscribeEvent
    static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.POLLINATOR_DRONE.get(),
                ctx -> new MachineEntityRenderer<>(ctx, new DroneModel(ctx.bakeLayer(DroneModel.LAYER)), texture("pollinator_drone"), 0.25f));
        event.registerEntityRenderer(ModEntities.HARVESTER_CART.get(),
                ctx -> new MachineEntityRenderer<>(ctx, new CartModel(ctx.bakeLayer(CartModel.LAYER)), texture("harvester_cart"), 0.5f));
        event.registerEntityRenderer(ModEntities.SOIL_PROBE_ROVER.get(),
                ctx -> new MachineEntityRenderer<>(ctx, new RoverModel(ctx.bakeLayer(RoverModel.LAYER)), texture("soil_probe_rover"), 0.35f));
        event.registerEntityRenderer(ModEntities.SPORE_DRIFT.get(), ctx -> new EffectRenderer<>(ctx, texture("spore_drift"), 0.6f, 3));
        event.registerEntityRenderer(ModEntities.MIST_CLOUD.get(), ctx -> new EffectRenderer<>(ctx, texture("mist_cloud"), 1.1f, 4));
        event.registerEntityRenderer(ModEntities.WATER_JET.get(), WaterJetRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLING_PRODUCE.get(), FallingProduceRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.TRIAL_MARKER.get(), TrialMarkerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SEED_BANK.get(), SeedBankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CLIMATE_CONTROLLER.get(), ClimateControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GENOME_LAB.get(), ctx -> new MachineProgressRenderer<>());
        event.registerBlockEntityRenderer(ModBlockEntities.MUTAGEN_CHAMBER.get(), ctx -> new MachineProgressRenderer<>());
        event.registerBlockEntityRenderer(ModBlockEntities.THERMAL_SHOCK_UNIT.get(), ctx -> new MachineProgressRenderer<>());
        event.registerBlockEntityRenderer(ModBlockEntities.COMPOST_BIN.get(), ctx -> new MachineProgressRenderer<>());
    }

    @SubscribeEvent
    static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.POLLEN_MOTE.get(), sprites -> new CultivarParticle.Provider(sprites, CultivarParticle.Style.POLLEN));
        event.registerSpriteSet(ModParticles.SPORE_SPECK.get(), sprites -> new CultivarParticle.Provider(sprites, CultivarParticle.Style.SPORE));
        event.registerSpriteSet(ModParticles.WATER_DROPLET.get(), sprites -> new CultivarParticle.Provider(sprites, CultivarParticle.Style.WATER));
        event.registerSpriteSet(ModParticles.MIST_WISP.get(), sprites -> new CultivarParticle.Provider(sprites, CultivarParticle.Style.MIST));
        event.registerSpriteSet(ModParticles.SOIL_CRUMB.get(), sprites -> new CultivarParticle.Provider(sprites, CultivarParticle.Style.SOIL));
    }

    @SubscribeEvent
    static void screens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GENOME_LAB.get(), GenomeLabScreen::new);
        event.register(ModMenus.SEED_BANK.get(), SeedBankScreen::new);
        event.register(ModMenus.MUTAGEN_CHAMBER.get(), MutagenChamberScreen::new);
        event.register(ModMenus.CLIMATE_CONTROLLER.get(), ClimateControllerScreen::new);
        event.register(ModMenus.COMPOST_BIN.get(), CompostBinScreen::new);
        event.register(ModMenus.GENOME_CARD.get(), GenomeCardScreen::new);
        event.register(ModMenus.PRODUCE_EXCHANGE.get(), ProduceExchangeScreen::new);
        event.register(ModMenus.CONTRACT_BOARD.get(), ContractBoardScreen::new);
        event.register(ModMenus.PROCESSING_STATION.get(), ProcessingStationScreen::new);
        event.register(ModMenus.GENE_EDITOR.get(), GeneEditorScreen::new);
        event.register(ModMenus.SHOW_BENCH.get(), ShowBenchScreen::new);
    }

    @SubscribeEvent
    static void setup(FMLClientSetupEvent event) {
        CultivarTooltip.Keys.setShift(() -> Minecraft.getInstance().hasShiftDown());
    }

    @SubscribeEvent
    static void tooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(GenomeTooltip.class, ClientGenomeTooltip::new);
    }

    @SubscribeEvent
    static void payloads(RegisterClientPayloadHandlersEvent event) {
        event.register(SeedBankLinesPayload.TYPE, (payload, context) -> {
            var player = Minecraft.getInstance().player;
            if (player != null && player.containerMenu instanceof SeedBankMenu menu && menu.containerId == payload.containerId()) {
                menu.setClientLines(payload.lines(), payload.capacity());
            }
        });
        event.register(MarketQuotesPayload.TYPE, (payload, context) -> {
            ProduceExchangeScreen.DEMAND.clear();
            ProduceExchangeScreen.DEMAND.putAll(payload.demand());
        });
    }
}
