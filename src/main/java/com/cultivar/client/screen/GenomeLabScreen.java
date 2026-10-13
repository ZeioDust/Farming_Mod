package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.GenomeLabBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.GenomeArt;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.component.CardData;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.menu.GenomeLabMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GenomeLabScreen extends CultivarContainerScreen<GenomeLabMenu> {
    private static final int VX = 38;
    private static final int VY = 22;
    private static final int VW = 156;
    private static final int VH = 70;
    private final Anim.Smooth progress = new Anim.Smooth(10f);
    private final Anim.Smooth spin = new Anim.Smooth(3f).from(0.4f);
    private float phase;

    public GenomeLabScreen(GenomeLabMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GenomeLabMenu.WIDTH, GenomeLabMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_LAB;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.HELIX;
    }

    @Override
    protected boolean working() {
        return menu.progress() > 0f;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return switch (slot.getContainerSlot()) {
            case GenomeLabBlockEntity.SLOT_SAMPLE -> Icon.SEED;
            case GenomeLabBlockEntity.SLOT_PAPER -> Icon.PAPER;
            case GenomeLabBlockEntity.SLOT_OUTPUT -> Icon.HELIX;
            default -> null;
        };
    }

    private Catalog catalog() {
        return minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults();
    }

    private enum State { INSERT, PAPER, COLLECT, SEQUENCING }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        Catalog catalog = catalog();
        ItemStack sample = menu.getSlot(GenomeLabBlockEntity.SLOT_SAMPLE).getItem();
        ItemStack paper = menu.getSlot(GenomeLabBlockEntity.SLOT_PAPER).getItem();
        ItemStack output = menu.getSlot(GenomeLabBlockEntity.SLOT_OUTPUT).getItem();
        CardData card = output.get(ModDataComponents.CARD.get());
        boolean strainSample = sample.is(ModItems.BLIGHT_SAMPLE.get());
        State state = sample.isEmpty() ? State.INSERT : paper.isEmpty() ? State.PAPER : !output.isEmpty() ? State.COLLECT : State.SEQUENCING;
        float p = progress.update(state == State.SEQUENCING ? menu.progress() : 0f, dt);
        boolean active = state == State.SEQUENCING;
        phase += dt * spin.update(active ? 2.6f : 0.5f, dt);

        Genome genome;
        float reveal;
        if (card != null && card.genome().isPresent() && sample.isEmpty()) {
            genome = card.genome().get();
            reveal = 1f;
        } else if (state == State.COLLECT && card != null && card.genome().isPresent()) {
            genome = card.genome().get();
            reveal = 1f;
        } else {
            genome = sample.get(ModDataComponents.GENOME.get());
            reveal = active ? p : 0f;
        }

        float x0 = leftPos + VX;
        float y0 = topPos + VY;
        Instruments.well(c, x0, y0, VW, VH);
        c.scissor((int) x0 + 1, (int) y0 + 1, (int) (x0 + VW - 1), (int) (y0 + VH - 1));
        for (float gx = x0 + 6; gx < x0 + VW; gx += 12) {
            c.rect(gx, y0, 0.5f, VH, 0x0F3FE0C8);
        }
        c.rect(x0, y0 + VH / 2f, VW, 0.5f, 0x183FE0C8);
        float cy = y0 + 30;
        if (strainSample && state != State.INSERT) {
            drawAssay(c, x0 + VW / 2f, cy, active ? p : 0f);
        } else {
            GenomeArt.helix(c, x0 + 6, x0 + VW - 6, cy, 15, phase, genome, catalog, reveal, active, Theme.ACCENT_LAB, time);
        }
        if (active) {
            float bx = x0 + 6 + (VW - 12) * p;
            c.glow(true);
            c.rectH(bx - 14, y0 + 2, 14, VH - 4, Theme.withAlpha(0x3FE0C8, 0f), Theme.withAlpha(0x3FE0C8, 0.28f));
            c.rect(bx - 0.6f, y0 + 2, 1.2f, VH - 4, Theme.withAlpha(0xFFFFFF, 0.85f));
            c.glow(false);
            fx.emit(18f, dt, () -> {
                var r = fx.random();
                fx.spawn(UiParticles.Kind.GLINT, bx + (r.nextFloat() - 0.5f) * 3f, cy + (r.nextFloat() - 0.5f) * 34f,
                        -8f - r.nextFloat() * 10f, (r.nextFloat() - 0.5f) * 6f, 0.5f + r.nextFloat() * 0.4f, 1.6f, Theme.ACCENT_LAB);
            });
        }
        fx.draw(c);
        Instruments.scanlines(c, x0, y0, VW, VH, time * 1.7f);
        c.endScissor();
        Instruments.brackets(c, x0 - 1, y0 - 1, VW + 2, VH + 2, 6, 1f, Theme.fade(Theme.ACCENT_LAB, 0.9f));

        Component status = switch (state) {
            case INSERT -> Texts.tr(Texts.UI_LAB_INSERT);
            case PAPER -> Texts.tr(Texts.UI_LAB_PAPER);
            case COLLECT -> Texts.tr(Texts.UI_LAB_COMPLETE);
            case SEQUENCING -> Texts.tr(strainSample ? Texts.UI_LAB_ASSAY : Texts.UI_LAB_SEQUENCING);
        };
        int statusColor = switch (state) {
            case INSERT -> Theme.fade(Theme.TEXT_DIM, 0.55f + 0.45f * Anim.pulse(time, 0.8f));
            case PAPER -> Theme.WARN;
            case COLLECT -> Theme.GOOD;
            case SEQUENCING -> Theme.ACCENT_LAB;
        };
        c.text(font, status, x0 + 4, y0 + 3, Instruments.MICRO, statusColor);
        if (active) {
            c.right(font, Component.literal(String.format(Locale.ROOT, "%3d%%", Math.round(p * 100))), x0 + VW - 4, y0 + 3, Instruments.MICRO,
                    Theme.ACCENT_LAB);
        }

        float ry = y0 + VH - 15;
        float cw = (VW - 8) / (float) Gene.COUNT;
        for (Gene gene : Gene.VALUES) {
            float gx = x0 + 4 + gene.ordinal() * cw;
            boolean decoded = genome != null && (reveal >= (gene.ordinal() + 1f) / Gene.COUNT) && !strainSample;
            c.rect(gx + 0.5f, ry, cw - 1, 12, decoded ? 0x30000000 : 0x18000000);
            int geneColor = catalog.gene(gene).color() | 0xFF000000;
            c.rect(gx + 0.5f, ry, cw - 1, 0.8f, Theme.fade(geneColor, decoded ? 0.9f : 0.3f));
            c.centered(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), gx + cw / 2f, ry + 1.5f, 0.5f,
                    decoded ? Theme.TEXT_DIM : Theme.TEXT_MUTED);
            String code;
            if (decoded) {
                code = Integer.toHexString(genome.first(gene)).toUpperCase(Locale.ROOT) + "/" + Integer.toHexString(genome.second(gene)).toUpperCase(Locale.ROOT);
            } else if (active) {
                code = Integer.toHexString((int) (Anim.hash((int) (time * 14) + gene.ordinal() * 17) * 16)).toUpperCase(Locale.ROOT)
                        + "/" + Integer.toHexString((int) (Anim.hash((int) (time * 14) + gene.ordinal() * 31 + 3) * 16)).toUpperCase(Locale.ROOT);
            } else {
                code = "-/-";
            }
            c.centered(font, Component.literal(code), gx + cw / 2f, ry + 6.5f, 0.6f, decoded ? geneColor : Theme.fade(Theme.ACCENT_LAB, 0.35f));
            if (decoded && mouseX >= gx && mouseX < gx + cw && mouseY >= ry && mouseY < ry + 12) {
                var table = catalog.table();
                tooltip(c.g(), List.of(GeneText.gene(gene), GeneText.allele(table.get(gene, genome.first(gene))),
                        GeneText.allele(table.get(gene, genome.second(gene)))), mouseX, mouseY);
            }
        }

        float by = y0 + VH + 6;
        Instruments.bar(c, x0, by, VW, 4, p, Theme.ACCENT_LAB, time);
        int total = CultivarConfig.LAB_ANALYZE_TICKS.get();
        float seconds = active ? (1f - p) * total / 20f : total / 20f;
        c.text(font, Texts.tr(Texts.UI_ETA, String.format(Locale.ROOT, "%.1f", seconds)), x0, by + 7, Instruments.MICRO, Theme.TEXT_DIM);
        c.right(font, Texts.tr(Texts.UI_PAPER_COST), x0 + VW, by + 7, Instruments.MICRO, Theme.TEXT_MUTED);

        float flow = time * (active ? 22f : 4f);
        int flowColor = Theme.fade(Theme.ACCENT_LAB, active ? 0.8f : 0.3f);
        float sx = leftPos + 14 + 17;
        c.dashed(sx + 1, topPos + 42 + 8, x0 - 1, topPos + 42 + 8, 1f, 3, 2, -flow, flowColor);
        c.dashed(sx + 1, topPos + 82 + 8, x0 + 10, topPos + 82 + 8, 1f, 3, 2, -flow, Theme.fade(flowColor, 0.7f));
        c.dashed(x0 + 10, topPos + 82 + 8, x0 + 10, y0 + VH + 1, 1f, 3, 2, -flow, Theme.fade(flowColor, 0.7f));
        c.dashed(x0 + VW + 1, topPos + 62 + 8, leftPos + 202 - 2, topPos + 62 + 8, 1f, 3, 2, -flow, flowColor);

        c.text(font, Texts.tr(Texts.LABEL_SAMPLE), leftPos + 13, topPos + 32, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.LABEL_PAPER), leftPos + 13, topPos + 72, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.LABEL_CARD), leftPos + 201, topPos + 52, Instruments.MICRO, Theme.TEXT_DIM);
    }

    private void drawAssay(Canvas c, float cx, float cy, float p) {
        for (int ring = 0; ring < 3; ring++) {
            float r = 8 + ring * 7;
            c.ring(cx, cy, r, 0.6f, Theme.fade(Theme.ACCENT_BLIGHT, 0.25f + 0.1f * ring));
            int points = 6 + ring * 4;
            for (int i = 0; i < points; i++) {
                float a = (float) (Math.PI * 2 * i / points) + time * (0.6f - ring * 0.2f) * (1.2f - p);
                float jitter = (1f - p) * 2.5f * (Anim.hash(i * 7 + ring * 131 + (int) (time * 10)) - 0.5f);
                float px = cx + Canvas.cos(a) * (r + jitter);
                float py = cy + Canvas.sin(a) * (r + jitter);
                c.bloom(px, py, 2.6f, Theme.fade(Theme.ACCENT_BLIGHT, 0.6f));
                c.rect(px - 0.5f, py - 0.5f, 1, 1, Theme.ACCENT_BLIGHT);
            }
        }
        c.bloom(cx, cy, 6 + 4 * Anim.pulse(time, 1f), Theme.fade(Theme.ACCENT_BLIGHT, 0.5f));
    }
}
