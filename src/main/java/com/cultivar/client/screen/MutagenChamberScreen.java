package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.menu.MutagenChamberMenu;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MutagenChamberScreen extends CultivarContainerScreen<MutagenChamberMenu> {
    private static final int VESSEL_X = 96;
    private static final int VESSEL_Y = 22;
    private static final int VESSEL_W = 42;
    private static final int VESSEL_H = 74;
    private final Anim.Smooth level = new Anim.Smooth(4f);
    private final Anim.Smooth progress = new Anim.Smooth(10f);
    private final Anim.Smooth heat = new Anim.Smooth(2f).from(0f);

    public MutagenChamberScreen(MutagenChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MutagenChamberMenu.WIDTH, MutagenChamberMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_MUTAGEN;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.FLASK;
    }

    @Override
    protected boolean working() {
        return menu.progress() > 0f;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        int index = slot.getContainerSlot();
        if (index == MutagenChamberBlockEntity.SLOT_VIAL) {
            return Icon.FLASK;
        }
        return index < MutagenChamberBlockEntity.SLOT_VIAL ? Icon.SEED : null;
    }

    private ItemStack treating() {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = menu.getSlot(MutagenChamberBlockEntity.FIRST_INPUT + i).getItem();
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        boolean active = menu.progress() > 0f;
        float lvl = level.update(menu.vialUses() / (float) MutagenChamberBlockEntity.USES_PER_VIAL, dt);
        float p = progress.update(menu.progress(), dt);
        float h = heat.update(active ? 1f : 0f, dt);
        float vx = leftPos + VESSEL_X;
        float vy = topPos + VESSEL_Y;
        float vw = VESSEL_W;
        float vh = VESSEL_H;

        c.round(vx - 2, vy - 2, vw + 4, vh + 4, 6, 0xFF0A1216);
        c.round(vx, vy, vw, vh, 5, 0xFF0E1A20);
        float surface = vy + vh - 6 - (vh - 12) * lvl;
        int deep = Theme.shade(Theme.ACCENT_MUTAGEN, 0.35f);
        int bright = Theme.shade(Theme.ACCENT_MUTAGEN, 0.95f);
        if (lvl > 0.01f) {
            int cols = 14;
            for (int i = 0; i < cols; i++) {
                float xa = vx + 2 + (vw - 4) * i / cols;
                float xb = vx + 2 + (vw - 4) * (i + 1) / cols;
                float ya = surface + Canvas.sin(time * 3.1f + i * 0.7f) * (1.2f + h) ;
                float yb = surface + Canvas.sin(time * 3.1f + (i + 1) * 0.7f) * (1.2f + h);
                float bottom = vy + vh - 3;
                c.quad(xa, ya, Theme.fade(bright, 0.85f), xa, bottom, Theme.fade(deep, 0.95f), xb, bottom, Theme.fade(deep, 0.95f), xb, yb,
                        Theme.fade(bright, 0.85f));
                c.line(xa, ya, xb, yb, 0.8f, Theme.withAlpha(0xF0DCFF, 0.8f));
            }
            c.bloom(vx + vw / 2f, vy + vh - 8, 14 + 6 * h, Theme.fade(Theme.ACCENT_MUTAGEN, 0.25f + 0.35f * h * (0.7f + 0.3f * Anim.pulse(time, 2f))));
        }
        for (int i = 0; i < 5; i++) {
            float cx = vx + 6 + i * (vw - 12) / 4f;
            c.bloom(cx, vy + vh - 2, 3 + 2 * h, Theme.fade(0xFFFF7A3A, 0.15f + 0.6f * h * Anim.pulse(time + i * 0.2f, 1.7f)));
        }
        if (lvl > 0.01f) {
            fx.emit(active ? 22f : 2.5f, dt, () -> {
                var r = fx.random();
                fx.spawn(UiParticles.Kind.BUBBLE, vx + 5 + r.nextFloat() * (vw - 10), vy + vh - 5, 0, -6f - r.nextFloat() * 8f,
                        1.4f + r.nextFloat(), 0.8f + r.nextFloat() * 1.4f, Theme.withAlpha(0xE8D0FF, 0.9f));
            });
        }
        c.scissor((int) vx + 1, (int) Math.max(vy + 1, surface - 2), (int) (vx + vw - 1), (int) (vy + vh - 1));
        fx.draw(c);
        c.endScissor();
        c.flush();
        ItemStack seed = treating();
        if (!seed.isEmpty() && lvl > 0.01f) {
            float bob = Canvas.sin(time * 2.2f) * 2f;
            int ix = (int) (vx + vw / 2f - 8);
            int iy = (int) (Math.max(surface + 4, vy + vh / 2f - 4) + bob);
            if (active) {
                c.bloom(ix + 8, iy + 8, 12, Theme.fade(Theme.ACCENT_MUTAGEN, 0.35f + 0.25f * Anim.pulse(time, 4f)));
                c.flush();
            }
            c.g().item(seed, ix, iy);
        }
        c.rectH(vx + 3, vy + 3, 4, vh - 6, 0x30FFFFFF, 0x00FFFFFF);
        c.rect(vx + vw - 6, vy + 6, 1, vh - 12, 0x18FFFFFF);
        for (int i = 1; i < 8; i++) {
            float gy = vy + vh - 6 - (vh - 12) * i / 8f;
            c.rect(vx + vw - 9, gy, i % 2 == 0 ? 6 : 3, 0.6f, 0x70D8C8F0);
        }
        c.frame(vx, vy, vw, vh, 0.8f, Theme.fade(Theme.ACCENT_MUTAGEN, 0.7f));
        Instruments.brackets(c, vx - 3, vy - 3, vw + 6, vh + 6, 5, 1f, Theme.ACCENT_MUTAGEN);
        c.stripes(vx - 2, vy + vh + 4, vw + 4, 5, 8, active ? time * 18f : 0f, Theme.fade(Theme.WARN, active ? 0.9f : 0.4f), 0xFF14181A);
        c.text(font, Texts.tr(Texts.LABEL_VIAL, menu.vialUses()), vx - 2, vy + vh + 12, Instruments.MICRO, Theme.TEXT_DIM);

        float rx = leftPos + 160;
        float ry = topPos + 46;
        Instruments.ring(c, rx, ry, 11, 3, p, Theme.ACCENT_MUTAGEN, time);
        c.centered(font, Component.literal(Math.round(p * 100) + "%"), rx, ry - 3, Instruments.MICRO, Theme.TEXT);
        int seconds = Math.round((1f - p) * MutagenChamberBlockEntity.TICKS_PER_SEED / 20f);
        c.centered(font, Component.literal(active ? seconds + "s" : "--"), rx, ry + 2, 0.5f, Theme.TEXT_MUTED);

        float flow = time * (active ? 20f : 3f);
        int flowColor = Theme.fade(Theme.ACCENT_MUTAGEN, active ? 0.85f : 0.3f);
        c.dashed(leftPos + 52, topPos + 45, vx - 3, topPos + 45, 1f, 3, 2, -flow, flowColor);
        c.dashed(leftPos + 43, topPos + 92, vx - 3, topPos + 92, 1f, 3, 2, -flow, flowColor);
        c.dashed(vx + vw + 3, topPos + 45, rx - 15, topPos + 45, 1f, 3, 2, -flow, flowColor);
        c.dashed(rx + 15, topPos + 45, leftPos + 181, topPos + 45, 1f, 3, 2, -flow, flowColor);

        double destroy = Math.min(1.0, CultivarConfig.MUTAGEN_DESTROY_CHANCE.get());
        double mutate = Math.min(1.0 - destroy, CultivarConfig.MUTAGEN_SUCCESS_CHANCE.get());
        double unchanged = Math.max(0.0, 1.0 - destroy - mutate);
        float ox = leftPos + 150;
        float oy = topPos + 80;
        float ow = 72;
        Instruments.section(c, font, Texts.tr(Texts.UI_OUTCOMES), ox, oy, ow, Theme.ACCENT_MUTAGEN);
        float bx = ox;
        float barY = oy + 7;
        float mw = (float) (ow * mutate);
        float dw = (float) (ow * destroy);
        float uw = ow - mw - dw;
        Instruments.well(c, ox - 1, barY - 1, ow + 2, 6);
        c.rectV(bx, barY, mw, 4, Theme.MUTANT, Theme.shade(Theme.MUTANT, 0.6f));
        c.rectV(bx + mw, barY, uw, 4, 0xFF6A8A90, 0xFF3F5559);
        c.rectV(bx + mw + uw, barY, dw, 4, Theme.BAD, Theme.shade(Theme.BAD, 0.6f));
        legend(c, ox, barY + 7, Theme.MUTANT, Texts.tr(Texts.UI_MUTATE, pct(mutate)));
        legend(c, ox, barY + 13, 0xFF6A8A90, Texts.tr(Texts.UI_UNCHANGED, pct(unchanged)));
        legend(c, ox, barY + 19, Theme.BAD, Texts.tr(Texts.UI_DESTROYED, pct(destroy)));
        if (mouseX >= ox && mouseX < ox + ow && mouseY >= barY - 1 && mouseY < barY + 25) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_OUTCOMES), Texts.tr(Texts.UI_OUTCOME_NOTE)), mouseX, mouseY);
        }
        c.text(font, Texts.tr(Texts.UI_INPUT), leftPos + 15, topPos + 19, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_OUTPUT), leftPos + 181, topPos + 19, Instruments.MICRO, Theme.TEXT_DIM);
    }

    private void legend(Canvas c, float x, float y, int color, Component text) {
        c.rect(x, y + 0.5f, 3, 3, color);
        c.text(font, text, x + 5, y, 0.55f, Theme.TEXT_DIM);
    }

    private static String pct(double v) {
        return String.format(Locale.ROOT, "%d%%", Math.round(v * 100));
    }
}
