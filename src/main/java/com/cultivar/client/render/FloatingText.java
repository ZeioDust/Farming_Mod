package com.cultivar.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

final class FloatingText {
    static final float SCALE = 0.025f;

    private FloatingText() {
    }

    static void begin(PoseStack poseStack, CameraRenderState camera, float height) {
        poseStack.pushPose();
        poseStack.translate(0.5f, height, 0.5f);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(SCALE, -SCALE, SCALE);
    }

    static void lines(SubmitNodeCollector collector, PoseStack poseStack, Font font, List<Component> lines, int light, int color) {
        int y = -lines.size() * 10;
        for (Component line : lines) {
            FormattedCharSequence text = line.getVisualOrderText();
            collector.order(1).submitText(poseStack, -font.width(text) / 2f, y, text, false, Font.DisplayMode.NORMAL, light, color, 0x50000000, 0);
            y += 10;
        }
    }

    static void bar(SubmitNodeCollector collector, PoseStack poseStack, float progress, int light, int fillColor) {
        float x0 = -20;
        float x1 = 20;
        float y0 = 2;
        float y1 = 6;
        float fx = x0 + (x1 - x0) * Math.max(0f, Math.min(1f, progress));
        collector.submitCustomGeometry(poseStack, RenderTypes.textBackground(), (pose, buffer) -> {
            quad(buffer, pose, x0, y0, x1, y1, 0.0f, 0xC0202020, light);
            quad(buffer, pose, x0, y0, fx, y1, -0.01f, fillColor, light);
        });
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float x1, float y1, float z, int color, int light) {
        buffer.addVertex(pose, x0, y0, z).setColor(color).setLight(light);
        buffer.addVertex(pose, x0, y1, z).setColor(color).setLight(light);
        buffer.addVertex(pose, x1, y1, z).setColor(color).setLight(light);
        buffer.addVertex(pose, x1, y0, z).setColor(color).setLight(light);
    }
}
