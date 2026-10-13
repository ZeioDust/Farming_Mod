package com.cultivar.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class MachineEntityRenderer<T extends Entity> extends EntityRenderer<T, MachineRenderState> {
    private final EntityModel<MachineRenderState> model;
    private final Identifier texture;

    public MachineEntityRenderer(EntityRendererProvider.Context context, EntityModel<MachineRenderState> model, Identifier texture, float shadow) {
        super(context);
        this.model = model;
        this.texture = texture;
        this.shadowRadius = shadow;
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(T entity, MachineRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.moving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5;
        state.working = entity instanceof com.cultivar.entity.MachineEntity machine && machine.isWorking();
    }

    @Override
    public void submit(MachineRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.yRot));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        collector.submitModel(model, state, poseStack, texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
