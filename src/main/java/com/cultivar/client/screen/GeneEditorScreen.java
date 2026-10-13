package com.cultivar.client.screen;

import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.client.guide.GuideClient;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.menu.GeneEditorMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GeneEditorScreen extends CultivarContainerScreen<GeneEditorMenu> {
    private static final int MID = 128;
    private final Anim.Smooth progress = new Anim.Smooth(10f);
    private @Nullable LabButton edit;

    public GeneEditorScreen(GeneEditorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GeneEditorMenu.WIDTH, GeneEditorMenu.HEIGHT);
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
        return menu.running();
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return switch (slot.getContainerSlot()) {
            case GeneEditorBlockEntity.SLOT_PACKET -> Icon.SEED;
            case GeneEditorBlockEntity.SLOT_REAGENT -> Icon.FLASK;
            default -> null;
        };
    }

    private AlleleTable table() {
        return (minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults()).table();
    }

    private Gene gene() {
        return Gene.VALUES[Math.floorMod(menu.gene(), Gene.COUNT)];
    }

    private @Nullable Allele selected() {
        AlleleTable table = table();
        return table.has(gene(), menu.alleleCode()) ? table.get(gene(), menu.alleleCode()) : null;
    }

    private @Nullable Genome genome() {
        return menu.getSlot(GeneEditorBlockEntity.SLOT_PACKET).getItem().get(ModDataComponents.GENOME.get());
    }

    @Override
    protected void init() {
        super.init();
        int y = topPos + 22;
        addRenderableWidget(new LabButton(leftPos + MID - 70, y, 14, 12, Component.empty(), Icon.LEFT, Theme.ACCENT_LAB, in -> clickButton(0)));
        addRenderableWidget(new LabButton(leftPos + MID + 56, y, 14, 12, Component.empty(), Icon.RIGHT, Theme.ACCENT_LAB, in -> clickButton(1)));
        int ay = topPos + 76;
        addRenderableWidget(new LabButton(leftPos + MID + 4, ay, 14, 12, Component.empty(), Icon.LEFT, Theme.MUTANT, in -> clickButton(2)));
        addRenderableWidget(new LabButton(leftPos + MID + 70, ay, 14, 12, Component.empty(), Icon.RIGHT, Theme.MUTANT, in -> clickButton(3)));
        edit = addRenderableWidget(new LabButton(leftPos + MID - 36, topPos + 106, 72, 14, Texts.tr(Texts.UI_GE_EDIT), Icon.HELIX, Theme.ACCENT_LAB,
                in -> clickButton(5)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (edit != null) {
            Allele allele = selected();
            Genome genome = genome();
            edit.active = !menu.running() && genome != null && allele != null && known(allele)
                    && menu.getSlot(GeneEditorBlockEntity.SLOT_REAGENT).hasItem() && !menu.getSlot(GeneEditorBlockEntity.SLOT_OUTPUT).hasItem()
                    && genome.allele(gene(), menu.copy()) != allele.code();
        }
    }

    private static boolean known(Allele allele) {
        return GuideClient.done(AlleleAtlas.PREFIX + allele.id());
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        float x0 = leftPos;
        float y0 = topPos;
        float mid = x0 + MID;
        AlleleTable table = table();
        Gene gene = gene();
        Genome genome = genome();
        Allele target = selected();
        float p = progress.update(menu.progress(), dt);

        c.text(font, Texts.tr(Texts.UI_GE_PACKET), x0 + 13, y0 + 28, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_GE_REAGENT), x0 + 13, y0 + 74, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_GE_RESULT), x0 + 218, y0 + 50, Instruments.MICRO, Theme.TEXT_DIM);

        c.round(mid - 54, y0 + 22, 108, 12, 2, 0x14FFFFFF);
        c.centered(font, Component.translatable(gene.translationKey()), mid, y0 + 24.5f, 0.75f, Theme.ACCENT_LAB);
        c.centered(font, Texts.tr(Texts.UI_GE_GENE, gene.ordinal() + 1, Gene.COUNT), mid, y0 + 36, 0.5f, Theme.TEXT_MUTED);

        for (int copy = 0; copy < 2; copy++) {
            float bx = mid - 54 + copy * 56;
            float by = y0 + 44;
            boolean targeted = copy == menu.copy();
            boolean hover = mouseX >= bx && mouseX < bx + 52 && mouseY >= by && mouseY < by + 24;
            c.round(bx, by, 52, 24, 3, targeted ? Theme.fade(Theme.ACCENT_LAB, 0.18f) : hover ? 0x18FFFFFF : 0x0AFFFFFF);
            if (targeted) {
                Instruments.brackets(c, bx - 1, by - 1, 54, 26, 3, 0.8f, Theme.ACCENT_LAB);
            }
            c.text(font, Texts.tr(copy == 0 ? Texts.UI_GE_COPY_1 : Texts.UI_GE_COPY_2), bx + 3, by + 3, 0.5f, targeted ? Theme.ACCENT_LAB : Theme.TEXT_DIM);
            if (genome != null && table.has(gene, genome.allele(gene, copy))) {
                Allele current = table.get(gene, genome.allele(gene, copy));
                String name = font.plainSubstrByWidth(Component.translatable(current.translationKey()).getString(), (int) (46 / 0.6f));
                c.text(font, Component.literal(name), bx + 3, by + 11, 0.6f, Theme.TEXT);
                c.text(font, Component.literal(value(current)), bx + 3, by + 18, 0.45f, Theme.TEXT_MUTED);
            } else {
                c.text(font, Texts.tr(Texts.UI_GE_NO_PACKET), bx + 3, by + 12, 0.5f, Theme.TEXT_MUTED);
            }
            if (hover && !targeted) {
                tooltip(c.g(), List.of(Texts.tr(Texts.UI_GE_SWITCH_COPY)), mouseX, mouseY);
            }
        }

        float ay = y0 + 76;
        c.text(font, Texts.tr(Texts.UI_GE_REPLACE), mid - 54, ay + 3, 0.55f, Theme.TEXT_DIM);
        c.round(mid + 19, ay, 50, 12, 2, 0x14FFFFFF);
        if (target != null) {
            boolean known = known(target);
            String name = font.plainSubstrByWidth(Component.translatable(target.translationKey()).getString(), (int) (46 / 0.6f));
            c.centered(font, known ? Component.literal(name) : Texts.tr(Texts.UI_GE_LOCKED), mid + 44, ay + 3.5f, 0.6f, known ? Theme.MUTANT : Theme.TEXT_MUTED);
            c.centered(font, Component.literal(known ? value(target) : "?"), mid + 44, ay + 15, 0.45f, Theme.TEXT_MUTED);
            if (mouseX >= mid + 19 && mouseX < mid + 69 && mouseY >= ay && mouseY < ay + 12) {
                tooltip(c.g(), known ? List.of(Component.translatable(target.translationKey()), Texts.tr(Texts.UI_GE_VALUE, value(target)),
                        Texts.tr(Texts.UI_GE_DOMINANCE, target.dominance())) : List.of(Texts.tr(Texts.UI_GE_LOCKED), Texts.tr(Texts.UI_GE_LOCKED_HELP)),
                        mouseX, mouseY);
            }
        }

        helix(c, x0 + 44, y0 + 98, 164, p, genome != null);

        if (menu.running()) {
            Instruments.bar(c, mid - 36, y0 + 122, 72, 3, p, Theme.ACCENT_LAB, time);
        }
        int recorded = 0;
        for (Allele allele : table.all()) {
            if (known(allele)) {
                recorded++;
            }
        }
        c.right(font, Texts.tr(Texts.UI_GE_ATLAS, recorded, table.all().size()), x0 + imageWidth - 8, y0 + 120, 0.5f, Theme.TEXT_DIM);
        if (mouseX >= x0 + imageWidth - 70 && mouseX < x0 + imageWidth - 8 && mouseY >= y0 + 118 && mouseY < y0 + 126) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_GE_ATLAS_HELP)), mouseX, mouseY);
        }
        ItemStack packet = menu.getSlot(GeneEditorBlockEntity.SLOT_PACKET).getItem();
        if (packet.isEmpty()) {
            c.text(font, Texts.tr(Texts.UI_GE_HINT), x0 + 8, y0 + 120, 0.5f, Theme.fade(Theme.TEXT_MUTED, 0.7f + 0.3f * Anim.pulse(time, 0.5f)));
        }
    }

    private void helix(Canvas c, float x, float y, float w, float p, boolean loaded) {
        int rungs = 24;
        int locus = gene().ordinal() * 3 + 1;
        float phase = time * (menu.running() ? 4f : 1f);
        for (int i = 0; i < rungs; i++) {
            float t = i / (float) (rungs - 1);
            float px = x + t * w;
            float a = phase + t * (float) Math.PI * 4f;
            float s = Canvas.sin(a);
            float y1 = y + s * 5f;
            float y2 = y - s * 5f;
            boolean mark = i == locus;
            int strand = loaded ? Theme.fade(Theme.ACCENT_LAB, 0.35f + 0.4f * (s * 0.5f + 0.5f)) : 0x30FFFFFF;
            c.line(px, y1, px, y2, mark ? 1.4f : 0.6f, mark ? Theme.MUTANT : Theme.fade(strand, 0.6f));
            c.disc(px, y1, 1.1f, strand, strand);
            c.disc(px, y2, 1.1f, Theme.fade(Theme.MUTANT, 0.5f), Theme.fade(Theme.MUTANT, 0.5f));
            if (mark) {
                c.bloom(px, y, 7 + 3 * p, Theme.fade(Theme.MUTANT, 0.25f + 0.25f * Anim.pulse(time, 2f)));
            }
        }
    }

    private static String value(Allele allele) {
        return String.format(Locale.ROOT, "%.2f", allele.value());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        float mid = leftPos + MID;
        float by = topPos + 44;
        if (event.y() >= by && event.y() < by + 24 && !menu.running()) {
            int copy = event.x() >= mid - 54 && event.x() < mid - 2 ? 0 : event.x() >= mid + 2 && event.x() < mid + 54 ? 1 : -1;
            if (copy >= 0 && copy != menu.copy()) {
                clickButton(4);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
