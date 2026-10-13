package com.cultivar.client.render;

import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.menu.SeedBankLine;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
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

public class SeedBankRenderer implements BlockEntityRenderer<SeedBankBlockEntity, SeedBankRenderer.State> {
    public static final int SHOWN = 3;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public final List<Component> lines = new ArrayList<>();
    }

    public SeedBankRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SeedBankBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.lines.clear();
        List<SeedBankLine> lines = be.lines();
        if (lines.isEmpty()) {
            return;
        }
        state.lines.add(Texts.tr(Texts.READOUT_LINES, lines.size()));
        for (int i = Math.max(0, lines.size() - SHOWN); i < lines.size(); i++) {
            state.lines.add(Component.literal(lines.get(i).name()));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.lines.isEmpty()) {
            return;
        }
        FloatingText.begin(poseStack, camera, 1.6f);
        FloatingText.lines(collector, poseStack, font, state.lines, state.lightCoords, 0xFFE8F0E0);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 24;
    }
}
