package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.History;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.menu.ClimateControllerMenu;
import com.cultivar.util.Texts;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public class ClimateControllerScreen extends CultivarContainerScreen<ClimateControllerMenu> {
    private static final int W = 268;
    private static final int H = 184;
    private static final float T_MIN = -0.5f;
    private static final float T_MAX = 2.0f;
    private static final float DIAL_START = (float) (Math.PI * 0.75);
    private static final float DIAL_SWEEP = (float) (Math.PI * 1.5);
    private static final Map<BlockPos, History[]> HISTORIES = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, History[]> eldest) {
            return size() > 8;
        }
    };
    private static final Map<BlockPos, Long> LAST_SAMPLE = new LinkedHashMap<>();

    public static void clearHistory() {
        HISTORIES.clear();
        LAST_SAMPLE.clear();
    }

    private final Anim.Smooth temp = new Anim.Smooth(5f);
    private final Anim.Smooth setpoint = new Anim.Smooth(9f);
    private final Anim.Smooth humidity = new Anim.Smooth(4f);
    private final Anim.Smooth sealed = new Anim.Smooth(5f);
    private final Anim.Smooth light = new Anim.Smooth(6f);
    private final Anim.Smooth insulation = new Anim.Smooth(5f);
    private float pingAge = 10f;

    public ClimateControllerScreen(ClimateControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, W, H);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_CLIMATE;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.THERMO;
    }

    @Override
    protected boolean working() {
        return menu.sealed();
    }

    @Override
    protected void init() {
        super.init();
        int by = topPos + H - 22;
        addRenderableWidget(new LabButton(leftPos + 10, by, 18, 16, Component.empty(), Icon.MINUS, Theme.INFO,
                input -> adjust(ClimateControllerMenu.BUTTON_COLDER, input.hasShiftDown())));
        addRenderableWidget(new LabButton(leftPos + 82, by, 18, 16, Component.empty(), Icon.PLUS, Theme.WARN,
                input -> adjust(ClimateControllerMenu.BUTTON_WARMER, input.hasShiftDown())));
        addRenderableWidget(new LabButton(leftPos + W - 78, by, 68, 16, Texts.tr(Texts.LABEL_RECOMPUTE), Icon.REFRESH, Theme.ACCENT_CLIMATE,
                input -> {
                    clickButton(ClimateControllerMenu.BUTTON_RESCAN);
                    pingAge = 0f;
                }));
    }

    private void adjust(int button, boolean coarse) {
        int steps = coarse ? 5 : 1;
        for (int i = 0; i < steps; i++) {
            clickButton(button);
        }
    }

    private History[] histories() {
        return HISTORIES.computeIfAbsent(menu.pos(), p -> new History[] {new History(160), new History(160)});
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        float rawTemp = menu.data(ClimateControllerBlockEntity.D_TEMP) / 100f;
        float rawSet = menu.data(ClimateControllerBlockEntity.D_SETPOINT) / 100f;
        float rawHum = menu.data(ClimateControllerBlockEntity.D_HUMIDITY) / 100f;
        int lights = menu.data(ClimateControllerBlockEntity.D_GROW_LIGHTS);
        int humidifiers = menu.data(ClimateControllerBlockEntity.D_HUMIDIFIERS);
        int dehumidifiers = menu.data(ClimateControllerBlockEntity.D_DEHUMIDIFIERS);
        boolean isSealed = menu.sealed();
        float t = temp.update(rawTemp, dt);
        float sp = setpoint.update(rawSet, dt);
        float hum = humidity.update(rawHum, dt);
        float seal = sealed.update(isSealed ? 1f : 0f, dt);
        float lt = light.update(menu.data(ClimateControllerBlockEntity.D_LIGHT), dt);
        float ins = insulation.update(menu.data(ClimateControllerBlockEntity.D_INSULATION) / 100f, dt);
        pingAge += dt;

        History[] hist = histories();
        long now = Util.getMillis();
        Long last = LAST_SAMPLE.get(menu.pos());
        if (last == null || now - last >= 375) {
            LAST_SAMPLE.put(menu.pos(), now);
            hist[0].push(rawTemp);
            hist[1].push(rawHum);
        }

        drawDial(c, leftPos + 50, topPos + 60, 27, t, sp, seal);
        float humTarget = Anim.clamp01(0.5f + 0.25f * humidifiers - 0.25f * dehumidifiers);
        drawTank(c, leftPos + 96, topPos + 24, 30, 70, hum, humTarget, seal, mouseX, mouseY);
        drawRadar(c, leftPos + 168, topPos + 56, 27, seal, lights, humidifiers, dehumidifiers);
        drawSide(c, leftPos + 210, topPos + 22, lt, ins, lights, humidifiers, dehumidifiers);

        float gy = topPos + 104;
        float gw = 122;
        float gh = 32;
        graphFrame(c, leftPos + 8, gy, gw, gh, Texts.tr(Texts.UI_HISTORY_TEMP));
        float spY = gy + gh - (sp - T_MIN) / (T_MAX - T_MIN) * gh;
        c.dashed(leftPos + 8, spY, leftPos + 8 + gw, spY, 0.7f, 3, 2, time * 6f, Theme.fade(Theme.WARN, 0.7f));
        hist[0].draw(c, leftPos + 8, gy, gw, gh, T_MIN, T_MAX, tempColor(t));
        graphFrame(c, leftPos + 138, gy, gw, gh, Texts.tr(Texts.UI_HISTORY_HUMIDITY));
        float htY = gy + gh - humTarget * gh;
        c.dashed(leftPos + 138, htY, leftPos + 138 + gw, htY, 0.7f, 3, 2, time * 6f, Theme.fade(Theme.INFO, 0.6f));
        hist[1].draw(c, leftPos + 138, gy, gw, gh, 0f, 1f, Theme.INFO);

        float by = topPos + H - 22;
        Instruments.well(c, leftPos + 31, by, 48, 16);
        c.centered(font, Component.literal(String.format(Locale.ROOT, "%.2f", rawSet)), leftPos + 55, by + 2.5f, Theme.WARN);
        c.centered(font, Texts.tr(Texts.UI_SETPOINT), leftPos + 55, by + 11, 0.45f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.UI_SHIFT_COARSE), leftPos + 104, by + 6, 0.5f, Theme.TEXT_MUTED);
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 8 + gw && mouseY >= gy && mouseY < gy + gh && hist[0].size() > 0) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_HISTORY_TEMP), Texts.tr(Texts.UI_HISTORY_NOTE)), mouseX, mouseY);
        }
    }

    private static int tempColor(float t) {
        float k = Anim.clamp01((t - T_MIN) / (T_MAX - T_MIN));
        if (k < 0.4f) {
            return Theme.lerp(0xFF5AA8FF, 0xFF5CE0B0, k / 0.4f);
        }
        if (k < 0.7f) {
            return Theme.lerp(0xFF5CE0B0, 0xFFF2C04A, (k - 0.4f) / 0.3f);
        }
        return Theme.lerp(0xFFF2C04A, 0xFFF2585A, (k - 0.7f) / 0.3f);
    }

    private static float dialAngle(float value) {
        return DIAL_START + DIAL_SWEEP * Anim.clamp01((value - T_MIN) / (T_MAX - T_MIN));
    }

    private void drawDial(Canvas c, float cx, float cy, float r, float t, float sp, float seal) {
        c.disc(cx, cy, r + 14, 0xFF0C171C, 0xFF09120F);
        c.ring(cx, cy, r + 14, 0.8f, Theme.fade(Theme.ACCENT_CLIMATE, 0.5f));
        int seg = 40;
        for (int i = 0; i < seg; i++) {
            float v0 = T_MIN + (T_MAX - T_MIN) * i / seg;
            float v1 = T_MIN + (T_MAX - T_MIN) * (i + 1) / seg;
            int col = Theme.fade(tempColor((v0 + v1) / 2f), 0.85f);
            c.arc(cx, cy, r - 4, r, dialAngle(v0), dialAngle(v1) + 0.01f, Theme.shade(col, 0.6f), col);
        }
        for (int i = 0; i <= 10; i++) {
            float v = T_MIN + 0.25f * i;
            float a = dialAngle(v);
            boolean major = i % 2 == 0;
            float r0 = r - 6 - (major ? 3 : 1.5f);
            c.line(cx + Canvas.cos(a) * (r - 5), cy + Canvas.sin(a) * (r - 5), cx + Canvas.cos(a) * r0, cy + Canvas.sin(a) * r0, major ? 1f : 0.6f,
                    0xB0A8D0D8);
            if (major) {
                String label = String.format(Locale.ROOT, v == Math.floor(v) ? "%.0f" : "%.1f", v);
                c.centered(font, Component.literal(label), cx + Canvas.cos(a) * (r + 9), cy + Canvas.sin(a) * (r + 9) - 1.5f, 0.45f,
                        Theme.TEXT_DIM);
            }
        }
        float at = dialAngle(t);
        float as = dialAngle(sp);
        if (Math.abs(as - at) > 0.01f) {
            c.glow(true);
            c.arc(cx, cy, r + 1.5f, r + 3.5f, Math.min(at, as), Math.max(at, as), Theme.withAlpha(0xF2B84A, 0.15f), Theme.withAlpha(0xF2B84A, 0.45f));
            c.glow(false);
        }
        float mx = cx + Canvas.cos(as) * (r + 3);
        float my = cy + Canvas.sin(as) * (r + 3);
        float ox = Canvas.cos(as);
        float oy = Canvas.sin(as);
        c.tri(mx, my, Theme.WARN, mx + ox * 5 - oy * 3, my + oy * 5 + ox * 3, Theme.WARN, mx + ox * 5 + oy * 3, my + oy * 5 - ox * 3, Theme.WARN);
        int needle = Theme.lerp(0xFF6A7A80, 0xFFFFFFFF, 0.4f + 0.6f * seal);
        float nx = cx + Canvas.cos(at) * (r - 3);
        float ny = cy + Canvas.sin(at) * (r - 3);
        float px = -Canvas.sin(at) * 1.8f;
        float py = Canvas.cos(at) * 1.8f;
        c.bloom(nx, ny, 5, Theme.fade(tempColor(t), 0.7f * (0.4f + 0.6f * seal)));
        c.quad(cx + px, cy + py, needle, nx, ny, needle, nx, ny, needle, cx - px, cy - py, Theme.shade(needle, 0.7f));
        c.disc(cx, cy, 3.2f, 0xFF2A3E46, 0xFF15232A);
        c.ring(cx, cy, 3.2f, 0.7f, Theme.ACCENT_CLIMATE);
        c.centered(font, Component.literal(String.format(Locale.ROOT, "%.2f", t)), cx, cy + 8, tempColor(t));
        c.centered(font, Texts.tr(Texts.UI_TEMPERATURE), cx, cy + 17, 0.45f, Theme.TEXT_MUTED);
    }

    private void drawTank(Canvas c, float x, float y, float w, float h, float hum, float target, float seal, int mouseX, int mouseY) {
        c.round(x - 2, y - 2, w + 4, h + 4, 4, 0xFF0A1418);
        c.round(x, y, w, h, 3, 0xFF0D1B21);
        float level = y + h - 2 - (h - 4) * Anim.clamp01(hum);
        int cols = 12;
        for (int i = 0; i < cols; i++) {
            float xa = x + 1 + (w - 2) * i / cols;
            float xb = x + 1 + (w - 2) * (i + 1) / cols;
            float ya = level + Canvas.sin(time * 2.4f + i * 0.9f) * 1.3f + Canvas.sin(time * 3.7f + i * 0.4f) * 0.6f;
            float yb = level + Canvas.sin(time * 2.4f + (i + 1) * 0.9f) * 1.3f + Canvas.sin(time * 3.7f + (i + 1) * 0.4f) * 0.6f;
            c.quad(xa, ya, 0xD05AB8F2, xa, y + h - 1, 0xF0123A5A, xb, y + h - 1, 0xF0123A5A, xb, yb, 0xD05AB8F2);
            c.line(xa, ya, xb, yb, 0.7f, 0xC0D8F0FF);
        }
        fx.emit(3f + 4f * seal, dt, () -> {
            var r = fx.random();
            fx.spawn(UiParticles.Kind.BUBBLE, x + 4 + r.nextFloat() * (w - 8), y + h - 3, 0, -4f, 1.2f + r.nextFloat(), 0.6f + r.nextFloat(),
                    0xC0D8F0FF);
        });
        c.scissor((int) x + 1, (int) Math.max(y + 1, level), (int) (x + w - 1), (int) (y + h - 1));
        fx.draw(c);
        c.endScissor();
        for (int i = 1; i < 10; i++) {
            float gy = y + h - 2 - (h - 4) * i / 10f;
            c.rect(x + w - (i % 5 == 0 ? 8 : 4), gy, i % 5 == 0 ? 8 : 4, 0.5f, 0x90D8F0FF);
        }
        float ty = y + h - 2 - (h - 4) * target;
        c.tri(x - 1, ty, Theme.INFO, x - 5, ty - 3, Theme.INFO, x - 5, ty + 3, Theme.INFO);
        c.rectH(x + 2, y + 2, 3, h - 4, 0x28FFFFFF, 0x00FFFFFF);
        c.frame(x, y, w, h, 0.8f, Theme.fade(Theme.INFO, 0.7f));
        c.centered(font, Component.literal(Math.round(hum * 100) + "%"), x + w / 2f, y + 4, Theme.TEXT);
        c.centered(font, Texts.tr(Texts.UI_HUMIDITY), x + w / 2f, y + h + 4, 0.45f, Theme.TEXT_MUTED);
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            tooltip(c.g(), List.of(Texts.tr(Texts.LABEL_MOISTURE, Math.round(hum * 100)), Texts.tr(Texts.UI_HUMIDITY_TARGET,
                    Math.round(target * 100))), mouseX, mouseY);
        }
    }

    private void drawRadar(Canvas c, float cx, float cy, float r, float seal, int lights, int humidifiers, int dehumidifiers) {
        int good = Theme.lerp(Theme.BAD, Theme.GOOD, seal);
        c.disc(cx, cy, r, Theme.fade(Theme.shade(good, 0.25f), 0.9f), 0xF0081210);
        for (int i = 1; i <= 3; i++) {
            c.ring(cx, cy, r * i / 3f, 0.5f, Theme.fade(good, 0.25f));
        }
        c.line(cx - r, cy, cx + r, cy, 0.5f, Theme.fade(good, 0.2f));
        c.line(cx, cy - r, cx, cy + r, 0.5f, Theme.fade(good, 0.2f));
        if (seal > 0.5f) {
            float a = time * 2.2f;
            c.glow(true);
            for (int i = 0; i < 14; i++) {
                float a0 = a - i * 0.06f;
                float a1 = a - (i + 1) * 0.06f;
                int col = Theme.fade(good, 0.5f * (1f - i / 14f));
                c.quad(cx, cy, col, cx + Canvas.cos(a0) * r, cy + Canvas.sin(a0) * r, col, cx + Canvas.cos(a1) * r, cy + Canvas.sin(a1) * r, col,
                        cx, cy, col);
            }
            c.glow(false);
            c.line(cx, cy, cx + Canvas.cos(a) * r, cy + Canvas.sin(a) * r, 0.9f, Theme.fade(good, 0.9f));
            int total = lights + humidifiers + dehumidifiers;
            for (int i = 0; i < Math.min(total, 24); i++) {
                int col = i < lights ? Theme.WARN : i < lights + humidifiers ? Theme.INFO : Theme.ACCENT_COMPOST;
                float ba = Anim.hash(i * 97 + 11) * (float) (Math.PI * 2);
                float br = r * (0.3f + 0.6f * Anim.hash(i * 31 + 7));
                float since = (float) ((a - ba) % (Math.PI * 2));
                if (since < 0) {
                    since += (float) (Math.PI * 2);
                }
                float lit = Anim.clamp01(1f - since / 3f);
                float bx = cx + Canvas.cos(ba) * br;
                float by = cy + Canvas.sin(ba) * br;
                c.bloom(bx, by, 3 + 2 * lit, Theme.fade(col, 0.3f + 0.6f * lit));
                c.rect(bx - 0.6f, by - 0.6f, 1.2f, 1.2f, col);
            }
        } else {
            float flash = Anim.pulse(time, 2f);
            for (int i = 0; i < 8; i++) {
                float a0 = (float) (Math.PI * 2 * i / 8) + 0.1f;
                float a1 = a0 + 0.55f + 0.1f * Anim.hash(i + (int) (time * 4));
                c.arc(cx, cy, r - 1.5f, r + 0.5f, a0, a1, Theme.fade(Theme.BAD, 0.5f + 0.5f * flash), Theme.fade(Theme.BAD, 0.5f + 0.5f * flash));
            }
            c.bloom(cx, cy, r * 0.8f, Theme.fade(Theme.BAD, 0.18f * flash));
        }
        if (pingAge < 1.2f) {
            float k = pingAge / 1.2f;
            c.glow(true);
            c.ring(cx, cy, r * Anim.easeOutCubic(k) * 1.15f, 1.5f, Theme.fade(Theme.ACCENT_CLIMATE, 1f - k));
            c.glow(false);
        }
        c.ring(cx, cy, r, 0.9f, Theme.fade(good, 0.8f));
        Component status = Texts.tr(seal > 0.5f ? Texts.UI_SEALED : Texts.UI_BREACH);
        c.centered(font, status, cx, cy + r + 4, seal > 0.5f ? Theme.GOOD : Theme.fade(Theme.BAD, 0.6f + 0.4f * Anim.pulse(time, 2f)));
        int max = CultivarConfig.CLIMATE_VOLUME_MAX.get();
        Component volume = menu.sealed() ? Texts.tr(Texts.UI_VOLUME, menu.volume(), max) : Texts.tr(Texts.UI_VOLUME_UNSEALED, max);
        c.centered(font, volume, cx, cy + r + 13, 0.5f, Theme.TEXT_DIM);
    }

    private void drawSide(Canvas c, float x, float y, float lightLevel, float ins, int lights, int humidifiers, int dehumidifiers) {
        Instruments.section(c, font, Texts.tr(Texts.UI_LIGHT), x, y, 50, Theme.WARN);
        for (int i = 0; i < 16; i++) {
            float sy = y + 66 - i * 4;
            float on = Anim.clamp01(lightLevel - i);
            c.rect(x, sy, 10, 3, 0xFF0D1A1E);
            if (on > 0) {
                int col = Theme.lerp(0xFF7A5A20, 0xFFFFE070, i / 15f);
                c.rect(x, sy, 10, 3, Theme.fade(col, 0.3f + 0.7f * on));
                c.bloom(x + 5, sy + 1.5f, 5, Theme.fade(col, 0.25f * on));
            }
        }
        c.text(font, Component.literal(String.valueOf(Math.round(lightLevel))), x + 13, y + 60, Theme.WARN);
        float gx = x + 33;
        float gy = y + 22;
        Instruments.ring(c, gx, gy, 10, 3, ins, Theme.ACCENT_CLIMATE, time);
        Icons.draw(c, Icon.SHIELD, gx, gy, 8, Theme.fade(Theme.ACCENT_CLIMATE, 0.8f));
        c.centered(font, Component.literal(Math.round(ins * 100) + "%"), gx, gy + 15, 0.55f, Theme.TEXT_DIM);
        c.centered(font, Texts.tr(Texts.UI_INSULATION), gx, gy + 20, 0.42f, Theme.TEXT_MUTED);
        float dy = y + 52;
        device(c, x + 22, dy, Icon.LIGHT, lights, Theme.WARN);
        device(c, x + 22, dy + 8, Icon.DROP, humidifiers, Theme.INFO);
        device(c, x + 22, dy + 16, Icon.DROP, dehumidifiers, Theme.ACCENT_COMPOST);
    }

    private void device(Canvas c, float x, float y, Icon icon, int count, int color) {
        Icons.draw(c, icon, x + 3, y + 3, 6, count > 0 ? color : Theme.TEXT_MUTED);
        c.text(font, Component.literal("x" + count), x + 8, y + 0.5f, 0.6f, count > 0 ? color : Theme.TEXT_MUTED);
    }

    private void graphFrame(Canvas c, float x, float y, float w, float h, Component label) {
        Instruments.well(c, x - 1, y - 1, w + 2, h + 2);
        for (int i = 1; i < 4; i++) {
            c.rect(x, y + h * i / 4f, w, 0.4f, 0x203FA8E0);
        }
        for (int i = 1; i < 8; i++) {
            c.rect(x + w * i / 8f, y, 0.4f, h, 0x183FA8E0);
        }
        c.text(font, label, x + 2, y + 2, 0.5f, Theme.TEXT_MUTED);
    }
}
