package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.CropSpecies;
import com.cultivar.menu.SeedBankLine;
import com.cultivar.menu.SeedBankMenu;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class SeedBankScreen extends CultivarContainerScreen<SeedBankMenu> {
    private static final int LIST_X = 54;
    private static final int LIST_Y = 38;
    private static final int LIST_W = 194;
    private static final int ROW_H = 16;
    private static final int ROWS = 5;
    private static final long ARM_MILLIS = 2500;

    private EditBox search;
    private LabButton deposit;
    private float scroll;
    private final Anim.Smooth scrollShown = new Anim.Smooth(16f);
    private int armedIndex = -1;
    private long armedAt;
    private int lastCount = -1;
    private float flashNew;

    public SeedBankScreen(SeedBankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SeedBankMenu.WIDTH, SeedBankMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_BANK;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.GRID;
    }

    @Override
    protected boolean working() {
        return !menu.clientLines().isEmpty();
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return switch (slot.getContainerSlot()) {
            case SeedBankBlockEntity.SLOT_DEPOSIT -> Icon.SEED;
            case SeedBankBlockEntity.SLOT_PAPER -> Icon.PAPER;
            case SeedBankBlockEntity.SLOT_OUTPUT -> Icon.COPY;
            default -> null;
        };
    }

    @Override
    protected void init() {
        super.init();
        String previous = search != null ? search.getValue() : "";
        search = new EditBox(font, leftPos + LIST_X + 12, topPos + 22, 100, 10, Texts.tr(Texts.UI_SEARCH));
        search.setBordered(false);
        search.setMaxLength(40);
        search.setTextColor(Theme.TEXT);
        search.setHint(Texts.tr(Texts.UI_SEARCH_HINT));
        search.setValue(previous);
        search.setResponder(text -> scroll = 0);
        addRenderableWidget(search);
        deposit = addRenderableWidget(new LabButton(leftPos + 34, topPos + 30, 16, 18, Component.empty(), Icon.DEPOSIT, Theme.ACCENT_BANK,
                input -> clickButton(0)));
    }

    private List<Integer> visible() {
        List<SeedBankLine> lines = menu.clientLines();
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            SeedBankLine line = lines.get(i);
            if (query.isEmpty() || line.name().toLowerCase(Locale.ROOT).contains(query)
                    || GeneText.species(line.species()).getString().toLowerCase(Locale.ROOT).contains(query)
                    || Long.toString(line.lineage()).contains(query)) {
                out.add(i);
            }
        }
        return out;
    }

    private float maxScroll(int count) {
        return Math.max(0, count * ROW_H - ROWS * ROW_H);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack incoming = menu.getSlot(SeedBankBlockEntity.SLOT_DEPOSIT).getItem();
        deposit.active = !incoming.isEmpty() && menu.clientLines().size() < menu.clientCapacity();
    }

    private ItemStack icon(SeedBankLine line) {
        if (line.wild()) {
            return new ItemStack(ModItems.WILD_SEED.get());
        }
        CropSpecies species = CropSpecies.byKey(line.species());
        return species != null ? new ItemStack(ModItems.seedPacket(species)) : new ItemStack(Items.WHEAT_SEEDS);
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        List<SeedBankLine> lines = menu.clientLines();
        if (lastCount >= 0 && lines.size() > lastCount) {
            flashNew = 1f;
        }
        lastCount = lines.size();
        flashNew = Math.max(0f, flashNew - dt * 1.2f);
        List<Integer> shown = visible();
        scroll = Math.max(0, Math.min(scroll, maxScroll(shown.size())));
        float offset = scrollShown.update(scroll, dt);

        float sx = leftPos + LIST_X;
        float sy = topPos + 20;
        Instruments.well(c, sx, sy, 116, 13);
        Icons.draw(c, Icon.SEARCH, sx + 6, sy + 6.5f, 7, search.isFocused() ? Theme.ACCENT_BANK : Theme.TEXT_MUTED);
        if (search.isFocused()) {
            c.haloRect(sx, sy, 116, 13, 3, Theme.fade(Theme.ACCENT_BANK, 0.3f));
        }

        int capacity = Math.max(1, menu.clientCapacity());
        int cells = Math.min(24, capacity);
        float perCell = capacity / (float) cells;
        float cx0 = sx + 122;
        float cw = (LIST_W - 122) / (float) cells;
        for (int i = 0; i < cells; i++) {
            float fill = Anim.clamp01(lines.size() / perCell - i);
            float px = cx0 + i * cw;
            float py = sy + 1;
            c.rect(px, py, cw - 0.8f, 5, 0xFF0D1A1C);
            if (fill > 0) {
                int lit = Theme.lerp(Theme.ACCENT_BANK, 0xFFFFFFFF, flashNew * (i == (int) ((lines.size() - 1) / perCell) ? 1f : 0.2f));
                c.rect(px, py, cw - 0.8f, 5, Theme.fade(lit, 0.35f + 0.65f * fill));
            }
        }
        c.right(font, Texts.tr(Texts.LABEL_LINES, lines.size(), menu.clientCapacity()), sx + LIST_W, sy + 8, 0.55f, Theme.TEXT_DIM);

        float lx = sx;
        float ly = topPos + LIST_Y;
        float lh = ROWS * ROW_H;
        Instruments.well(c, lx - 1, ly - 1, LIST_W + 2, lh + 2);
        c.scissor((int) lx, (int) ly, (int) (lx + LIST_W), (int) (ly + lh));
        int paper = menu.getSlot(SeedBankBlockEntity.SLOT_PAPER).getItem().getCount();
        int cost = CultivarConfig.SEED_BANK_WITHDRAW_COST.get();
        long now = Util.getMillis();
        if (armedIndex >= 0 && now - armedAt > ARM_MILLIS) {
            armedIndex = -1;
        }
        SeedBankLine hoveredLine = null;
        Component hoveredAction = null;
        for (int r = 0; r < shown.size(); r++) {
            float ry = ly + r * ROW_H - offset;
            if (ry + ROW_H < ly || ry > ly + lh) {
                continue;
            }
            int index = shown.get(r);
            SeedBankLine line = lines.get(index);
            boolean rowHover = mouseX >= lx && mouseX < lx + LIST_W - 6 && mouseY >= Math.max(ly, ry) && mouseY < Math.min(ly + lh, ry + ROW_H);
            boolean armed = armedIndex == index;
            int base = r % 2 == 0 ? 0x10FFFFFF : 0x06FFFFFF;
            c.rect(lx, ry, LIST_W - 6, ROW_H - 1, armed ? Theme.fade(Theme.BAD, 0.22f) : rowHover ? Theme.fade(Theme.ACCENT_BANK, 0.14f) : base);
            if (rowHover) {
                c.rect(lx, ry, 1.2f, ROW_H - 1, Theme.ACCENT_BANK);
            }
            if (index == lines.size() - 1 && flashNew > 0) {
                c.haloRect(lx + 1, ry + 1, LIST_W - 8, ROW_H - 3, 3, Theme.fade(Theme.ACCENT_BANK, flashNew * 0.6f));
            }
            c.flush();
            c.g().pose().pushMatrix();
            c.g().pose().translate(lx + 3, ry + 1.5f);
            c.g().pose().scale(0.75f, 0.75f);
            c.g().item(icon(line), 0, 0);
            c.g().pose().popMatrix();
            String name = font.plainSubstrByWidth(line.name(), 96);
            c.text(font, Component.literal(name), lx + 18, ry + 1.5f, line.wild() ? Theme.WILD : Theme.TEXT);
            Component meta = Texts.tr(Texts.UI_LINE_META, GeneText.species(line.species()), line.lineage(), line.storedAt() / 24000L);
            c.text(font, meta, lx + 18, ry + 10, 0.5f, Theme.TEXT_MUTED);
            float badgeX = lx + 118;
            if (line.wild()) {
                Instruments.chip(c, font, Texts.tr(Texts.UI_BADGE_WILD), badgeX, ry + 4, Theme.WILD, false);
            }
            float ax = lx + LIST_W - 36;
            boolean canCopy = paper >= cost;
            boolean copyHover = mouseX >= ax && mouseX < ax + 13 && mouseY >= ry + 1 && mouseY < ry + 14 && rowHover;
            boolean delHover = mouseX >= ax + 15 && mouseX < ax + 28 && mouseY >= ry + 1 && mouseY < ry + 14 && rowHover;
            actionButton(c, ax, ry + 1, Icon.COPY, canCopy ? Theme.ACCENT_BANK : Theme.TEXT_MUTED, copyHover);
            actionButton(c, ax + 15, ry + 1, armed ? Icon.CHECK : Icon.CROSS, Theme.BAD, delHover);
            if (rowHover) {
                hoveredLine = line;
                if (copyHover) {
                    hoveredAction = Texts.tr(canCopy ? Texts.LABEL_COST : Texts.UI_COPY_NEEDS_PAPER, cost);
                } else if (delHover) {
                    hoveredAction = Texts.tr(armed ? Texts.UI_DISCARD_CONFIRM : Texts.UI_DISCARD);
                }
            }
        }
        c.endScissor();
        if (shown.isEmpty()) {
            Component empty = Texts.tr(lines.isEmpty() ? Texts.LABEL_NO_LINES : Texts.UI_NO_MATCH);
            c.centered(font, empty, lx + LIST_W / 2f, ly + lh / 2f - 4, Theme.fade(Theme.TEXT_DIM, 0.6f + 0.4f * Anim.pulse(time, 0.6f)));
        }
        float max = maxScroll(shown.size());
        float trackX = lx + LIST_W - 4;
        c.rect(trackX, ly + 1, 2, lh - 2, 0xFF0F1D20);
        if (max > 0) {
            float thumb = Math.max(10, lh * (ROWS / (float) shown.size()));
            float ty = ly + 1 + (lh - 2 - thumb) * (offset / max);
            c.rect(trackX, ty, 2, thumb, Theme.ACCENT_BANK);
            c.bloom(trackX + 1, ty + thumb / 2, 4, Theme.fade(Theme.ACCENT_BANK, 0.4f));
        }
        Instruments.brackets(c, lx - 2, ly - 2, LIST_W + 4, lh + 4, 5, 1f, Theme.fade(Theme.ACCENT_BANK, 0.8f));

        c.text(font, Texts.tr(Texts.LABEL_DEPOSIT), leftPos + 13, topPos + 21, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.LABEL_PAPER), leftPos + 13, topPos + 55, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.LABEL_WITHDRAW), leftPos + 13, topPos + 89, Instruments.MICRO, Theme.TEXT_DIM);

        if (hoveredLine != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal(hoveredLine.name()));
            tip.add(GeneText.species(hoveredLine.species()));
            tip.add(Texts.tr(Texts.TIP_LINEAGE, hoveredLine.lineage()));
            tip.add(Texts.tr(Texts.UI_STORED_DAY, hoveredLine.storedAt() / 24000L));
            if (hoveredAction != null) {
                tip.add(hoveredAction);
            }
            tooltip(c.g(), tip, mouseX, mouseY);
        }
    }

    private void actionButton(Canvas c, float x, float y, Icon icon, int color, boolean hover) {
        c.round(x, y, 13, 13, 2, hover ? Theme.fade(color, 0.3f) : 0x30000000);
        c.frame(x, y, 13, 13, 0.7f, Theme.fade(color, hover ? 1f : 0.5f));
        if (hover) {
            c.haloRect(x, y, 13, 13, 3, Theme.fade(color, 0.4f));
        }
        Icons.draw(c, icon, x + 6.5f, y + 6.5f, 8, hover ? 0xFFFFFFFF : color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        float lx = leftPos + LIST_X;
        float ly = topPos + LIST_Y;
        if (mx >= lx && mx < lx + LIST_W - 6 && my >= ly && my < ly + ROWS * ROW_H) {
            List<Integer> shown = visible();
            int r = (int) ((my - ly + scrollShown.value()) / ROW_H);
            if (r >= 0 && r < shown.size()) {
                int index = shown.get(r);
                float ry = ly + r * ROW_H - scrollShown.value();
                float ax = lx + LIST_W - 36;
                if (my >= ry + 1 && my < ry + 14) {
                    if (mx >= ax && mx < ax + 13) {
                        playClick();
                        clickButton(SeedBankMenu.WITHDRAW_BASE + index);
                        return true;
                    }
                    if (mx >= ax + 15 && mx < ax + 28) {
                        playClick();
                        if (armedIndex == index) {
                            armedIndex = -1;
                            clickButton(SeedBankMenu.DELETE_BASE + index);
                        } else {
                            armedIndex = index;
                            armedAt = Util.getMillis();
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2f));
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        float lx = leftPos + LIST_X;
        float ly = topPos + LIST_Y;
        if (x >= lx && x < lx + LIST_W && y >= ly && y < ly + ROWS * ROW_H) {
            scroll = Math.max(0, Math.min(maxScroll(visible().size()), scroll - (float) scrollY * ROW_H));
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (search != null && search.isFocused() && !event.isEscape()) {
            return search.keyPressed(event) || search.canConsumeInput() || super.keyPressed(event);
        }
        return super.keyPressed(event);
    }
}
