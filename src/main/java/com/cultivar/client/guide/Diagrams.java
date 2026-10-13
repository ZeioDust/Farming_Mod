package com.cultivar.client.guide;

import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.GenomeArt;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.PhBand;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.market.Grade;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class Diagrams {
    public enum Kind { HELIX, GENES, DOMINANCE, WIND, PUNNETT, PH, NPK, BLIGHT, GREENHOUSE, MUTATION, GRAFT, CANOPY, BACKCROSS, CARD, GRADES, MARKET,
        ATLAS }

    static final Genome SAMPLE = genome(1, 3, 1, 3, 1, 2, 2, 3, 2, 2, 0, 3, 2, 3, 1, 5);

    private Diagrams() {
    }

    private static Genome genome(int... pairs) {
        byte[] bytes = new byte[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            bytes[i] = (byte) pairs[i];
        }
        return Genome.of(bytes);
    }

    public static void draw(Kind kind, Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        Instruments.well(c, x, y, w, h);
        c.scissor((int) x + 1, (int) y + 1, (int) (x + w - 1), (int) (y + h - 1));
        switch (kind) {
            case HELIX -> helix(c, font, catalog, x, y, w, h, time);
            case GENES -> genes(c, font, catalog, x, y, w, h, time);
            case DOMINANCE -> dominance(c, font, catalog, x, y, w, h, time);
            case WIND -> wind(c, font, x, y, w, h, time);
            case PUNNETT -> punnett(c, font, catalog, x, y, w, h, time);
            case PH -> ph(c, font, catalog, x, y, w, h, time);
            case NPK -> npk(c, font, x, y, w, h, time);
            case BLIGHT -> blight(c, font, x, y, w, h, time);
            case GREENHOUSE -> greenhouse(c, font, x, y, w, h, time);
            case MUTATION -> mutation(c, font, catalog, x, y, w, h, time);
            case GRAFT -> graft(c, font, catalog, x, y, w, h, time);
            case CANOPY -> canopy(c, font, x, y, w, h, time);
            case BACKCROSS -> backcross(c, font, x, y, w, h, time);
            case CARD -> card(c, font, catalog, x, y, w, h, time);
            case GRADES -> grades(c, font, x, y, w, h, time);
            case MARKET -> market(c, font, x, y, w, h, time);
            case ATLAS -> atlas(c, font, catalog, x, y, w, h, time);
        }
        c.endScissor();
    }

    private static void helix(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        GenomeArt.helix(c, x + 10, x + w - 10, y + h / 2f, h * 0.32f, time * 1.3f, SAMPLE, catalog, 1f, false, Theme.ACCENT_LAB, time);
        Instruments.scanlines(c, x, y, w, h, time);
    }

    private static final String[] EFFECTS = {Texts.FX_YIELD, Texts.FX_GROWTH, Texts.FX_COLD, Texts.FX_DROUGHT, Texts.FX_LIGHT,
            Texts.FX_BLIGHT, Texts.FX_NUTRIENT, Texts.FX_MUTATION};

    private static void genes(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        float row = (h - 4) / Gene.COUNT;
        for (Gene gene : Gene.VALUES) {
            float ry = y + 2 + gene.ordinal() * row;
            int gc = catalog.gene(gene).color() | 0xFF000000;
            if (gene.ordinal() % 2 == 0) {
                c.rect(x + 1, ry, w - 2, row, 0x0CFFFFFF);
            }
            c.rect(x + 3, ry + 1.5f, 2, row - 3, gc);
            c.text(font, GeneText.gene(gene), x + 8, ry + row / 2f - 2.8f, 0.68f, Theme.TEXT);
            c.text(font, Texts.tr(EFFECTS[gene.ordinal()]), x + 76, ry + row / 2f - 2.3f, 0.58f, Theme.TEXT_DIM);
            float bx = x + w - 34;
            float k = 0.5f + 0.5f * Canvas.sin(time * 0.9f + gene.ordinal() * 0.8f);
            c.rectH(bx, ry + row / 2f - 1, 28, 2, Theme.fade(Theme.BAD, 0.5f), Theme.fade(Theme.GOOD, 0.8f));
            c.rect(bx + 28 * k - 0.6f, ry + row / 2f - 2.5f, 1.2f, 5, 0xFFFFFFFF);
        }
    }

    private static void capsule(Canvas c, Font font, Catalog catalog, Allele allele, float x, float y, float w, int state) {
        int col = GenomeArt.alleleColor(catalog, allele);
        float hh = 11;
        if (state == 0) {
            c.round(x, y, w, hh, 3, Theme.fade(col, 0.14f));
            c.dashed(x + 2, y, x + w - 2, y, 0.6f, 3, 2, 0, Theme.fade(col, 0.5f));
            c.dashed(x + 2, y + hh, x + w - 2, y + hh, 0.6f, 3, 2, 0, Theme.fade(col, 0.5f));
        } else {
            int edge = state == 2 ? Theme.INFO : col;
            c.haloRect(x, y, w, hh, 3, Theme.fade(edge, 0.25f));
            c.round(x, y, w, hh, 3, Theme.fade(Theme.shade(col, 0.55f), 0.95f));
            c.frame(x, y, w, hh, 0.7f, edge);
        }
        String name = font.plainSubstrByWidth(Component.translatable(allele.translationKey()).getString(), (int) ((w - 14) / 0.6f));
        c.text(font, Component.literal(name), x + 3, y + 3.3f, 0.6f, state == 0 ? Theme.TEXT_MUTED : Theme.TEXT);
        for (int i = 0; i < 3; i++) {
            c.disc(x + w - 9 + i * 3, y + hh / 2f, 0.9f, i < allele.dominance() ? Theme.TEXT : 0x30FFFFFF, i < allele.dominance() ? Theme.TEXT : 0x30FFFFFF);
        }
    }

    private static void dominance(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        AlleleTable table = catalog.table();
        float colW = w / 3f;
        int[][] pairs = {{Gene.YIELD.ordinal(), 1, 3}, {Gene.COLD_TOLERANCE.ordinal(), 0, 1}, {Gene.YIELD.ordinal(), 3, 3}};
        String[] labels = {Texts.DG_DOMINANT, Texts.DG_CODOMINANT, Texts.DG_RECESSIVE};
        for (int i = 0; i < 3; i++) {
            Gene gene = Gene.VALUES[pairs[i][0]];
            if (!table.has(gene, pairs[i][1]) || !table.has(gene, pairs[i][2])) {
                continue;
            }
            Genome g = SAMPLE.withGene(gene, Math.min(pairs[i][1], pairs[i][2]), Math.max(pairs[i][1], pairs[i][2]));
            Phenotype ph = Phenotype.resolve(g, table);
            Allele a = table.get(gene, pairs[i][1]);
            Allele b = table.get(gene, pairs[i][2]);
            boolean homo = a.code() == b.code();
            boolean both = ph.expression(gene) == Phenotype.EXPRESSED_BOTH;
            int sa = homo ? 1 : both ? 2 : a.dominance() >= b.dominance() ? 1 : 0;
            int sb = homo ? 1 : both ? 2 : b.dominance() > a.dominance() ? 1 : 0;
            float cx = x + colW * i + 6;
            float cw = colW - 12;
            c.centered(font, Texts.tr(labels[i]), cx + cw / 2f, y + 4, 0.6f, Theme.ACCENT_CARD);
            float pulse = Anim.pulse(time + i * 0.4f, 0.6f);
            capsule(c, font, catalog, a, cx, y + 13, cw, sa);
            capsule(c, font, catalog, b, cx, y + 27, cw, sb);
            float ay = y + 42;
            c.tri(cx + cw / 2f - 3, ay, Theme.fade(Theme.TEXT_DIM, 0.6f + 0.4f * pulse), cx + cw / 2f + 3, ay, Theme.fade(Theme.TEXT_DIM, 0.6f + 0.4f * pulse),
                    cx + cw / 2f, ay + 4, Theme.fade(Theme.TEXT_DIM, 0.6f + 0.4f * pulse));
            float value = ph.get(gene);
            c.centered(font, Component.literal(gene.format(value)), cx + cw / 2f, y + 49, 0.8f,
                    Theme.quality(catalog.gene(gene).favourability(value)));
        }
    }

    private static void punnett(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        AlleleTable table = catalog.table();
        Gene gene = Gene.YIELD;
        if (!table.has(gene, 1) || !table.has(gene, 3)) {
            return;
        }
        Allele dom = table.get(gene, 1);
        Allele rec = table.get(gene, 3);
        float cell = (h - 22) / 2f;
        float gx = x + 30;
        float gy = y + 16;
        float cycle = time % 7f;
        int domColor = GenomeArt.alleleColor(catalog, dom);
        int recColor = GenomeArt.alleleColor(catalog, rec);
        String domName = Component.translatable(dom.translationKey()).getString();
        String recName = Component.translatable(rec.translationKey()).getString();
        for (int i = 0; i < 2; i++) {
            Allele g = i == 0 ? dom : rec;
            int col = i == 0 ? domColor : recColor;
            c.round(gx + i * cell + 2, y + 3, cell - 4, 10, 3, Theme.fade(Theme.shade(col, 0.6f), 0.95f));
            c.centered(font, Component.literal(i == 0 ? domName : recName), gx + i * cell + cell / 2f, y + 5.5f, 0.55f, Theme.TEXT);
            c.round(x + 4, gy + i * cell + 2, 22, cell - 4, 3, Theme.fade(Theme.shade(col, 0.6f), 0.95f));
            c.centered(font, Component.literal(g == dom ? "A" : "a"), x + 15, gy + i * cell + cell / 2f - 3, Theme.TEXT);
        }
        int shown = 0;
        for (int r = 0; r < 2; r++) {
            for (int col = 0; col < 2; col++) {
                int idx = r * 2 + col;
                float k = Anim.easeOutBack((cycle - 0.4f - idx * 0.45f) / 0.4f);
                if (k <= 0) {
                    continue;
                }
                shown++;
                boolean recessive = r == 1 && col == 1;
                int q = recessive ? recColor : domColor;
                float cx = gx + col * cell + cell / 2f;
                float cy = gy + r * cell + cell / 2f;
                float half = (cell - 6) / 2f * Math.min(1f, k);
                c.round(cx - half, cy - half, half * 2, half * 2, 3, Theme.fade(Theme.shade(q, 0.4f), 0.9f));
                c.frame(cx - half, cy - half, half * 2, half * 2, 0.8f, q);
                if (k > 0.8f) {
                    String geno = (r == 0 ? "A" : "a") + (col == 0 ? "A" : "a");
                    c.centered(font, Component.literal(geno.equals("aA") ? "Aa" : geno), cx, cy - 6, Theme.TEXT);
                    String label = font.plainSubstrByWidth(recessive ? recName : domName, (int) ((cell - 8) / 0.5f));
                    c.centered(font, Component.literal(label), cx, cy + 4, 0.5f, recessive ? recColor : domColor);
                }
            }
        }
        float rx = gx + cell * 2 + 14;
        float rw = x + w - rx - 6;
        if (shown == 4) {
            float k = Anim.clamp01((cycle - 2.6f) / 0.8f);
            c.text(font, Texts.tr(Texts.DG_RATIO), rx, y + 14, 1.4f, Theme.fade(Theme.TEXT, k));
            Instruments.bar(c, rx, y + 34, rw, 4, 0.75f * k, domColor, time);
            c.text(font, Texts.tr(Texts.DG_SHOWS_DOMINANT), rx, y + 41, 0.55f, Theme.fade(Theme.TEXT_DIM, k));
            Instruments.bar(c, rx, y + 52, rw, 4, 0.25f * k, recColor, time);
            c.text(font, Texts.tr(Texts.DG_SHOWS_RECESSIVE), rx, y + 59, 0.55f, Theme.fade(Theme.TEXT_DIM, k));
        }
    }

    private static void mutation(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        int rungs = 12;
        float x0 = x + 14;
        float x1 = x + w - 14;
        float cy = y + h / 2f - 2;
        float flipAt = (time % 4f);
        boolean flipped = flipAt > 2f;
        c.line(x0, cy - 9, x1, cy - 9, 1.6f, Theme.ACCENT_MUTAGEN);
        c.line(x0, cy + 9, x1, cy + 9, 1.6f, Theme.shade(Theme.ACCENT_MUTAGEN, 0.6f));
        for (int i = 0; i < rungs; i++) {
            float rx = x0 + (x1 - x0) * (i + 0.5f) / rungs;
            Gene gene = Gene.VALUES[i % Gene.COUNT];
            int top = GenomeArt.alleleColor(catalog, gene, SAMPLE.first(gene));
            int bottom = GenomeArt.alleleColor(catalog, gene, SAMPLE.second(gene));
            if (i == 7 && flipped) {
                top = Theme.MUTANT;
                float burst = Anim.clamp01(1f - (flipAt - 2f) * 1.5f);
                c.bloom(rx, cy - 4, 6 + 10 * burst, Theme.fade(Theme.MUTANT, 0.4f + 0.5f * burst));
                for (int s = 0; s < 6; s++) {
                    float a = s * 1.047f + time;
                    c.line(rx, cy - 4, rx + Canvas.cos(a) * 8 * burst, cy - 4 + Canvas.sin(a) * 8 * burst, 0.7f, Theme.fade(Theme.MUTANT, burst));
                }
            }
            c.line(rx, cy - 9, rx, cy, 2f, top);
            c.line(rx, cy, rx, cy + 9, 2f, bottom);
        }
        if (flipped) {
            c.centered(font, Texts.tr(Texts.DG_MUTATED), x0 + (x1 - x0) * 7.5f / rungs, y + h - 9, 0.6f, Theme.MUTANT);
        }
    }

    private static void graft(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        Gene[] root = {Gene.COLD_TOLERANCE, Gene.DROUGHT_TOLERANCE, Gene.BLIGHT_RESISTANCE, Gene.NUTRIENT_DRAW};
        Gene[] scion = {Gene.YIELD, Gene.GROWTH_RATE, Gene.LIGHT_NEED, Gene.MUTATION_RATE};
        float colW = (w - 20) / 2f;
        c.text(font, Texts.tr(Texts.DG_ROOTSTOCK), x + 6, y + 3, 0.55f, Theme.ACCENT_COMPOST);
        c.text(font, Texts.tr(Texts.DG_SCION), x + 14 + colW, y + 3, 0.55f, Theme.GOOD);
        float ux = x + w / 2f;
        float uy = y + h - 9;
        for (int i = 0; i < 4; i++) {
            float ry = y + 11 + i * 9;
            for (int side = 0; side < 2; side++) {
                Gene gene = side == 0 ? root[i] : scion[i];
                float cx = side == 0 ? x + 6 : x + 14 + colW;
                int gc = catalog.gene(gene).color() | 0xFF000000;
                c.round(cx, ry, colW - 6, 7.5f, 2, Theme.fade(gc, 0.22f));
                c.rect(cx, ry, 1.5f, 7.5f, gc);
                c.text(font, GeneText.gene(gene), cx + 4, ry + 1.6f, 0.55f, Theme.TEXT);
                float sx = side == 0 ? cx + colW - 6 : cx;
                c.dashed(sx, ry + 3.75f, ux, uy - 3, 0.6f, 2, 2, -time * 10f, Theme.fade(gc, 0.45f));
            }
        }
        Component union = Texts.tr(Texts.DG_UNION);
        float uw = font.width(union) * 0.65f + 10;
        c.haloRect(ux - uw / 2, uy - 3, uw, 9, 3, Theme.fade(Theme.ACCENT_CARD, 0.3f + 0.2f * Anim.pulse(time, 0.8f)));
        c.round(ux - uw / 2, uy - 3, uw, 9, 3, 0xF0141E22);
        c.frame(ux - uw / 2, uy - 3, uw, 9, 0.7f, Theme.ACCENT_CARD);
        c.centered(font, union, ux, uy - 0.6f, 0.65f, Theme.ACCENT_CARD);
    }

    private static void backcross(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float lx = x + 8;
        c.text(font, Texts.tr(Texts.DG_F1), lx, y + 4, 0.55f, Theme.TEXT_DIM);
        pair(c, lx, y + 13, Theme.WILD, Theme.WILD, "W", "W", font);
        c.text(font, Component.literal("x"), lx + 30, y + 15, Theme.TEXT_DIM);
        pair(c, lx + 40, y + 13, Theme.ACCENT_CARD, Theme.ACCENT_CARD, "C", "C", font);
        c.tri(lx + 30, y + 32, Theme.TEXT_DIM, lx + 36, y + 32, Theme.TEXT_DIM, lx + 33, y + 36, Theme.TEXT_DIM);
        pair(c, lx + 20, y + 38, Theme.WILD, Theme.ACCENT_CARD, "W", "C", font);
        float gx = x + 104;
        float cell = Math.min((w - 112) / 4f, (h - 16) / 4f);
        c.text(font, Texts.tr(Texts.DG_F2), gx, y + 3, 0.55f, Theme.WILD);
        int target = 15;
        for (int i = 0; i < 16; i++) {
            int r = i / 4;
            int col = i % 4;
            float px = gx + col * cell;
            float py = y + 11 + r * cell;
            boolean best = i == target;
            int fill = best ? Theme.WILD : Theme.lerp(0xFF1A2A2E, Theme.ACCENT_CARD, 0.2f + 0.15f * ((r + col) % 3));
            c.round(px + 0.8f, py + 0.8f, cell - 1.6f, cell - 1.6f, 2, Theme.fade(fill, best ? 0.9f : 0.5f));
            if (best) {
                c.haloRect(px + 0.8f, py + 0.8f, cell - 1.6f, cell - 1.6f, 3 + 2 * Anim.pulse(time, 1f), Theme.fade(Theme.WILD, 0.5f));
                float lx2 = gx + cell * 4 + 6;
                c.tri(lx2, py + cell / 2f, Theme.WILD, lx2 + 4, py + cell / 2f - 3, Theme.WILD, lx2 + 4, py + cell / 2f + 3, Theme.WILD);
                c.text(font, Component.literal("1 / 16"), lx2 + 7, py + cell / 2f - 3, 0.7f, Theme.WILD);
            }
        }
    }

    private static void pair(Canvas c, float x, float y, int a, int b, String la, String lb, Font font) {
        c.round(x, y, 12, 14, 3, Theme.fade(Theme.shade(a, 0.6f), 0.95f));
        c.round(x + 13, y, 12, 14, 3, Theme.fade(Theme.shade(b, 0.6f), 0.95f));
        c.centered(font, Component.literal(la), x + 6, y + 3.5f, a);
        c.centered(font, Component.literal(lb), x + 19, y + 3.5f, b);
    }

    private static void card(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        float bw = w - 40;
        float bx = x + 30;
        c.text(font, Component.literal("A"), x + 18, y + 7, 0.7f, Theme.TEXT_DIM);
        c.text(font, Component.literal("B"), x + 18, y + 17, 0.7f, Theme.TEXT_DIM);
        GenomeArt.barcode(c, bx, y + 5, bw, 20, SAMPLE, catalog);
        for (Gene gene : Gene.VALUES) {
            float gx = bx + gene.ordinal() * bw / Gene.COUNT;
            c.centered(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), gx + bw / Gene.COUNT / 2f, y + 28, 0.5f,
                    catalog.gene(gene).color() | 0xFF000000);
        }
        float sweep = bx + (time * 30f) % bw;
        c.glow(true);
        c.rect(sweep, y + 4, 1, 22, Theme.withAlpha(0xFFFFFF, 0.5f));
        c.glow(false);
    }

    private static void grades(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float t = (time % 8f) / 8f;
        float score = Anim.easeInOut(t < 0.8f ? t / 0.8f : 1f);
        float bx = x + 8;
        float bw = w - 16;
        float by = y + 6;
        float[] starts = {0f, 0.42f, 0.58f, 0.74f};
        Grade[] grades = Grade.values();
        for (int i = 0; i < grades.length; i++) {
            float a = starts[i];
            float b = i + 1 < starts.length ? starts[i + 1] : 1f;
            boolean lit = score >= a;
            c.rect(bx + a * bw, by, (b - a) * bw - 1, 6, Theme.fade(grades[i].color(), lit ? 0.9f : 0.25f));
            float cx = bx + (a + b) / 2f * bw;
            c.centered(font, Texts.tr(grades[i].translationKey()), cx, by + 9, 0.6f, lit ? grades[i].color() : Theme.TEXT_MUTED);
            c.centered(font, Component.literal("x" + trim(grades[i].priceMultiplier())), cx, by + 17, 0.55f, lit ? Theme.ACCENT_BANK : Theme.TEXT_MUTED);
        }
        float nx = bx + score * bw;
        c.rect(nx - 0.5f, by - 3, 1, 12, Theme.TEXT);
        c.bloom(nx, by + 3, 6, Theme.fade(Theme.TEXT, 0.3f));
        c.text(font, Texts.tr(Texts.DG_GENES_CARE), bx, by + 27, 0.55f, Theme.TEXT_DIM);
    }

    private static String trim(float v) {
        return v == Math.round(v) ? Integer.toString(Math.round(v)) : String.format(Locale.ROOT, "%.2f", v).replaceAll("0+$", "");
    }

    private static void market(Canvas c, Font font, float x, float y, float w, float h, float time) {
        int days = 10;
        float gx = x + 10;
        float gw = w - 20;
        float gy = y + 6;
        float gh = h - 18;
        c.rect(gx, gy + gh, gw, 0.6f, 0x40FFFFFF);
        float[] demand = new float[days];
        float d = 1f;
        for (int i = 0; i < days; i++) {
            boolean flood = i >= 2 && i <= 4;
            d = flood ? d * 0.62f : Math.min(1f, d + 0.15f);
            demand[i] = d;
        }
        float reveal = (time % 10f) / 10f * days;
        float colW = gw / days;
        for (int i = 0; i < days; i++) {
            if (i > reveal) {
                break;
            }
            float bh = demand[i] * gh;
            boolean flood = i >= 2 && i <= 4;
            c.rect(gx + i * colW + 1, gy + gh - bh, colW - 2, bh, Theme.fade(Theme.quality(demand[i]), flood ? 0.95f : 0.7f));
        }
        c.text(font, Texts.tr(Texts.DG_FLOOD), gx + 2 * colW, y, 0.5f, Theme.BAD);
        c.right(font, Texts.tr(Texts.DG_RECOVER), gx + gw, y, 0.5f, Theme.GOOD);
        c.text(font, Texts.tr(Texts.DG_DAYS), gx, gy + gh + 2, 0.5f, Theme.TEXT_MUTED);
    }

    private static void atlas(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        AlleleTable table = catalog.table();
        float cell = Math.min(9f, (h - 12) / Gene.COUNT);
        int known = 0;
        int total = 0;
        for (Gene gene : Gene.VALUES) {
            float ry = y + 2 + gene.ordinal() * cell;
            int gc = catalog.gene(gene).color() | 0xFF000000;
            c.text(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), x + 4, ry + 1, 0.5f, gc);
            int i = 0;
            for (Allele allele : table.alleles(gene)) {
                boolean have = GuideClient.done(AlleleAtlas.PREFIX + allele.id());
                float cx = x + 22 + i * (cell + 1);
                if (cx + cell > x + w) {
                    break;
                }
                c.round(cx, ry, cell, cell - 1, 1.5f, have ? gc : 0x18FFFFFF);
                if (have) {
                    known++;
                } else if (allele.mutationOnly()) {
                    c.text(font, Component.literal("?"), cx + cell / 2f - 1.2f, ry + 1.5f, 0.45f, Theme.fade(Theme.MUTANT, 0.6f + 0.4f * Anim.pulse(time, 0.7f)));
                }
                total++;
                i++;
            }
        }
        c.right(font, Texts.tr(Texts.UI_GE_ATLAS, known, total), x + w - 4, y + h - 8, 0.55f, Theme.TEXT_DIM);
    }

    private static void plant(Canvas c, float x, float ground, float height, int leaf, int flower, float sway) {
        c.line(x, ground, x + sway, ground - height, 1.2f, leaf);
        c.line(x + sway * 0.4f, ground - height * 0.4f, x - 3 + sway * 0.4f, ground - height * 0.55f, 1f, leaf);
        c.line(x + sway * 0.6f, ground - height * 0.6f, x + 3 + sway * 0.6f, ground - height * 0.75f, 1f, leaf);
        if (flower != 0) {
            c.disc(x + sway, ground - height, 2f, Theme.shade(flower, 1.2f), flower);
        }
    }

    private static void wind(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float ground = y + h - 8;
        c.rectV(x + 1, ground, w - 2, 7, 0xFF3A2A1A, 0xFF22180F);
        c.text(font, Texts.tr(Texts.DG_WIND), x + 6, y + 4, 0.55f, Theme.INFO);
        c.dashed(x + 30, y + 7, x + w - 30, y + 7, 0.8f, 6, 4, time * 14f, Theme.fade(Theme.INFO, 0.7f));
        c.tri(x + w - 30, y + 4, Theme.INFO, x + w - 30, y + 10, Theme.INFO, x + w - 25, y + 7, Theme.INFO);
        int leaf = Theme.opaque(CropSpecies.CLOVER.leafColor());
        int flower = Theme.opaque(CropSpecies.CLOVER.flowerColor());
        float spacing = (w - 30) / 5f;
        float screenX = x + 15 + spacing * 3.5f;
        for (int i = 0; i < 5; i++) {
            float px = x + 15 + spacing * i;
            plant(c, px, ground, 20 + (i % 2) * 4, leaf, flower, Canvas.sin(time * 1.6f + i) * 1.5f);
        }
        c.stripes(screenX - 2, ground - 30, 4, 30, 4, 0, Theme.fade(Theme.TEXT_DIM, 0.6f), 0xFF1A2428);
        c.centered(font, Texts.tr(Texts.DG_SCREEN), screenX, ground - 37, 0.5f, Theme.TEXT_DIM);
        float startX = x + 15;
        float startY = ground - 22;
        for (int i = 0; i < 16; i++) {
            float phase = (time * 0.22f + i / 16f) % 1f;
            float px = startX + phase * (w - 30);
            if (px > screenX - 3) {
                continue;
            }
            float py = startY - 4 + Canvas.sin(phase * 12f + i) * 3f;
            float a = Math.min(1f, phase * 6f);
            c.bloom(px, py, 3f, Theme.fade(0xFFF2D25A, 0.6f * a));
            c.rect(px - 0.5f, py - 0.5f, 1, 1, Theme.fade(0xFFFFF0A0, a));
        }
    }

    private static void ph(Canvas c, Font font, Catalog catalog, float x, float y, float w, float h, float time) {
        float sx = x + 48;
        float sw = w - 54;
        for (int i = 0; i < 30; i++) {
            float t = i / 30f;
            c.rect(sx + sw * t, y + 4, sw / 30f + 0.3f, 4, Theme.hsv(0.75f * t, 0.65f, 0.85f));
        }
        for (int v = 4; v <= 9; v++) {
            float px = sx + sw * (v - 3.5f) / 5.5f;
            c.rect(px, y + 8, 0.5f, 2, Theme.TEXT_DIM);
            c.centered(font, Component.literal(String.valueOf(v)), px, y + 10.5f, 0.45f, Theme.TEXT_DIM);
        }
        float row = (h - 18) / CropSpecies.VALUES.length;
        for (CropSpecies species : CropSpecies.VALUES) {
            PhBand band = catalog.phBand(species.key());
            float ry = y + 16 + species.ordinal() * row;
            c.text(font, GeneText.species(species.key()), x + 4, ry + row / 2f - 2.2f, 0.52f, Theme.TEXT_DIM);
            float b0 = sx + sw * Anim.clamp01((band.min() - 3.5f) / 5.5f);
            float b1 = sx + sw * Anim.clamp01((band.max() - 3.5f) / 5.5f);
            float l0 = sx + sw * Anim.clamp01((band.min() - band.lethalMargin() - 3.5f) / 5.5f);
            float l1 = sx + sw * Anim.clamp01((band.max() + band.lethalMargin() - 3.5f) / 5.5f);
            c.rect(l0, ry + row / 2f - 0.5f, l1 - l0, 1, Theme.fade(Theme.WARN, 0.35f));
            int leaf = Theme.opaque(species.leafColor());
            c.round(b0, ry + 1, b1 - b0, row - 2, 1.5f, Theme.fade(leaf, 0.85f));
        }
        float sweep = sx + sw * (0.5f + 0.5f * Canvas.sin(time * 0.5f));
        c.rect(sweep - 0.4f, y + 14, 0.8f, h - 16, Theme.fade(0xFFFFFFFF, 0.35f));
    }

    private static void npk(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float cycle = time % 10f;
        boolean fixing = cycle >= 5f;
        float t = fixing ? (cycle - 5f) / 5f : cycle / 5f;
        float n = fixing ? 20 + 45 * t : 65 - 45 * t;
        float p = fixing ? 52 - 8 * t : 60 - 8 * t;
        float k = fixing ? 52 - 4 * t : 60 - 8 * t;
        ItemStack crop = new ItemStack(fixing ? ModItems.produce(CropSpecies.CLOVER) : ModItems.produce(CropSpecies.WHEATLINE));
        c.flush();
        c.g().item(crop, (int) x + 6, (int) (y + h / 2f - 8));
        c.text(font, Texts.tr(fixing ? Texts.DG_FIXER : Texts.DG_CEREAL), x + 26, y + 4, 0.6f, fixing ? Theme.GOOD : Theme.WARN);
        String[] labels = {"N", "P", "K"};
        float[] values = {n, p, k};
        int[] colors = {0xFF6FD06F, 0xFFE07A5A, 0xFFB08AE0};
        for (int i = 0; i < 3; i++) {
            float ry = y + 14 + i * 10;
            c.text(font, Component.literal(labels[i]), x + 26, ry, 0.6f, colors[i]);
            Instruments.bar(c, x + 34, ry + 1, w - 60, 3.5f, values[i] / 100f, colors[i], time);
            c.right(font, Component.literal(String.valueOf(Math.round(values[i]))), x + w - 6, ry, 0.6f, Theme.TEXT_DIM);
        }
    }

    private static void blight(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float cycle = time % 11f;
        float fieldW = (w - 18) / 2f;
        for (int f = 0; f < 2; f++) {
            boolean clones = f == 0;
            float fx = x + 6 + f * (fieldW + 6);
            float fy = y + 11;
            c.text(font, Texts.tr(clones ? Texts.DG_CLONES : Texts.DG_MIXED), fx, y + 3, 0.55f, clones ? Theme.BAD : Theme.GOOD);
            int cols = 8;
            int rows = 5;
            float cell = Math.min(fieldW / cols, (h - 14) / rows);
            int infected = 0;
            for (int r = 0; r < rows; r++) {
                for (int col = 0; col < cols; col++) {
                    int seed = f * 1000 + r * 31 + col * 7;
                    int base = clones ? 0xFF6FBF4F : Theme.hsv(0.18f + 0.22f * Anim.hash(seed), 0.55f, 0.75f);
                    float dist = Math.abs(col - 0) + Math.abs(r - 2);
                    boolean resists = !clones && Anim.hash(seed + 5) < 0.35f;
                    float delay = clones ? dist * 0.55f : dist * 1.6f + Anim.hash(seed + 9) * 1.2f;
                    float since = cycle - 0.8f - delay;
                    float px = fx + col * cell;
                    float py = fy + r * cell;
                    int colr = base;
                    if (!resists && since > 0) {
                        infected++;
                        colr = since > 3f ? 0xFF3A2A1E : Theme.lerp(base, Theme.ACCENT_BLIGHT, Anim.clamp01(since / 0.8f));
                        if (since < 3f) {
                            float sp = (since * 9f) % 1f;
                            c.rect(px + cell * Anim.hash(seed + (int) (since * 9)), py + cell * sp, 0.8f, 0.8f, Theme.fade(0xFFE8C080, 0.8f));
                        }
                    }
                    c.round(px + 0.6f, py + 0.6f, cell - 1.2f, cell - 1.2f, 1.5f, colr);
                }
            }
            c.right(font, Component.literal(infected + "/" + (cols * rows)), fx + fieldW, y + 3, 0.55f, Theme.TEXT_DIM);
        }
    }

    private static void greenhouse(Canvas c, Font font, float x, float y, float w, float h, float time) {
        float cycle = time % 9f;
        int cols = 12;
        int rows = 5;
        float cell = Math.min((w - 40) / cols, (h - 16) / rows);
        float bx = x + (w - cell * cols) / 2f;
        float by = y + 6;
        float ground = by + rows * cell;
        c.rectV(x + 1, ground, w - 2, h - (ground - y) - 1, 0xFF3A2A1A, 0xFF22180F);
        int originCol = cols / 2;
        int originRow = rows - 1;
        boolean leaking = cycle > 5f && cycle < 8f;
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                float dist = Math.abs(col - originCol) + Math.abs(r - originRow);
                float fillAt = 0.4f + dist * 0.18f;
                float px = bx + col * cell;
                float py = by + r * cell;
                if (cycle > fillAt) {
                    int air = leaking ? Theme.fade(Theme.BAD, 0.25f + 0.15f * Anim.pulse(time, 2f)) : Theme.fade(Theme.ACCENT_CLIMATE, 0.28f);
                    c.rect(px + 0.4f, py + 0.4f, cell - 0.8f, cell - 0.8f, air);
                }
            }
        }
        int pane = 0xFF9ED8F0;
        c.rect(bx - 2, by - 2, cell * cols + 4, 2, pane);
        c.rect(bx - 2, by, 2, rows * cell, pane);
        if (leaking) {
            c.rect(bx + cell * cols, by, 2, (rows - 2) * cell, pane);
            c.centered(font, Texts.tr(Texts.DG_LEAK), bx + cell * cols + 10, ground - cell * 1.5f - 3, 0.55f,
                    Theme.fade(Theme.BAD, 0.6f + 0.4f * Anim.pulse(time, 2f)));
            for (int i = 0; i < 6; i++) {
                float ph = (time * 0.8f + i / 6f) % 1f;
                c.rect(bx + cell * cols + 2 + ph * 16, ground - cell * (0.5f + (i % 2)) - ph * 3, 1, 1, Theme.fade(Theme.BAD, 1f - ph));
            }
        } else {
            c.rect(bx + cell * cols, by, 2, rows * cell, pane);
        }
        float cx = bx + originCol * cell;
        float cy = by + originRow * cell;
        c.rect(cx + 1, cy + 1, cell - 2, cell - 2, 0xFF2A4A58);
        c.bloom(cx + cell / 2f, cy + cell / 2f, cell, Theme.fade(Theme.ACCENT_CLIMATE, 0.5f));
        if (!leaking && cycle > 2.2f) {
            c.text(font, Texts.tr(Texts.DG_SEALED), bx + 3, by + 2, 0.55f, Theme.GOOD);
        }
    }

    private static void canopy(Canvas c, Font font, float x, float y, float w, float h, float time) {
        c.rectV(x + 1, y + 1, w - 2, h - 2, 0xFF0E2218, 0xFF07120C);
        c.glow(true);
        for (int i = 0; i < 4; i++) {
            float sx = x + 20 + i * (w - 40) / 3f + Canvas.sin(time * 0.3f + i) * 4f;
            c.quad(sx - 3, y, Theme.withAlpha(0x9FE07A, 0.12f), sx - 10, y + h, 0x009FE07A, sx + 10, y + h, 0x009FE07A, sx + 3, y,
                    Theme.withAlpha(0x9FE07A, 0.12f));
        }
        c.glow(false);
        for (int i = 0; i < 3; i++) {
            float tx = x + 30 + i * (w - 60) / 2f;
            c.rect(tx - 4, y + 2, 8, h - 6, 0xFF4A3424);
            c.rect(tx - 4, y + 2, 2, h - 6, 0xFF5E432E);
        }
        float[] levels = {0.28f, 0.58f, 0.86f};
        for (int l = 0; l < levels.length; l++) {
            float py = y + h * levels[l] - 4;
            float off = (l % 2) * 18;
            for (int s = 0; s < 3; s++) {
                float px = x + 10 + off + s * (w - 40) / 2.2f;
                c.round(px, py, 46, 5, 2.5f, 0xFF2F6A2A);
                c.rect(px + 2, py, 42, 1.2f, 0xFF4E9A3C);
                for (int d = 0; d < 3; d++) {
                    float dx = px + 8 + d * 13;
                    c.disc(dx, py - 1.5f, 1.4f, Theme.WILD, Theme.shade(Theme.WILD, 0.7f));
                }
                for (int v = 0; v < 2; v++) {
                    float vx = px + 10 + v * 24;
                    float sway = Canvas.sin(time * 1.2f + s + v + l) * 1.5f;
                    c.line(vx, py + 5, vx + sway, py + 13, 0.8f, 0xFF3E7A30);
                }
            }
        }
        float rx = x + w - 34;
        float ry = y + h - 9;
        for (int i = 0; i < 8; i++) {
            float a = (float) (Math.PI * 2 * i / 8);
            c.rect(rx + Canvas.cos(a) * 9 - 1.5f, ry + Canvas.sin(a) * 3.2f - 1.5f, 3, 3, 0xFF8A6A3A);
        }
        c.bloom(rx, ry, 9 + 2 * Anim.pulse(time, 0.7f), Theme.fade(Theme.WILD, 0.5f));
        Component portal = Texts.tr(Texts.DG_PORTAL);
        float lw = font.width(portal) * 0.5f;
        c.round(rx - 16 - lw - 3, ry - 4.5f, lw + 6, 7, 2, 0xE0081410);
        c.text(font, portal, rx - 16 - lw, ry - 2.5f, 0.5f, Theme.WILD);
        Icons.draw(c, Icon.PORTAL, x + 10, y + 8, 8, Theme.fade(Theme.WILD, 0.7f));
    }
}
