package com.cultivar.client.screen;

import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.market.Grade;
import com.cultivar.menu.ProcessingStationMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Texts;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ProcessingStationScreen extends CultivarContainerScreen<ProcessingStationMenu> {
    private static final int CX = 109;
    private static final int CY = 54;
    private final Anim.Smooth progress = new Anim.Smooth(10f);
    private final Anim.Smooth speed = new Anim.Smooth(3f).from(0f);
    private float spin;

    public ProcessingStationScreen(ProcessingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ProcessingStationMenu.WIDTH, ProcessingStationMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_COMPOST;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.SPLIT;
    }

    @Override
    protected boolean working() {
        return menu.progress() > 0f;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return slot.getContainerSlot() == ProcessingStationBlockEntity.SLOT_INPUT ? Icon.SEED : null;
    }

    private @Nullable ProcessingStationBlockEntity station() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.pos()) instanceof ProcessingStationBlockEntity s ? s : null;
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        boolean boosted = menu.boostSeconds() > 0;
        float p = progress.update(menu.progress(), dt);
        float v = speed.update(menu.progress() > 0f ? (boosted ? 2f : 1f) : 0f, dt);
        spin += dt * v * 3f;
        float x0 = leftPos;
        float y0 = topPos;
        float cx = x0 + CX;
        float cy = y0 + CY;
        ItemStack input = menu.getSlot(ProcessingStationBlockEntity.SLOT_INPUT).getItem();
        ProcessingStationBlockEntity station = station();
        ItemStack preview = station != null ? station.preview() : ItemStack.EMPTY;

        c.text(font, Texts.tr(Texts.UI_PS_INPUT), x0 + 29, y0 + 38, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_PS_OUTPUT), x0 + 170, y0 + 38, Instruments.MICRO, Theme.TEXT_DIM);

        float belt = time * 12f * Math.max(0.15f, v);
        int beltColor = Theme.fade(Theme.ACCENT_COMPOST, preview.isEmpty() ? 0.25f : 0.7f);
        c.rect(x0 + 49, cy + 9, cx - x0 - 70, 3, 0xFF14201F);
        c.dashed(x0 + 50, cy + 10.5f, cx - 22, cy + 10.5f, 1.2f, 3, 2, -belt, beltColor);
        c.rect(cx + 22, cy + 9, x0 + 168 - cx - 22, 3, 0xFF14201F);
        c.dashed(cx + 22, cy + 10.5f, x0 + 168, cy + 10.5f, 1.2f, 3, 2, -belt, beltColor);

        c.shadowRect(cx - 24, cy - 20, 48, 36, 3, 0x60000000);
        c.round(cx - 24, cy - 20, 48, 36, 4, 0xFF1A2624);
        c.frame(cx - 24, cy - 20, 48, 36, 0.8f, Theme.fade(Theme.ACCENT_COMPOST, 0.45f));
        roller(c, cx - 10, cy - 3, spin);
        roller(c, cx + 10, cy - 3, -spin);
        if (p > 0f) {
            c.bloom(cx, cy - 3, 14, Theme.fade(boosted ? Theme.ACCENT_BANK : Theme.ACCENT_COMPOST, 0.18f + 0.1f * Anim.pulse(time, 2f)));
            float squeeze = 2f + 4f * p;
            c.rectV(cx - 1.2f, cy + 3, 2.4f, squeeze, Theme.fade(Theme.ACCENT_COMPOST, 0.9f), Theme.fade(Theme.ACCENT_COMPOST, 0.1f));
        }

        if (!preview.isEmpty()) {
            c.flush();
            c.g().item(preview, (int) cx - 8, (int) y0 + 17);
            c.g().itemDecorations(font, preview, (int) cx - 8, (int) y0 + 17);
            Component makes = Texts.tr(Texts.UI_PS_MAKES, station.batch(), input.getHoverName(), preview.getCount(), preview.getHoverName());
            String line = font.plainSubstrByWidth(makes.getString(), (int) ((imageWidth - 20) / 0.55f));
            c.centered(font, Component.literal(line), cx, y0 + 80, 0.55f, Theme.TEXT);
            if (mouseX >= cx - 8 && mouseX < cx + 8 && mouseY >= y0 + 17 && mouseY < y0 + 33) {
                c.g().setTooltipForNextFrame(font, preview, mouseX, mouseY);
            }
        } else {
            c.centered(font, Texts.tr(input.isEmpty() ? Texts.UI_PS_IDLE : Texts.UI_PS_NO_RECIPE), cx, y0 + 80, 0.55f,
                    Theme.fade(input.isEmpty() ? Theme.TEXT_MUTED : Theme.BAD, 0.7f + 0.3f * Anim.pulse(time, 0.5f)));
        }

        Instruments.bar(c, x0 + 30, y0 + 88, imageWidth - 60, 4, p, boosted ? Theme.ACCENT_BANK : Theme.ACCENT_COMPOST, time);
        Grade grade = input.get(ModDataComponents.GRADE.get());
        float gy = y0 + 96;
        if (grade != null) {
            Instruments.chip(c, font, Texts.tr(Texts.UI_PS_GRADE_KEPT, Texts.tr(grade.translationKey())), x0 + 30, gy, grade.color(), grade == Grade.PRIME);
        } else if (!input.isEmpty()) {
            Instruments.chip(c, font, Texts.tr(Texts.UI_PS_UNGRADED), x0 + 30, gy, Theme.TEXT_MUTED, false);
        }
        Instruments.led(c, x0 + imageWidth - 80, gy + 3, Theme.ACCENT_BANK, boosted, time);
        c.text(font, boosted ? Texts.tr(Texts.UI_PS_OILED, menu.boostSeconds()) : Texts.tr(Texts.UI_PS_NOT_OILED), x0 + imageWidth - 75, gy + 1, 0.55f,
                boosted ? Theme.ACCENT_BANK : Theme.TEXT_MUTED);

        if (mouseX >= cx - 24 && mouseX < cx + 24 && mouseY >= cy - 20 && mouseY < cy + 16) {
            tooltip(c.g(), List.of(Texts.tr(Texts.SCREEN_PROCESSING), Texts.tr(Texts.UI_PS_HELP_1), Texts.tr(Texts.UI_PS_HELP_2),
                    Texts.tr(Texts.UI_PS_HELP_3)), mouseX, mouseY);
        }
        if (mouseX >= x0 + imageWidth - 82 && mouseX < x0 + imageWidth - 8 && mouseY >= gy && mouseY < gy + 8) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_PS_OIL_HELP)), mouseX, mouseY);
        }
    }

    private static void roller(Canvas c, float x, float y, float angle) {
        c.disc(x, y, 9, 0xFF3A4A46, 0xFF22302D);
        c.ring(x, y, 9, 1f, 0xFF52645F);
        for (int i = 0; i < 6; i++) {
            float a = angle + i * (float) Math.PI / 3f;
            c.line(x, y, x + Canvas.cos(a) * 8f, y + Canvas.sin(a) * 8f, 0.9f, 0xFF607570);
        }
        for (int i = 0; i < 12; i++) {
            float a = angle + i * (float) Math.PI / 6f;
            c.disc(x + Canvas.cos(a) * 9.4f, y + Canvas.sin(a) * 9.4f, 0.9f, 0xFF6E827C, 0xFF6E827C);
        }
        c.disc(x, y, 2.2f, Theme.ACCENT_COMPOST, Theme.fade(Theme.ACCENT_COMPOST, 0.5f));
    }
}
