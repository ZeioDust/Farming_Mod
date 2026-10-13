package com.cultivar.client.render;

import com.cultivar.Cultivar;
import com.cultivar.entity.WaterJetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WaterJetRenderer extends EntityRenderer<WaterJetEntity, WaterJetRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityTranslucent(
            Identifier.fromNamespaceAndPath(Cultivar.MODID, "textures/entity/water_jet.png"));

    public static class State extends EntityRenderState {
        public float yaw;
        public float pitch;
    }

    public WaterJetRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WaterJetEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Vec3 v = entity.getDeltaMovement();
        state.yaw = (float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG);
        state.pitch = (float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.pitch));
        int light = state.lightCoords;
        int argb = 0xB0FFFFFF;
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            float w = 0.05f;
            float back = -0.5f;
            buffer.addVertex(pose, -w, 0, back).setColor(argb).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, w, 0, back).setColor(argb).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, w, 0, 0).setColor(argb).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, -w, 0, 0).setColor(argb).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, 0, -w, back).setColor(argb).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            buffer.addVertex(pose, 0, w, back).setColor(argb).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            buffer.addVertex(pose, 0, w, 0).setColor(argb).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
            buffer.addVertex(pose, 0, -w, 0).setColor(argb).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
