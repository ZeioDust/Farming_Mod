package com.cultivar.client.screen;

import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Chassis;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.menu.MachineMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

public abstract class CultivarContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    protected static final int HEADER = Chassis.HEADER;
    protected final UiParticles fx = new UiParticles();
    private final UiParticles backdrop = new UiParticles();
    protected float time;
    protected float dt;
    private long lastFrame;
    private long openedAt;
    private int inventoryTop = -1;

    protected CultivarContainerScreen(M menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
    }

    protected abstract int accent();

    protected @Nullable Icon headerIcon() {
        return null;
    }

    protected boolean working() {
        return false;
    }

    protected @Nullable Icon slotHint(Slot slot) {
        return null;
    }

    protected abstract void extractPanelContents(Canvas c, int mouseX, int mouseY);

    protected void extractForeground(Canvas c, int mouseX, int mouseY) {
    }

    protected float age() {
        return (Util.getMillis() - openedAt) / 1000f;
    }

    @Override
    protected void init() {
        super.init();
        if (openedAt == 0) {
            openedAt = Util.getMillis();
        }
        inventoryTop = -1;
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory) {
                inventoryTop = inventoryTop < 0 ? slot.y : Math.min(inventoryTop, slot.y);
            }
        }
        titleLabelX = headerIcon() != null ? 20 : 8;
        titleLabelY = 4;
        if (inventoryTop >= 0) {
            inventoryLabelY = inventoryTop - 10;
        }
    }

    private void advanceClock() {
        long now = Util.getMillis();
        dt = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        time = Anim.now();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        advanceClock();
        Canvas c = new Canvas(g);
        drawBackdrop(c);
        drawChassis(c);
        drawSlotWells(c, mouseX, mouseY);
        fx.update(dt);
        extractPanelContents(c, mouseX, mouseY);
        c.flush();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractContents(g, mouseX, mouseY, partialTick);
        Canvas c = new Canvas(g);
        extractForeground(c, mouseX, mouseY);
        drawOpening(c);
        c.flush();
    }

    private void drawBackdrop(Canvas c) {
        int accent = accent();
        backdrop.update(dt);
        backdrop.emit(9f, dt, () -> {
            var r = backdrop.random();
            backdrop.spawn(UiParticles.Kind.MOTE, r.nextFloat() * width, height + 4, (r.nextFloat() - 0.5f) * 6f, -6f - r.nextFloat() * 10f,
                    6f + r.nextFloat() * 6f, 1.5f + r.nextFloat() * 2.5f, Theme.fade(accent, 0.18f + r.nextFloat() * 0.2f));
        });
        backdrop.draw(c);
        Chassis.backdrop(c, leftPos, topPos, imageWidth, imageHeight, accent, time);
    }

    private void drawChassis(Canvas c) {
        float x = leftPos;
        float y = topPos;
        float w = imageWidth;
        float h = imageHeight;
        int accent = accent();
        Chassis.panel(c, x, y, w, h, accent, headerIcon(), working(), time, age());
        if (inventoryTop >= 0) {
            float iy = y + inventoryTop - 12;
            c.rect(x + 4, iy, w - 8, h - (iy - y) - 4, 0x40000000);
            c.rectH(x + 4, iy, w - 8, 0.6f, Theme.fade(accent, 0.45f), Theme.fade(accent, 0.05f));
        }
    }

    private void drawSlotWells(Canvas c, int mouseX, int mouseY) {
        int accent = accent();
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            float sx = leftPos + slot.x - 1;
            float sy = topPos + slot.y - 1;
            boolean machine = !(slot.container instanceof Inventory);
            boolean hovered = mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18;
            c.rect(sx, sy, 18, 18, Theme.WELL);
            c.rectV(sx, sy, 18, 5, 0x70000000, 0x00000000);
            c.frame(sx, sy, 18, 18, 0.7f, machine ? Theme.fade(accent, 0.55f) : Theme.WELL_EDGE);
            if (machine) {
                Instruments.brackets(c, sx - 1, sy - 1, 20, 20, 3, 0.8f, Theme.fade(accent, 0.8f));
                if (!slot.hasItem()) {
                    Icon hint = slotHint(slot);
                    if (hint != null) {
                        Icons.draw(c, hint, sx + 9, sy + 9, 10, Theme.fade(accent, 0.22f + 0.08f * Anim.pulse(time, 0.5f)));
                    }
                }
                if (slot instanceof MachineMenu.OutputSlot && slot.hasItem()) {
                    float p = Anim.pulse(time, 1.2f);
                    c.haloRect(sx, sy, 18, 18, 4 + 2 * p, Theme.fade(Theme.GOOD, 0.25f + 0.25f * p));
                }
            }
            if (hovered) {
                c.haloRect(sx, sy, 18, 18, 4, Theme.fade(accent, 0.35f));
            }
        }
    }

    private void drawOpening(Canvas c) {
        Chassis.opening(c, leftPos, topPos, imageWidth, imageHeight, accent(), age());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Canvas c = new Canvas(g);
        c.glowText(font, title, titleLabelX, titleLabelY + 1, Theme.TEXT, accent());
        if (inventoryTop >= 0) {
            Instruments.section(c, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + 2, imageWidth - 16, accent());
        }
        c.flush();
    }

    protected boolean hovering(int x, int y, int w, int h, int mouseX, int mouseY) {
        return isHovering(x, y, w, h, mouseX, mouseY);
    }

    protected void tooltip(GuiGraphicsExtractor g, List<Component> lines, int mouseX, int mouseY) {
        g.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    protected void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }
}
