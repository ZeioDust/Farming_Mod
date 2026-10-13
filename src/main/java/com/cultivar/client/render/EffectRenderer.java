package com.cultivar.client.render;

import com.cultivar.entity.EffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.util.ARGB;

public class EffectRenderer<T extends EffectEntity> extends EntityRenderer<T, EffectRenderer.State> {
    private final RenderType renderType;
    private final float size;
    private final int layers;

    public static class State extends EntityRenderState {
        public int color;
        public float fade;
    }

    public EffectRenderer(EntityRendererProvider.Context context, Identifier texture, float size, int layers) {
        super(context);
        this.renderType = RenderTypes.entityTranslucent(texture);
        this.size = size;
        this.layers = layers;
        this.shadowRadius = 0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.color();
        float age = entity.age(partialTicks);
        state.fade = Math.min(1f, Math.min(age * 5f, (1f - age) * 3f));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, size * 0.4f, 0);
        poseStack.mulPose(camera.orientation);
        int baseAlpha = Math.round(70 * state.fade);
        for (int i = 0; i < layers; i++) {
            float scale = size * (1f - i * 0.18f);
            float spin = state.ageInTicks * (i % 2 == 0 ? 1.2f : -0.9f) + i * 40f;
            int argb = ARGB.color(baseAlpha, ARGB.red(state.color), ARGB.green(state.color), ARGB.blue(state.color));
            int light = state.lightCoords;
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            poseStack.translate(0, 0, i * 0.02f);
            collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> quad(buffer, pose, scale, argb, light));
            poseStack.popPose();
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float half, int argb, int light) {
        buffer.addVertex(pose, -half, -half, 0).setColor(argb).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        buffer.addVertex(pose, half, -half, 0).setColor(argb).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        buffer.addVertex(pose, half, half, 0).setColor(argb).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        buffer.addVertex(pose, -half, half, 0).setColor(argb).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
    }
}
