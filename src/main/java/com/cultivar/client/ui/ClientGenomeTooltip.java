package com.cultivar.client.ui;

import com.cultivar.component.GenomeTooltip;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Phenotype;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public final class ClientGenomeTooltip implements ClientTooltipComponent {
    private static final int CELL = 13;
    private final GenomeTooltip tooltip;

    public ClientGenomeTooltip(GenomeTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight(Font font) {
        return 28;
    }

    @Override
    public int getWidth(Font font) {
        return CELL * Gene.COUNT;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor g) {
        var level = Minecraft.getInstance().level;
        Catalog catalog = level != null ? Catalog.get(level.registryAccess()) : Catalog.defaults();
        Canvas c = new Canvas(g);
        float bw = CELL * Gene.COUNT;
        c.rect(x, y + 2, bw, 14, 0xFF071014);
        GenomeArt.barcode(c, x + 0.5f, y + 2.5f, bw - 1, 13, tooltip.genome(), catalog);
        var phenotype = Phenotype.resolve(tooltip.genome(), catalog.table());
        for (Gene gene : Gene.VALUES) {
            float gx = x + gene.ordinal() * CELL;
            float fav = catalog.gene(gene).favourability(phenotype.get(gene));
            c.rect(gx + 1, y + 17, (CELL - 2) * fav, 1.2f, Theme.quality(fav));
            c.centered(font, Component.literal(gene.shortCode().toUpperCase(Locale.ROOT)), gx + CELL / 2f, y + 20, 0.5f,
                    catalog.gene(gene).color() | 0xFF000000);
        }
        c.flush();
    }
}
