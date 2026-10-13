package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.menu.CompostBinMenu;
import com.cultivar.util.Texts;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

public class CompostBinScreen extends CultivarContainerScreen<CompostBinMenu> {
    private static final int BIN_X = 46;
    private static final int BIN_Y = 24;
    private static final int BIN_W = 104;
    private static final int BIN_H = 72;
    private static final int[] STRATA = {0xFF3A2818, 0xFF4E3421, 0xFF5E4029, 0xFF45301E, 0xFF55402A};
    private final Anim.Smooth fill = new Anim.Smooth(3f);
    private final Anim.Smooth cure = new Anim.Smooth(6f);
    private int lastCharge = -1;

    public CompostBinScreen(CompostBinMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CompostBinMenu.WIDTH, CompostBinMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_COMPOST;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.GRID;
    }

    @Override
    protected boolean working() {
        return menu.progress() > 0f;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return slot.getContainerSlot() < CompostBinBlockEntity.SLOT_OUTPUT ? Icon.SEED : null;
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        int charge = menu.charge();
        float level = fill.update(charge / (float) CompostBinBlockEntity.FULL_CHARGE, dt);
        float p = cure.update(menu.progress(), dt);
        boolean curing = menu.progress() > 0f;
        float x = leftPos + BIN_X;
        float y = topPos + BIN_Y;
        float w = BIN_W;
        float h = BIN_H;
        float inner = h - 4;
        float surface = y + 2 + inner * (1f - level);

        if (lastCharge >= 0 && charge > lastCharge) {
            for (int i = 0; i < 26; i++) {
                var r = fx.random();
                fx.spawn(UiParticles.Kind.SPARK, x + 10 + r.nextFloat() * (w - 20), y - 4 - r.nextFloat() * 6, (r.nextFloat() - 0.5f) * 20f,
                        10f + r.nextFloat() * 20f, 0.5f + r.nextFloat() * 0.5f, 1f, STRATA[r.nextInt(STRATA.length)] | 0xFF000000);
            }
        }
        lastCharge = charge;

        c.round(x - 3, y - 3, w + 6, h + 6, 3, 0xFF2A1E14);
        c.rect(x, y, w, h, 0xFF0B0F10);
        c.rectV(x, y, w, 8, 0x80000000, 0x00000000);
        if (level > 0.003f) {
            c.scissor((int) x, (int) Math.floor(surface), (int) (x + w), (int) (y + h));
            float bottom = y + h - 2;
            int band = 0;
            for (float by = bottom; by > surface - 6; by -= 6) {
                c.rect(x + 1, by - 6, w - 2, 6, STRATA[band % STRATA.length]);
                band++;
            }
            c.rectV(x + 1, surface, w - 2, 5, 0xFF56662E, 0x0056662E);
            for (int gx = 0; gx < (int) w - 2; gx += 2) {
                for (int gy = 0; gy < (int) inner; gy += 2) {
                    float py = bottom - gy;
                    if (py < surface) {
                        break;
                    }
                    float n = Anim.hash(gx * 131 + gy * 7919);
                    if (n < 0.18f) {
                        c.rect(x + 1 + gx, py - 2, 1.2f, 1.2f, 0x50000000);
                    } else if (n > 0.9f) {
                        c.rect(x + 1 + gx, py - 2, 1, 1, 0x26FFE6B0);
                    }
                }
            }
            if (curing || p > 0.01f) {
                float cx = x + w / 2f;
                float cy = Math.max(surface + 10, y + h * 0.62f);
                float k = 0.25f + 0.75f * p;
                c.bloom(cx, cy, 34 + 6 * Anim.pulse(time, 0.4f), Theme.withAlpha(0xFF7A2A, 0.45f * k));
                c.bloom(cx, cy, 16, Theme.withAlpha(0xFFC060, 0.5f * k));
            }
            c.endScissor();
            c.line(x + 1, surface, x + w - 1, surface, 0.8f, 0xC07C8E3A);
        }
        if (curing && level > 0.05f) {
            fx.emit(9f, dt, () -> {
                var r = fx.random();
                fx.spawn(UiParticles.Kind.STEAM, x + 8 + r.nextFloat() * (w - 16), surface - 1, 0, -5f - r.nextFloat() * 6f, 2.2f + r.nextFloat(),
                        2.5f + r.nextFloat() * 2.5f, Theme.withAlpha(0xFFF4E8, 0.9f));
            });
        }
        c.scissor(leftPos + 1, topPos + HEADER, leftPos + imageWidth - 1, topPos + imageHeight - 1);
        fx.draw(c);
        c.endScissor();
        for (int i = 1; i < 6; i++) {
            float sx = x + w * i / 6f;
            c.rect(sx - 0.5f, y, 1, h, 0x50281C12);
        }
        c.frame(x - 1, y - 1, w + 2, h + 2, 1, Theme.fade(Theme.ACCENT_COMPOST, 0.6f));
        Instruments.brackets(c, x - 4, y - 4, w + 8, h + 8, 5, 1f, Theme.ACCENT_COMPOST);

        gauge(c, leftPos + 155, y, 5, h, level, Theme.ACCENT_COMPOST, Texts.tr(Texts.UI_GAUGE_CHARGE));
        gauge(c, leftPos + 168, y, 5, h, p, Theme.GOOD, Texts.tr(Texts.UI_GAUGE_CURE));

        float ty = y + h + 4;
        c.text(font, Texts.tr(Texts.LABEL_CHARGE, charge), x, ty, Instruments.MICRO, Theme.TEXT_DIM);
        if (curing) {
            float seconds = (1f - menu.progress()) * CultivarConfig.COMPOST_TICKS.get() / 20f;
            c.text(font, Texts.tr(Texts.UI_COMPOST_NEXT, String.format(Locale.ROOT, "%.0f", seconds)), x, ty + 6, Instruments.MICRO, Theme.GOOD);
        } else if (charge < CompostBinBlockEntity.FULL_CHARGE) {
            c.text(font, Texts.tr(Texts.UI_COMPOST_NEEDS, CompostBinBlockEntity.FULL_CHARGE - charge), x, ty + 6, Instruments.MICRO,
                    Theme.TEXT_MUTED);
        } else {
            c.text(font, Texts.tr(Texts.UI_COMPOST_BLOCKED), x, ty + 6, Instruments.MICRO, Theme.WARN);
        }
        float flow = time * (curing ? 14f : 3f);
        c.dashed(leftPos + 35, topPos + 61, x - 4, topPos + 61, 1f, 3, 2, -flow, Theme.fade(Theme.ACCENT_COMPOST, 0.6f));
        c.dashed(leftPos + 173, topPos + 61, leftPos + 177, topPos + 61, 1f, 2, 1, -flow, Theme.fade(Theme.GOOD, 0.6f));
        c.text(font, Texts.tr(Texts.UI_INPUT), leftPos + 15, topPos + 20, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_OUTPUT), leftPos + 177, topPos + 42, Instruments.MICRO, Theme.TEXT_DIM);
    }

    private void gauge(Canvas c, float x, float y, float w, float h, float value, int color, Component label) {
        Instruments.well(c, x - 1, y - 1, w + 2, h + 2);
        float fh = h * Anim.clamp01(value);
        if (fh > 0.3f) {
            c.rectH(x, y + h - fh, w, fh, Theme.shade(color, 1.1f), Theme.shade(color, 0.6f));
            c.bloom(x + w / 2f, y + h - fh, 5, Theme.fade(color, 0.7f));
        }
        for (int i = 1; i < 10; i++) {
            c.rect(x, y + h * i / 10f, i % 5 == 0 ? w : w / 2f, 0.5f, 0x60000000);
        }
        c.centered(font, label, x + w / 2f, y + h + 3, 0.42f, Theme.TEXT_MUTED);
    }
}
