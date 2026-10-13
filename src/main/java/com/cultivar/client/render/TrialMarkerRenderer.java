package com.cultivar.client.render;

import com.cultivar.blockentity.TrialMarkerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TrialMarkerRenderer implements BlockEntityRenderer<TrialMarkerBlockEntity, TrialMarkerRenderer.State> {
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public String label = "";
        public int number;
    }

    public TrialMarkerRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrialMarkerBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.label = be.label();
        state.number = be.number();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.number <= 0) {
            return;
        }
        FloatingText.begin(poseStack, camera, 1.35f);
        FloatingText.lines(collector, poseStack, font, List.of(Component.literal(state.label + " #" + state.number)), state.lightCoords, 0xFFFFF4C0);
        poseStack.popPose();
    }
}
