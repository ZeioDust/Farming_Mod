package com.cultivar.client.render;

import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.cultivar.util.Texts;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ClimateControllerRenderer implements BlockEntityRenderer<ClimateControllerBlockEntity, ClimateControllerRenderer.State> {
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public final List<Component> lines = new ArrayList<>();
        public boolean sealed;
    }

    public ClimateControllerRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ClimateControllerBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.lines.clear();
        state.sealed = be.isSealed();
        state.lines.add(state.sealed ? Texts.tr(Texts.READOUT_SEALED, be.volume()) : Texts.tr(Texts.READOUT_UNSEALED));
        state.lines.add(Texts.tr(Texts.READOUT_TEMP, String.format(Locale.ROOT, "%.2f", be.temperature()),
                String.format(Locale.ROOT, "%.2f", be.setpoint())));
        state.lines.add(Texts.tr(Texts.READOUT_HUMIDITY, Math.round(be.humidity() * 100), be.lightFloor()));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        FloatingText.begin(poseStack, camera, 1.55f);
        FloatingText.lines(collector, poseStack, font, state.lines, state.lightCoords, state.sealed ? 0xFFB0F0FF : 0xFFFFB0A0);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 16;
    }
}
