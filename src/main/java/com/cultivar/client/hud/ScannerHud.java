package com.cultivar.client.hud;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.GenomeArt;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.Catalog;
import com.cultivar.data.Nutrient;
import com.cultivar.data.PhBand;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class ScannerHud {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Cultivar.MODID, "field_scanner");
    private static final Anim.Smooth LOCK = new Anim.Smooth(10f).from(0f);
    private static final Anim.Smooth SHOW = new Anim.Smooth(8f).from(0f);
    private static long lastFrame;
    private static @Nullable BlockPos lastTarget;
    private static long lockedAt;

    private ScannerHud() {
    }

    @SubscribeEvent
    static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ID, ScannerHud::render);
    }

    private enum Tool { NONE, LENS, PROBE }

    private static Tool tool(Minecraft mc) {
        var player = mc.player;
        if (player == null) {
            return Tool.NONE;
        }
        if (player.getMainHandItem().is(ModItems.HAND_LENS.get()) || player.getOffhandItem().is(ModItems.HAND_LENS.get())) {
            return Tool.LENS;
        }
        if (player.getMainHandItem().is(ModItems.SOIL_PROBE.get()) || player.getOffhandItem().is(ModItems.SOIL_PROBE.get())) {
            return Tool.PROBE;
        }
        return Tool.NONE;
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();
        float dt = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        Tool tool = tool(mc);
        Level level = mc.level;
        if (tool == Tool.NONE || level == null || mc.options.hideGui || mc.screen != null) {
            SHOW.update(0f, dt);
            LOCK.update(0f, dt);
            return;
        }
        BlockPos target = null;
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            if (tool == Tool.LENS && level.getBlockEntity(pos) instanceof CropBlockEntity) {
                target = pos;
            } else if (tool == Tool.PROBE) {
                if (level.getBlockEntity(pos) instanceof ManagedSoilBlockEntity) {
                    target = pos;
                } else if (level.getBlockEntity(pos.below()) instanceof ManagedSoilBlockEntity) {
                    target = pos.below();
                }
            }
        }
        if (target != null && !target.equals(lastTarget)) {
            lockedAt = now;
        }
        lastTarget = target;
        float lock = LOCK.update(target != null ? 1f : 0f, dt);
        float show = SHOW.update(1f, dt);
        float time = Anim.now();
        Canvas c = new Canvas(g);
        float cx = g.guiWidth() / 2f;
        float cy = g.guiHeight() / 2f;
        int accent = tool == Tool.LENS ? Theme.ACCENT_LAB : Theme.ACCENT_COMPOST;
        reticle(c, cx, cy, lock, show, accent, time);
        if (target != null) {
            float age = (now - lockedAt) / 1000f;
            c.alpha(Anim.easeOutCubic(age / 0.25f) * show);
            float px = cx + 26 + (1f - Anim.easeOutCubic(age / 0.25f)) * 10f;
            if (tool == Tool.LENS && level.getBlockEntity(target) instanceof CropBlockEntity crop) {
                cropPanel(c, mc.font, level, crop, level.getBlockState(target), px, cy - 44, accent, time, age);
            } else if (tool == Tool.PROBE && level.getBlockEntity(target) instanceof ManagedSoilBlockEntity soil) {
                CropBlockEntity above = level.getBlockEntity(target.above()) instanceof CropBlockEntity crop ? crop : null;
                soilPanel(c, mc.font, level, soil, above, px, cy - 34, accent, time, age);
            }
            c.alpha(1f);
        }
        c.flush();
    }

    private static void reticle(Canvas c, float cx, float cy, float lock, float show, int accent, float time) {
        float spread = 15f - 7f * lock;
        float len = 3f + 2f * lock;
        int col = Theme.fade(Theme.lerp(Theme.TEXT_DIM, accent, lock), 0.35f + 0.55f * show);
        for (int i = 0; i < 4; i++) {
            float sx = i % 2 == 0 ? -1 : 1;
            float sy = i < 2 ? -1 : 1;
            float x = cx + sx * spread;
            float y = cy + sy * spread;
            c.rect(sx < 0 ? x : x - len, y - 0.5f, len, 1f, col);
            c.rect(x - 0.5f, sy < 0 ? y : y - len, 1f, len, col);
        }
        if (lock > 0.05f) {
            float r = spread + 4;
            float a0 = time * 1.6f;
            for (int i = 0; i < 3; i++) {
                float a = a0 + i * (float) (Math.PI * 2 / 3);
                c.arc(cx, cy, r - 0.5f, r + 0.5f, a, a + 0.9f, Theme.fade(accent, 0.6f * lock), Theme.fade(accent, 0.6f * lock));
            }
            c.bloom(cx, cy, 10, Theme.fade(accent, 0.12f * lock));
        }
    }

    private static void plate(Canvas c, float x, float y, float w, float h, int accent) {
        c.shadowRect(x, y, w, h, 5, 0x70000000);
        c.round(x, y, w, h, 3, 0xD00B151A);
        c.rectV(x, y, w, 12, Theme.fade(Theme.shade(accent, 0.4f), 0.55f), 0x00000000);
        c.frame(x, y, w, h, 0.7f, Theme.fade(accent, 0.6f));
        Instruments.brackets(c, x - 1.5f, y - 1.5f, w + 3, h + 3, 4, 1f, accent);
        c.rectH(x - 12, y + 10, 11, 0.7f, Theme.fade(accent, 0f), Theme.fade(accent, 0.6f));
    }

    private static void cropPanel(Canvas c, Font font, Level level, CropBlockEntity crop, BlockState state, float x, float y, int accent,
                                  float time, float age) {
        Catalog catalog = Catalog.get(level.registryAccess());
        Genome genome = crop.genome();
        boolean reveal = CultivarConfig.HAND_LENS_REVEALS_GENOTYPE.get();
        float w = 142;
        float h = genome != null ? 131 : 58;
        plate(c, x, y, w, h, accent);
        int stage = state.getBlock() instanceof CultivarCropBlock ? CultivarCropBlock.stage(state) : 0;
        c.text(font, GeneText.species(crop.speciesKey()), x + 5, y + 2.5f, Theme.TEXT);
        c.right(font, Texts.stage(stage), x + w - 5, y + 3.5f, 0.6f, Theme.TEXT_DIM);
        for (int i = 0; i <= 6; i++) {
            float sx = x + 5 + i * 19;
            boolean done = i <= stage;
            int col = i == CultivarCropBlock.FLOWERING ? Theme.WARN : accent;
            c.rect(sx, y + 14, 16, 2.5f, done ? Theme.fade(col, 0.9f) : 0x30FFFFFF);
            if (i == stage) {
                c.bloom(sx + 8, y + 15, 7, Theme.fade(col, 0.5f + 0.3f * Anim.pulse(time, 1.5f)));
            }
        }
        float chipX = x + 5;
        float chipY = y + 20;
        if (stage == CultivarCropBlock.FLOWERING) {
            chipX += Instruments.chip(c, font, Texts.tr(Texts.UI_HUD_FLOWERING), chipX, chipY, Theme.WARN, false) + 2;
        }
        if (crop.isPollinated()) {
            chipX += Instruments.chip(c, font, Texts.tr(Texts.UI_HUD_POLLINATED), chipX, chipY, Theme.GOOD, true) + 2;
        }
        if (crop.isBagged()) {
            chipX += Instruments.chip(c, font, Texts.tr(Texts.UI_HUD_BAGGED), chipX, chipY, Theme.INFO, false) + 2;
        }
        if (crop.isGrafted()) {
            chipX += Instruments.chip(c, font, Texts.tr(Texts.UI_HUD_GRAFTED), chipX, chipY, Theme.ACCENT_CARD, false) + 2;
        }
        if (crop.isWild()) {
            chipX += Instruments.chip(c, font, Texts.tr(Texts.UI_BADGE_WILD), chipX, chipY, Theme.WILD, false) + 2;
        }
        if (crop.isBlighted()) {
            Component strain = crop.strain() != null ? GeneText.strain(crop.strain()) : Texts.tr(Texts.UNKNOWN);
            Instruments.chip(c, font, Texts.tr(Texts.UI_HUD_BLIGHTED, strain), chipX, chipY,
                    Theme.fade(Theme.BAD, 0.7f + 0.3f * Anim.pulse(time, 2f)), true);
        }
        if (genome == null) {
            c.text(font, Texts.tr(Texts.UI_HUD_UNSAMPLED), x + 5, y + 32, 0.6f, Theme.TEXT_DIM);
            c.text(font, Texts.tr(Texts.UI_HUD_UNSAMPLED_HINT), x + 5, y + 40, 0.5f, Theme.TEXT_MUTED);
            return;
        }
        Phenotype phenotype = crop.phenotype();
        float ty = y + 31;
        Instruments.section(c, font, Texts.tr(reveal ? Texts.UI_HUD_GENOTYPE : Texts.UI_HUD_TRAITS), x + 5, ty, w - 10, accent);
        for (Gene gene : Gene.VALUES) {
            float ry = ty + 8 + gene.ordinal() * 10;
            float reveal01 = Anim.clamp01((age - 0.1f - gene.ordinal() * 0.04f) / 0.25f);
            int gc = catalog.gene(gene).color() | 0xFF000000;
            float value = phenotype.get(gene);
            float fav = catalog.gene(gene).favourability(value);
            c.text(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), x + 5, ry + 1, 0.55f, gc);
            Instruments.bar(c, x + 22, ry + 2, 58, 3, fav * reveal01, gc, time);
            c.text(font, Component.literal(gene.format(value)), x + 84, ry + 0.5f, 0.6f, Theme.quality(fav));
            if (reveal) {
                int a = genome.first(gene);
                int b = genome.second(gene);
                c.rect(x + w - 19, ry + 1, 6, 5, GenomeArt.alleleColor(catalog, gene, a));
                c.rect(x + w - 12, ry + 1, 6, 5, GenomeArt.alleleColor(catalog, gene, b));
            }
        }
        Grade ceiling = GradeLogic.ceiling(phenotype, catalog);
        float gy = ty + 8 + Gene.COUNT * 10 + 1;
        c.text(font, Texts.tr(Texts.UI_HUD_CEILING), x + 5, gy + 1, 0.55f, Theme.TEXT_DIM);
        c.right(font, Texts.tr(ceiling.translationKey()), x + w - 5, gy, 0.7f, ceiling.color());
    }

    private static void soilPanel(Canvas c, Font font, Level level, ManagedSoilBlockEntity soil, @Nullable CropBlockEntity crop, float x,
                                  float y, int accent, float time, float age) {
        float w = 132;
        float h = 74;
        plate(c, x, y, w, h, accent);
        c.text(font, Texts.tr(Texts.UI_HUD_SOIL), x + 5, y + 2.5f, Theme.TEXT);
        Icons.draw(c, Icon.GRID, x + w - 9, y + 6, 7, accent);
        float k = Anim.easeOutCubic(age / 0.5f);
        int[] colors = {0xFF6FD06F, 0xFFE07A5A, 0xFFB08AE0};
        String[] labels = {"N", "P", "K"};
        Nutrient[] nutrients = {Nutrient.NITROGEN, Nutrient.PHOSPHORUS, Nutrient.POTASSIUM};
        for (int i = 0; i < 3; i++) {
            float ry = y + 15 + i * 9;
            float v = soil.get(nutrients[i]);
            c.text(font, Component.literal(labels[i]), x + 5, ry, 0.65f, colors[i]);
            Instruments.bar(c, x + 14, ry + 1, 86, 3.5f, v / 100f * k, colors[i], time);
            c.right(font, Component.literal(String.valueOf(Math.round(v))), x + w - 5, ry, 0.65f, Theme.TEXT_DIM);
        }
        float sx = x + 14;
        float sw = w - 19 - 14 + 5;
        float sy = y + 45;
        for (int i = 0; i < 22; i++) {
            float t0 = i / 22f;
            int col = Theme.hsv(0.0f + 0.75f * t0, 0.7f, 0.85f);
            c.rect(sx + sw * t0, sy, sw / 22f + 0.2f, 4, col);
        }
        if (crop != null) {
            PhBand band = Catalog.get(level.registryAccess()).phBand(crop.speciesKey());
            float b0 = (band.min() - 3.5f) / 5.5f;
            float b1 = (band.max() - 3.5f) / 5.5f;
            c.frame(sx + sw * b0, sy - 1.5f, sw * (b1 - b0), 7, 0.8f, 0xFFFFFFFF);
        }
        float ph = soil.ph();
        float mx = sx + sw * Anim.clamp01((ph - 3.5f) / 5.5f);
        c.tri(mx, sy + 5, 0xFFFFFFFF, mx - 2.5f, sy + 9, 0xFFFFFFFF, mx + 2.5f, sy + 9, 0xFFFFFFFF);
        c.text(font, Component.literal("pH"), x + 5, sy, 0.6f, Theme.TEXT_DIM);
        c.centered(font, Component.literal(String.format(Locale.ROOT, "%.1f", ph)), mx, sy + 10, 0.55f, Theme.TEXT);
        int moisture = soil.moisture();
        float my = y + 62;
        c.text(font, Texts.tr(Texts.UI_HUD_MOISTURE), x + 5, my + 1, 0.5f, Theme.TEXT_DIM);
        for (int i = 0; i < 8; i++) {
            Icons.draw(c, Icon.DROP, x + 46 + i * 9, my + 3, 7, i < moisture ? Theme.INFO : 0x30FFFFFF);
        }
        if (soil.isExhausted()) {
            c.text(font, Texts.tr(Texts.UI_HUD_EXHAUSTED), x + 5, y + h + 3, 0.6f, Theme.fade(Theme.BAD, 0.7f + 0.3f * Anim.pulse(time, 2f)));
        }
    }
}
