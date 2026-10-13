package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.GenomeArt;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.component.CardData;
import com.cultivar.data.BlightStrain;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.GeneDefinition;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.genetics.PunnettSimulator;
import com.cultivar.menu.GenomeCardMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GenomeCardScreen extends CultivarContainerScreen<GenomeCardMenu> {
    private static final int W = 344;
    private static final int H = 216;
    private static final int TAB_W = 52;
    private static final int TOP = 40;

    private enum Tab { GENOTYPE, PROFILE, CROSS }

    private Tab tab = Tab.GENOTYPE;
    private long tabSince = Util.getMillis();
    private Gene selected;
    private long selectedSince = Util.getMillis();

    public GenomeCardScreen(GenomeCardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, W, H);
    }

    @Override
    protected int accent() {
        return menu.card().strain().isPresent() ? Theme.ACCENT_BLIGHT : Theme.ACCENT_CARD;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.HELIX;
    }

    @Override
    protected boolean working() {
        return true;
    }

    private Catalog catalog() {
        return minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults();
    }

    private float tabAge() {
        return (Util.getMillis() - tabSince) / 1000f;
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        CardData card = menu.card();
        Catalog catalog = catalog();
        if (card.strain().isPresent()) {
            strainReport(c, catalog, card.strain().get(), mouseX, mouseY);
            return;
        }
        if (card.genome().isEmpty()) {
            c.centered(font, Texts.tr(Texts.TIP_BLANK_CARD), leftPos + W / 2f, topPos + H / 2f, Theme.TEXT_DIM);
            return;
        }
        Genome genome = card.genome().get();
        AlleleTable table = catalog.table();
        Phenotype phenotype = Phenotype.resolve(genome, table);
        if (selected == null) {
            selected = Gene.YIELD;
            for (Gene gene : Gene.VALUES) {
                if (!genome.isHomozygous(gene)) {
                    selected = gene;
                    break;
                }
            }
        }
        header(c, card, genome, phenotype, catalog);
        tabs(c, mouseX, mouseY);
        float k = Anim.easeOutCubic(tabAge() / 0.3f);
        c.alpha(k);
        c.g().pose().pushMatrix();
        c.g().pose().translate((1f - k) * 10f, 0);
        switch (tab) {
            case GENOTYPE -> genotype(c, catalog, genome, phenotype, mouseX, mouseY);
            case PROFILE -> profile(c, catalog, genome, phenotype, mouseX, mouseY);
            case CROSS -> cross(c, catalog, card, genome, mouseX, mouseY);
        }
        c.flush();
        c.g().pose().popMatrix();
        c.alpha(1f);
    }

    private void header(Canvas c, CardData card, Genome genome, Phenotype phenotype, Catalog catalog) {
        float x = leftPos + 8;
        float y = topPos + 20;
        Component species = card.species().map(GeneText::species).orElse(Texts.tr(Texts.UNKNOWN));
        c.glowText(font, species, x, y, Theme.TEXT, Theme.ACCENT_CARD);
        float sx = x + font.width(species) + 6;
        c.text(font, Texts.tr(Texts.TIP_LINEAGE, card.lineage()), sx, y + 1.5f, 0.7f, Theme.TEXT_DIM);
        boolean pure = genome.isTrueBreeding();
        Instruments.chip(c, font, Texts.tr(pure ? Texts.LABEL_TRUE_BREEDING : Texts.LABEL_SEGREGATING), x, y + 10, pure ? Theme.GOOD : Theme.WARN,
                pure);
        c.text(font, Component.literal(genome.toHex().toUpperCase(Locale.ROOT)), x + 62, y + 11.5f, 0.5f, Theme.TEXT_MUTED);
    }

    private void tabs(Canvas c, int mouseX, int mouseY) {
        float x = leftPos + W - 8 - TAB_W * 3;
        float y = topPos + 20;
        Tab[] all = Tab.values();
        for (int i = 0; i < all.length; i++) {
            float tx = x + i * TAB_W;
            boolean active = all[i] == tab;
            boolean hover = mouseX >= tx && mouseX < tx + TAB_W - 2 && mouseY >= y && mouseY < y + 14;
            int col = active ? Theme.ACCENT_CARD : hover ? Theme.TEXT : Theme.TEXT_MUTED;
            c.round(tx, y, TAB_W - 2, 14, 2, active ? Theme.fade(Theme.ACCENT_CARD, 0.18f) : hover ? 0x18FFFFFF : 0x0CFFFFFF);
            if (active) {
                c.rect(tx + 2, y + 13, TAB_W - 6, 1.2f, Theme.ACCENT_CARD);
                c.bloom(tx + TAB_W / 2f - 1, y + 13.5f, 10, Theme.fade(Theme.ACCENT_CARD, 0.35f));
            }
            c.centered(font, Texts.tr(switch (all[i]) {
                case GENOTYPE -> Texts.UI_TAB_GENOTYPE;
                case PROFILE -> Texts.UI_TAB_PROFILE;
                case CROSS -> Texts.UI_TAB_CROSS;
            }), tx + TAB_W / 2f - 1, y + 4.5f, 0.65f, col);
        }
        c.rectH(leftPos + 6, topPos + TOP - 4, W - 12, 0.6f, Theme.fade(Theme.ACCENT_CARD, 0.05f), Theme.fade(Theme.ACCENT_CARD, 0.5f));
    }

    private void genotype(Canvas c, Catalog catalog, Genome genome, Phenotype phenotype, int mouseX, int mouseY) {
        AlleleTable table = catalog.table();
        float x = leftPos + 8;
        float cols0 = x;
        float colA = x + 74;
        float colB = x + 172;
        float colV = x + 262;
        float hy = topPos + TOP;
        c.text(font, Texts.tr(Texts.LABEL_GENE), cols0, hy, 0.55f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.LABEL_ALLELE_A), colA, hy, 0.55f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.LABEL_ALLELE_B), colB, hy, 0.55f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.LABEL_VALUE), colV, hy, 0.55f, Theme.TEXT_MUTED);
        float age = tabAge();
        for (Gene gene : Gene.VALUES) {
            float rowK = Anim.easeOutBack((age - gene.ordinal() * 0.035f) / 0.35f);
            float y = topPos + TOP + 8 + gene.ordinal() * 20 + (1f - rowK) * 6f;
            float rowAlpha = Anim.clamp01((age - gene.ordinal() * 0.035f) / 0.2f);
            float saved = c.alpha();
            c.alpha(saved * rowAlpha);
            GeneDefinition def = catalog.gene(gene);
            int geneColor = def.color() | 0xFF000000;
            boolean rowHover = mouseX >= x && mouseX < leftPos + W - 8 && mouseY >= y - 2 && mouseY < y + 17;
            c.rect(x - 2, y - 2, W - 12, 19, rowHover ? 0x14FFFFFF : gene.ordinal() % 2 == 0 ? 0x08FFFFFF : 0x03FFFFFF);
            c.rect(x - 2, y - 2, 1.5f, 19, geneColor);
            c.text(font, GeneText.gene(gene), cols0 + 2, y + 1, 0.7f, Theme.TEXT);
            c.text(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), cols0 + 2, y + 9, 0.5f, Theme.fade(geneColor, 0.8f));
            int expression = phenotype.expression(gene);
            Allele a = table.get(gene, genome.first(gene));
            Allele b = table.get(gene, genome.second(gene));
            capsule(c, colA, y, 92, 14, catalog, a, stateOf(genome, phenotype, gene, 0, expression), mouseX, mouseY, gene);
            capsule(c, colB, y, 86, 14, catalog, b, stateOf(genome, phenotype, gene, 1, expression), mouseX, mouseY, gene);
            float mx = colA + 92 + 4;
            float my = y + 7;
            if (a.dominance() == b.dominance()) {
                c.rect(mx - 2.5f, my - 1.8f, 5, 0.9f, Theme.INFO);
                c.rect(mx - 2.5f, my + 0.9f, 5, 0.9f, Theme.INFO);
            } else {
                Icons.draw(c, a.dominance() > b.dominance() ? Icon.LEFT : Icon.RIGHT, mx, my, 7, Theme.GOOD);
            }
            float value = phenotype.get(gene);
            float fav = def.favourability(value);
            c.text(font, Component.literal(gene.format(value)), colV, y + 0.5f, 0.75f, Theme.quality(fav));
            float bw = 66;
            float barK = Anim.easeOutCubic((age - 0.15f - gene.ordinal() * 0.03f) / 0.5f);
            Instruments.bar(c, colV, y + 9, bw, 3, fav * barK, geneColor, time);
            c.alpha(saved);
        }
        c.text(font, Texts.tr(Texts.UI_LEGEND_GENOTYPE), x, topPos + H - 9, 0.5f, Theme.TEXT_MUTED);
    }

    private static int stateOf(Genome genome, Phenotype phenotype, Gene gene, int slot, int expression) {
        if (genome.isHomozygous(gene)) {
            return 1;
        }
        if (!phenotype.isExpressed(gene, slot)) {
            return 0;
        }
        return expression == Phenotype.EXPRESSED_BOTH ? 2 : 1;
    }

    private void capsule(Canvas c, float x, float y, float w, float h, Catalog catalog, Allele allele, int state, int mouseX, int mouseY,
                         Gene gene) {
        int col = GenomeArt.alleleColor(catalog, allele);
        float pulse = Anim.pulse(time + gene.ordinal() * 0.13f, 0.7f);
        switch (state) {
            case 0 -> {
                c.round(x, y, w, h, 3, Theme.fade(col, 0.12f));
                c.dashed(x + 2, y, x + w - 2, y, 0.7f, 3, 2, 0, Theme.fade(col, 0.45f));
                c.dashed(x + 2, y + h, x + w - 2, y + h, 0.7f, 3, 2, 0, Theme.fade(col, 0.45f));
            }
            case 1 -> {
                c.haloRect(x, y, w, h, 3 + pulse * 2, Theme.fade(col, 0.22f + 0.12f * pulse));
                c.round(x, y, w, h, 3, Theme.fade(Theme.shade(col, 0.55f), 0.95f));
                c.rectV(x + 1, y + 1, w - 2, h * 0.5f, Theme.fade(col, 0.45f), Theme.fade(col, 0.05f));
                c.frame(x, y, w, h, 0.8f, Theme.fade(col, 0.95f));
            }
            default -> {
                c.haloRect(x, y, w, h, 3 + pulse * 2, Theme.fade(Theme.INFO, 0.22f + 0.12f * pulse));
                c.round(x, y, w, h, 3, Theme.fade(Theme.shade(col, 0.5f), 0.95f));
                c.rectV(x + 1, y + 1, w - 2, h * 0.5f, Theme.fade(Theme.INFO, 0.35f), Theme.fade(Theme.INFO, 0.05f));
                c.frame(x, y, w, h, 0.8f, Theme.INFO);
            }
        }
        float textScale = 0.72f;
        int maxPx = (int) ((w - 22) / textScale);
        String name = font.plainSubstrByWidth(Component.translatable(allele.translationKey()).getString(), maxPx);
        c.text(font, Component.literal(name), x + 4, y + 4.2f, textScale, state == 0 ? Theme.TEXT_MUTED : Theme.TEXT);
        for (int i = 0; i < 3; i++) {
            float px = x + w - 15 + i * 4;
            boolean on = i < allele.dominance();
            c.disc(px, y + h / 2f, 1.3f, on ? (state == 0 ? Theme.TEXT_MUTED : Theme.TEXT) : 0x30FFFFFF, on ? Theme.TEXT_DIM : 0x18FFFFFF);
        }
        if (allele.wild() || allele.mutationOnly()) {
            int tagColor = allele.wild() ? Theme.WILD : Theme.MUTANT;
            c.round(x + w - 3.5f, y - 1.5f, 5, 5, 1.5f, tagColor);
        }
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            AlleleTable table = catalog.table();
            List<Component> tip = new ArrayList<>();
            tip.add(GeneText.allele(allele));
            tip.add(Texts.tr(Texts.DOMINANCE, allele.dominance()).withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal(gene.format(table.effectiveValue(allele))).withStyle(ChatFormatting.GRAY));
            tip.add(Texts.tr(state == 0 ? Texts.HIDDEN : state == 2 ? Texts.CODOMINANT : Texts.EXPRESSED));
            if (allele.wild()) {
                tip.add(Texts.tr(Texts.FLAG_WILD).withStyle(ChatFormatting.DARK_GREEN));
            }
            if (allele.mutationOnly()) {
                tip.add(Texts.tr(Texts.FLAG_MUTANT).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            tooltip(c.g(), tip, mouseX, mouseY);
        }
    }

    private void profile(Canvas c, Catalog catalog, Genome genome, Phenotype phenotype, int mouseX, int mouseY) {
        AlleleTable table = catalog.table();
        float cx = leftPos + 94;
        float cy = topPos + 128;
        float r = 54;
        float grow = Anim.easeOutBack(tabAge() / 0.6f);
        int n = Gene.COUNT;
        for (int ring = 1; ring <= 4; ring++) {
            float rr = r * ring / 4f;
            float[] pts = new float[n * 2];
            for (int i = 0; i < n; i++) {
                float a = axis(i);
                pts[i * 2] = cx + Canvas.cos(a) * rr;
                pts[i * 2 + 1] = cy + Canvas.sin(a) * rr;
            }
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                c.line(pts[i * 2], pts[i * 2 + 1], pts[j * 2], pts[j * 2 + 1], ring == 4 ? 0.9f : 0.5f,
                        Theme.fade(Theme.ACCENT_CARD, ring == 4 ? 0.45f : 0.18f));
            }
        }
        for (int i = 0; i < n; i++) {
            float a = axis(i);
            c.line(cx, cy, cx + Canvas.cos(a) * r, cy + Canvas.sin(a) * r, 0.5f, Theme.fade(Theme.ACCENT_CARD, 0.22f));
        }
        float[] pts = new float[n * 2];
        float total = 0;
        for (Gene gene : Gene.VALUES) {
            float fav = catalog.gene(gene).favourability(phenotype.get(gene));
            total += fav;
            float rr = r * Math.max(0.04f, fav) * grow;
            float a = axis(gene.ordinal());
            pts[gene.ordinal() * 2] = cx + Canvas.cos(a) * rr;
            pts[gene.ordinal() * 2 + 1] = cy + Canvas.sin(a) * rr;
        }
        float mean = total / n;
        int fill = Theme.quality(mean);
        c.fan(cx, cy, pts, n, Theme.fade(fill, 0.10f), Theme.fade(fill, 0.42f));
        c.glow(true);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            c.line(pts[i * 2], pts[i * 2 + 1], pts[j * 2], pts[j * 2 + 1], 1.4f, Theme.fade(fill, 0.9f));
        }
        c.glow(false);
        for (Gene gene : Gene.VALUES) {
            int i = gene.ordinal();
            int gc = catalog.gene(gene).color() | 0xFF000000;
            c.bloom(pts[i * 2], pts[i * 2 + 1], 5, Theme.fade(gc, 0.7f));
            c.disc(pts[i * 2], pts[i * 2 + 1], 1.8f, 0xFFFFFFFF, gc);
            float a = axis(i);
            float lx = cx + Canvas.cos(a) * (r + 10);
            float ly = cy + Canvas.sin(a) * (r + 10) - 2.5f;
            Component label = GeneText.gene(gene);
            float scale = 0.6f;
            float lw = font.width(label) * scale;
            float ox = Canvas.cos(a) > 0.3f ? 0 : Canvas.cos(a) < -0.3f ? -lw : -lw / 2f;
            c.text(font, label, lx + ox, ly, scale, gc);
            if (Math.hypot(mouseX - pts[i * 2], mouseY - pts[i * 2 + 1]) < 5) {
                tooltip(c.g(), List.of(GeneText.gene(gene), Component.literal(gene.format(phenotype.get(gene))),
                        Texts.tr(Texts.UI_FAVOURABILITY, Math.round(catalog.gene(gene).favourability(phenotype.get(gene)) * 100))), mouseX, mouseY);
            }
        }
        c.centered(font, Component.literal(Math.round(mean * 100) + "%"), cx, cy - 4, Theme.TEXT);

        float x = leftPos + 206;
        float y = topPos + TOP + 2;
        float w = W - 206 - 10;
        Instruments.section(c, font, Texts.tr(Texts.UI_PROFILE_SCORE), x, y, w, Theme.ACCENT_CARD);
        Instruments.bar(c, x, y + 8, w, 5, mean * grow, fill, time);
        c.right(font, Component.literal(Math.round(mean * 100) + "%"), x + w, y + 15, 0.7f, fill);

        int hetero = 0;
        Set<String> hidden = new LinkedHashSet<>();
        int wild = 0;
        int mutant = 0;
        for (Gene gene : Gene.VALUES) {
            if (!genome.isHomozygous(gene)) {
                hetero++;
            }
            for (int slot = 0; slot < 2; slot++) {
                Allele allele = table.get(gene, genome.allele(gene, slot));
                if (!genome.isHomozygous(gene) && !phenotype.isExpressed(gene, slot)) {
                    hidden.add(allele.id());
                }
                if (allele.wild()) {
                    wild++;
                }
                if (allele.mutationOnly()) {
                    mutant++;
                }
            }
        }
        float sy = y + 24;
        stat(c, x, sy, w, Texts.tr(Texts.UI_STAT_HETERO), hetero + " / 8", hetero == 0 ? Theme.GOOD : Theme.WARN);
        Instruments.segments(c, x, sy + 9, w, 3, 8, hetero, Theme.WARN);
        stat(c, x, sy + 17, w, Texts.tr(Texts.UI_STAT_WILD), String.valueOf(wild), wild > 0 ? Theme.WILD : Theme.TEXT_MUTED);
        stat(c, x, sy + 27, w, Texts.tr(Texts.UI_STAT_MUTANT), String.valueOf(mutant), mutant > 0 ? Theme.MUTANT : Theme.TEXT_MUTED);
        double perAllele = Math.min(0.5, phenotype.get(Gene.MUTATION_RATE) * CultivarConfig.MUTATION_RATE_MULTIPLIER.get());
        double perOffspring = 1.0 - Math.pow(1.0 - perAllele, Gene.COUNT);
        stat(c, x, sy + 37, w, Texts.tr(Texts.UI_STAT_MUTATION), String.format(Locale.ROOT, "%.1f%%", perOffspring * 100), Theme.MUTANT);
        c.text(font, Texts.tr(Texts.UI_STAT_MUTATION_NOTE), x, sy + 46, 0.48f, Theme.TEXT_MUTED);

        Instruments.section(c, font, Texts.tr(Texts.UI_STAT_HIDDEN), x, sy + 58, w, Theme.ACCENT_CARD);
        float chipX = x;
        float chipY = sy + 66;
        if (hidden.isEmpty()) {
            c.text(font, Texts.tr(Texts.NONE), x, chipY, 0.6f, Theme.TEXT_MUTED);
        }
        for (String id : hidden) {
            Allele allele = table.byId(id);
            if (allele == null) {
                continue;
            }
            Component name = Component.translatable(allele.translationKey());
            float cw = font.width(name) * Instruments.MICRO + 6;
            if (chipX + cw > x + w) {
                chipX = x;
                chipY += 9;
            }
            if (chipY > topPos + H - 14) {
                break;
            }
            Instruments.chip(c, font, name, chipX, chipY, GenomeArt.alleleColor(catalog, allele), false);
            chipX += cw + 3;
        }
        c.text(font, Texts.tr(Texts.UI_SELF_CLONAL), leftPos + 8, topPos + H - 9, 0.5f, Theme.TEXT_MUTED);
    }

    private static float axis(int i) {
        return (float) (-Math.PI / 2 + Math.PI * 2 * i / Gene.COUNT);
    }

    private void stat(Canvas c, float x, float y, float w, Component label, String value, int color) {
        c.text(font, label, x, y, 0.58f, Theme.TEXT_DIM);
        c.right(font, Component.literal(value), x + w, y, 0.75f, color);
    }

    private @Nullable CardData partner(CardData self) {
        if (minecraft == null || minecraft.player == null) {
            return null;
        }
        ItemStack main = minecraft.player.getMainHandItem();
        ItemStack off = minecraft.player.getOffhandItem();
        CardData a = main.is(ModItems.GENOME_CARD.get()) ? main.get(ModDataComponents.CARD.get()) : null;
        CardData b = off.is(ModItems.GENOME_CARD.get()) ? off.get(ModDataComponents.CARD.get()) : null;
        CardData other = a != null && a.equals(self) ? b : a;
        if (other == null || other.genome().isEmpty() || !other.species().equals(self.species())) {
            return null;
        }
        return other;
    }

    private void cross(Canvas c, Catalog catalog, CardData card, Genome genome, int mouseX, int mouseY) {
        AlleleTable table = catalog.table();
        CardData partnerCard = partner(card);
        Genome father = partnerCard != null ? partnerCard.genome().get() : genome;
        float x = leftPos + 8;
        float y = topPos + TOP;
        Component partnerLabel = partnerCard != null ? Texts.tr(Texts.UI_CROSS_PARTNER, partnerCard.lineage()) : Texts.tr(Texts.UI_CROSS_SIBLING);
        c.text(font, partnerLabel, x, y, 0.62f, partnerCard != null ? Theme.ACCENT_CARD : Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_CROSS_HINT), x, y + 6, 0.48f, Theme.TEXT_MUTED);

        float gy = y + 16;
        for (Gene gene : Gene.VALUES) {
            float ry = gy + gene.ordinal() * 18;
            boolean active = gene == selected;
            boolean hover = mouseX >= x && mouseX < x + 78 && mouseY >= ry && mouseY < ry + 16;
            int gc = catalog.gene(gene).color() | 0xFF000000;
            c.round(x, ry, 78, 16, 2, active ? Theme.fade(gc, 0.25f) : hover ? 0x14FFFFFF : 0x08FFFFFF);
            c.rect(x, ry, 1.5f, 16, gc);
            if (active) {
                c.frame(x, ry, 78, 16, 0.7f, gc);
            }
            c.text(font, GeneText.gene(gene), x + 4, ry + 2.5f, 0.62f, active ? Theme.TEXT : Theme.TEXT_DIM);
            boolean segregates = PunnettSimulator.expectedGenotypes(genome, father, gene).size() > 1;
            Component tag = Texts.tr(segregates ? Texts.UI_CROSS_SEGREGATES : Texts.UI_CROSS_FIXED);
            c.text(font, tag, x + 4, ry + 9.5f, 0.45f, segregates ? Theme.WARN : Theme.GOOD);
        }

        Gene gene = selected;
        float sk = Anim.easeOutCubic((Util.getMillis() - selectedSince) / 350f);
        float px = leftPos + 112;
        float py = y + 40;
        float cell = 52;
        int gc = catalog.gene(gene).color() | 0xFF000000;
        c.text(font, Texts.tr(Texts.UI_CROSS_MOTHER), px + cell, py - 22, 0.55f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.UI_CROSS_FATHER), px - 4, py + cell * 2 + 4, 0.55f, Theme.TEXT_MUTED);
        for (int m = 0; m < 2; m++) {
            Allele allele = table.get(gene, genome.allele(gene, m));
            gamete(c, catalog, allele, px + m * cell + 2, py - 14, cell - 4);
        }
        for (int f = 0; f < 2; f++) {
            Allele allele = table.get(gene, father.allele(gene, f));
            c.g().pose().pushMatrix();
            c.flush();
            gameteVertical(c, catalog, allele, px - 14, py + f * cell + 2, cell - 4);
            c.g().pose().popMatrix();
        }
        for (int f = 0; f < 2; f++) {
            for (int m = 0; m < 2; m++) {
                int a = genome.allele(gene, m);
                int b = father.allele(gene, f);
                int lo = Math.min(a, b);
                int hi = Math.max(a, b);
                Genome child = genome.withGene(gene, lo, hi);
                Phenotype ph = Phenotype.resolve(child, table);
                float value = ph.get(gene);
                float fav = catalog.gene(gene).favourability(value);
                float delay = (f * 2 + m) * 0.08f;
                float k = Anim.easeOutBack(Anim.clamp01(sk * 1.3f - delay));
                float cxp = px + m * cell + cell / 2f;
                float cyp = py + f * cell + cell / 2f;
                float half = (cell - 4) / 2f * k;
                int q = Theme.quality(fav);
                c.round(cxp - half, cyp - half, half * 2, half * 2, 3, Theme.fade(Theme.shade(q, 0.35f), 0.85f));
                c.frame(cxp - half, cyp - half, half * 2, half * 2, 0.8f, Theme.fade(q, 0.8f));
                if (k > 0.7f) {
                    int ca = GenomeArt.alleleColor(catalog, gene, lo);
                    int cb = GenomeArt.alleleColor(catalog, gene, hi);
                    c.round(cxp - 18, cyp - 16, 17, 6, 2, ca);
                    c.round(cxp + 1, cyp - 16, 17, 6, 2, cb);
                    Allele expressed = table.get(gene, ph.isExpressed(gene, 0) ? child.first(gene) : child.second(gene));
                    String name = font.plainSubstrByWidth(Component.translatable(expressed.translationKey()).getString(), (int) ((cell - 8) / 0.55f));
                    c.centered(font, Component.literal(name), cxp, cyp - 6, 0.55f, Theme.TEXT);
                    c.centered(font, Component.literal(gene.format(value)), cxp, cyp + 2, 0.7f, q);
                    c.centered(font, Component.literal("25%"), cxp, cyp + 12, 0.5f, Theme.TEXT_MUTED);
                }
            }
        }
        Instruments.brackets(c, px - 2, py - 2, cell * 2 + 4, cell * 2 + 4, 6, 1f, gc);

        float dx = leftPos + 236;
        float dy = y + 4;
        float dw = W - 236 - 10;
        Instruments.section(c, font, Texts.tr(Texts.UI_CROSS_OFFSPRING), dx, dy, dw, Theme.ACCENT_CARD);
        Map<String, Double> phenoShares = new LinkedHashMap<>();
        Map<String, Float> phenoValue = new LinkedHashMap<>();
        Map<int[], Double> genotypes = PunnettSimulator.expectedGenotypes(genome, father, gene);
        double homozygous = 0;
        float expectedFav = 0;
        for (Map.Entry<int[], Double> e : genotypes.entrySet()) {
            int[] pair = e.getKey();
            Genome child = genome.withGene(gene, pair[0], pair[1]);
            String cls = Phenotype.phenotypeClass(child, gene, table);
            float value = Phenotype.resolve(child, table).get(gene);
            phenoShares.merge(cls, e.getValue(), Double::sum);
            phenoValue.putIfAbsent(cls, value);
            if (pair[0] == pair[1]) {
                homozygous += e.getValue();
            }
        }
        float ry = dy + 9;
        for (Map.Entry<String, Double> e : phenoShares.entrySet()) {
            float value = phenoValue.get(e.getKey());
            float fav = catalog.gene(gene).favourability(value);
            expectedFav += (float) (fav * e.getValue());
            Component name = className(table, e.getKey());
            c.text(font, name, dx, ry, 0.55f, Theme.TEXT_DIM);
            c.right(font, Component.literal(String.format(Locale.ROOT, "%.0f%%", e.getValue() * 100)), dx + dw, ry, 0.6f, Theme.TEXT);
            Instruments.bar(c, dx, ry + 6, dw, 3, (float) (e.getValue() * sk), Theme.quality(fav), time);
            ry += 14;
            if (ry > dy + 40) {
                break;
            }
        }
        float pgy = dy + 54;
        Instruments.section(c, font, Texts.tr(Texts.UI_CROSS_PARENTS), dx, pgy, dw, Theme.ACCENT_CARD);
        float barW = dw - 22;
        c.text(font, Texts.tr(Texts.UI_CROSS_THIS), dx, pgy + 10, 0.5f, Theme.TEXT_DIM);
        GenomeArt.barcode(c, dx + 22, pgy + 8, barW, 10, genome, catalog);
        c.text(font, Texts.tr(Texts.UI_CROSS_POLLEN), dx, pgy + 23, 0.5f, Theme.TEXT_DIM);
        GenomeArt.barcode(c, dx + 22, pgy + 21, barW, 10, father, catalog);
        float cellW = barW / Gene.COUNT;
        float mark = dx + 22 + gene.ordinal() * cellW;
        c.frame(mark - 0.5f, pgy + 7, cellW + 1, 25, 0.8f, Theme.fade(0xFFFFFFFF, 0.6f + 0.4f * Anim.pulse(time, 1.2f)));

        double pureAll = 1;
        double favAll = 0;
        for (Gene g : Gene.VALUES) {
            double pHom = 0;
            double favG = 0;
            for (Map.Entry<int[], Double> e : PunnettSimulator.expectedGenotypes(genome, father, g).entrySet()) {
                int[] pair = e.getKey();
                if (pair[0] == pair[1]) {
                    pHom += e.getValue();
                }
                Genome child = genome.withGene(g, pair[0], pair[1]);
                favG += e.getValue() * catalog.gene(g).favourability(Phenotype.resolve(child, table).get(g));
            }
            pureAll *= pHom;
            favAll += favG / Gene.COUNT;
        }
        float oy = y + 92;
        Instruments.section(c, font, Texts.tr(Texts.UI_CROSS_ODDS), dx, oy, dw, Theme.ACCENT_CARD);
        stat(c, dx, oy + 9, dw, Texts.tr(Texts.UI_CROSS_HOMOZYGOUS_GENE), String.format(Locale.ROOT, "%.0f%%", homozygous * 100), Theme.TEXT);
        stat(c, dx, oy + 19, dw, Texts.tr(Texts.UI_CROSS_PURE_ALL), formatOdds(pureAll), pureAll > 0.2 ? Theme.GOOD : Theme.WARN);
        stat(c, dx, oy + 29, dw, Texts.tr(Texts.UI_CROSS_MEAN_FAV), String.format(Locale.ROOT, "%.0f%%", favAll * 100), Theme.quality((float) favAll));
        stat(c, dx, oy + 39, dw, Texts.tr(Texts.UI_CROSS_GENE_FAV), String.format(Locale.ROOT, "%.0f%%", expectedFav * 100),
                Theme.quality(expectedFav));
        c.text(font, Texts.tr(Texts.UI_CROSS_EXACT), dx, oy + 50, 0.48f, Theme.TEXT_MUTED);
    }

    private static String formatOdds(double p) {
        if (p >= 0.01) {
            return String.format(Locale.ROOT, "%.1f%%", p * 100);
        }
        if (p <= 0) {
            return "0%";
        }
        return String.format(Locale.ROOT, "1 in %,d", Math.round(1 / p));
    }

    private static Component className(AlleleTable table, String cls) {
        String[] parts = cls.split("\\+");
        MutableComponent out = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            Allele allele = table.byId(parts[i]);
            if (i > 0) {
                out.append(" + ");
            }
            out.append(allele != null ? Component.translatable(allele.translationKey()) : Component.literal(parts[i]));
        }
        return out;
    }

    private void gamete(Canvas c, Catalog catalog, Allele allele, float x, float y, float w) {
        int col = GenomeArt.alleleColor(catalog, allele);
        c.round(x, y, w, 10, 3, Theme.fade(Theme.shade(col, 0.6f), 0.95f));
        c.frame(x, y, w, 10, 0.7f, col);
        String name = font.plainSubstrByWidth(Component.translatable(allele.translationKey()).getString(), (int) ((w - 4) / 0.55f));
        c.centered(font, Component.literal(name), x + w / 2f, y + 3, 0.55f, Theme.TEXT);
    }

    private void gameteVertical(Canvas c, Catalog catalog, Allele allele, float x, float y, float h) {
        int col = GenomeArt.alleleColor(catalog, allele);
        c.round(x, y, 10, h, 3, Theme.fade(Theme.shade(col, 0.6f), 0.95f));
        c.frame(x, y, 10, h, 0.7f, col);
        c.flush();
        c.g().pose().translate(x + 3, y + h / 2f);
        c.g().pose().rotate((float) (-Math.PI / 2));
        String name = font.plainSubstrByWidth(Component.translatable(allele.translationKey()).getString(), (int) ((h - 4) / 0.55f));
        c.centered(font, Component.literal(name), 0, 0, 0.55f, Theme.TEXT);
    }

    private void strainReport(Canvas c, Catalog catalog, Identifier strainId, int mouseX, int mouseY) {
        BlightStrain strain = catalog.strain(strainId).orElse(null);
        float x = leftPos + 8;
        float y = topPos + 20;
        c.glowText(font, GeneText.strain(strainId), x, y, Theme.TEXT, Theme.ACCENT_BLIGHT);
        c.text(font, Texts.tr(Texts.UI_STRAIN_REPORT), x, y + 11, 0.55f, Theme.TEXT_MUTED);
        if (strain == null) {
            c.text(font, Texts.tr(Texts.UNKNOWN), x, y + 30, Theme.TEXT_DIM);
            return;
        }
        int sc = strain.color() | 0xFF000000;
        float cx = leftPos + 72;
        float cy = topPos + 118;
        c.disc(cx, cy, 52, Theme.fade(Theme.shade(sc, 0.3f), 0.5f), 0x00000000);
        for (int i = 0; i < 90; i++) {
            float seedA = Anim.hash(i * 3 + 1) * 6.283f;
            float seedR = (float) Math.sqrt(Anim.hash(i * 5 + 2)) * 46f;
            float a = seedA + time * (0.15f + 0.3f * Anim.hash(i));
            float rr = seedR + Canvas.sin(time * 0.8f + i) * 2f;
            float px = cx + Canvas.cos(a) * rr;
            float py = cy + Canvas.sin(a) * rr * 0.8f;
            float size = 0.8f + Anim.hash(i * 7) * 1.4f;
            c.bloom(px, py, size * 2.6f, Theme.fade(sc, 0.35f));
            c.disc(px, py, size * 0.6f, Theme.shade(sc, 1.3f), sc);
        }
        fx.emit(4f, dt, () -> {
            var r = fx.random();
            fx.spawn(UiParticles.Kind.MOTE, cx + (r.nextFloat() - 0.5f) * 60, cy + 30, (r.nextFloat() - 0.5f) * 6, -10 - r.nextFloat() * 8,
                    3f, 1.2f, Theme.fade(sc, 0.6f));
        });
        fx.draw(c);
        c.ring(cx, cy, 52, 0.7f, Theme.fade(sc, 0.5f));

        float rx = leftPos + 150;
        float ry = topPos + TOP;
        float rw = W - 150 - 10;
        Instruments.section(c, font, Texts.tr(Texts.UI_STRAIN_BEHAVIOUR), rx, ry, rw, Theme.ACCENT_BLIGHT);
        Instruments.ring(c, rx + 16, ry + 26, 11, 3, strain.transmission(), Theme.ACCENT_BLIGHT, time);
        c.centered(font, Component.literal(Math.round(strain.transmission() * 100) + "%"), rx + 16, ry + 23, 0.6f, Theme.TEXT);
        c.centered(font, Texts.tr(Texts.UI_STRAIN_TRANSMISSION), rx + 16, ry + 42, 0.45f, Theme.TEXT_MUTED);
        stat(c, rx + 36, ry + 12, rw - 36, Texts.tr(Texts.UI_STRAIN_LETHAL), String.valueOf(strain.lethalTicks()), Theme.BAD);
        Instruments.segments(c, rx + 36, ry + 21, rw - 36, 3, 12, strain.lethalTicks(), Theme.BAD);
        stat(c, rx + 36, ry + 28, rw - 36, Texts.tr(Texts.UI_STRAIN_SPONTANEOUS),
                String.format(Locale.ROOT, "%.1f%%", strain.spontaneousChance() * 100), Theme.WARN);

        Instruments.section(c, font, Texts.tr(Texts.UI_STRAIN_HOSTS), rx, ry + 52, rw, Theme.ACCENT_BLIGHT);
        float hx = rx;
        for (Identifier species : strain.species()) {
            CropSpecies crop = CropSpecies.byKey(species);
            if (crop != null) {
                c.flush();
                c.g().item(new ItemStack(ModItems.produce(crop)), (int) hx, (int) (ry + 60));
            }
            if (mouseX >= hx && mouseX < hx + 16 && mouseY >= ry + 60 && mouseY < ry + 76) {
                tooltip(c.g(), List.of(GeneText.species(species)), mouseX, mouseY);
            }
            hx += 20;
        }

        Instruments.section(c, font, Texts.tr(Texts.LABEL_STRAIN_DEFEATED), rx, ry + 84, rw, Theme.GOOD);
        float dy = ry + 93;
        AlleleTable table = catalog.table();
        for (String alleleId : strain.defeatedBy()) {
            Allele allele = table.byId(alleleId);
            int col = allele != null ? GenomeArt.alleleColor(catalog, allele) : Theme.GOOD;
            c.haloRect(rx, dy, rw, 12, 2, Theme.fade(col, 0.2f));
            c.round(rx, dy, rw, 12, 3, Theme.fade(Theme.shade(col, 0.5f), 0.9f));
            c.frame(rx, dy, rw, 12, 0.7f, col);
            Icons.draw(c, Icon.SHIELD, rx + 7, dy + 6, 7, Theme.GOOD);
            Component name = allele != null ? GeneText.allele(allele) : Component.literal(alleleId);
            c.text(font, name, rx + 14, dy + 3, 0.7f, Theme.TEXT);
            if (allele != null) {
                c.right(font, Component.literal(Gene.BLIGHT_RESISTANCE.format(table.effectiveValue(allele))), rx + rw - 4, dy + 3, 0.7f, Theme.GOOD);
            }
            dy += 15;
        }
        c.text(font, Texts.tr(Texts.UI_STRAIN_NOTE), x, topPos + H - 9, 0.5f, Theme.TEXT_MUTED);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (menu.card().genome().isPresent()) {
            float x = leftPos + W - 8 - TAB_W * 3;
            float y = topPos + 20;
            Tab[] all = Tab.values();
            for (int i = 0; i < all.length; i++) {
                float tx = x + i * TAB_W;
                if (mx >= tx && mx < tx + TAB_W - 2 && my >= y && my < y + 14) {
                    if (tab != all[i]) {
                        tab = all[i];
                        tabSince = Util.getMillis();
                        click();
                    }
                    return true;
                }
            }
            if (tab == Tab.CROSS) {
                float gx = leftPos + 8;
                float gy = topPos + TOP + 16;
                for (Gene gene : Gene.VALUES) {
                    float ry = gy + gene.ordinal() * 18;
                    if (mx >= gx && mx < gx + 78 && my >= ry && my < ry + 16) {
                        if (selected != gene) {
                            selected = gene;
                            selectedSince = Util.getMillis();
                            click();
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void click() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4f));
        }
    }
}
