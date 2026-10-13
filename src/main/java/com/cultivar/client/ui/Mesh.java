package com.cultivar.client.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

record Mesh(RenderPipeline pipeline, float[] xy, int[] colors, int vertices, @Nullable ScreenRectangle scissorArea,
            ScreenRectangle bounds) implements GuiElementRenderState {
    private static final Matrix3x2fc IDENTITY = new Matrix3x2f();

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0; i < vertices; i++) {
            consumer.addVertexWith2DPose(IDENTITY, xy[i * 2], xy[i * 2 + 1]).setColor(colors[i]);
        }
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }
}
