package com.cultivar.client.render;

import com.cultivar.blockentity.ProgressMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MachineProgressRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, MachineProgressRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public float progress;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.progress = be instanceof ProgressMachine machine ? machine.progressFraction() : 0f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.progress <= 0f) {
            return;
        }
        FloatingText.begin(poseStack, camera, 1.3f);
        FloatingText.bar(collector, poseStack, state.progress, state.lightCoords, 0xFF5FD06F);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 16;
    }
}
