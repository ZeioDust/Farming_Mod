package com.cultivar.client.ui;

import com.cultivar.client.ui.Icons.Icon;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class LabButton extends AbstractButton {
    private final int accent;
    private final @Nullable Icon icon;
    private final Consumer<InputWithModifiers> action;
    private final Anim.Smooth hover = new Anim.Smooth(14f).from(0f);
    private float flash;
    private long lastFrame;
    private boolean danger;

    public LabButton(int x, int y, int width, int height, Component label, @Nullable Icon icon, int accent,
                     Consumer<InputWithModifiers> action) {
        super(x, y, width, height, label);
        this.accent = accent;
        this.icon = icon;
        this.action = action;
    }

    public LabButton danger(boolean danger) {
        this.danger = danger;
        return this;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        flash = 1f;
        action.accept(input);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = net.minecraft.util.Util.getMillis();
        float dt = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        float h = hover.update(isHoveredOrFocused() && active ? 1f : 0f, dt);
        flash = Math.max(0f, flash - dt * 3.5f);
        int tint = danger ? Theme.BAD : accent;
        Canvas c = new Canvas(g);
        float x = getX();
        float y = getY();
        float w = getWidth();
        float hh = getHeight();
        float time = Anim.now();
        if (!active) {
            c.round(x, y, w, hh, 2, 0xFF0C1519);
            c.frame(x, y, w, hh, 1, 0xFF1A2B30);
        } else {
            c.haloRect(x, y, w, hh, 3 + 3 * h, Theme.fade(tint, 0.10f + 0.30f * h + 0.5f * flash));
            c.round(x, y, w, hh, 2, Theme.lerp(0xFF0F1D22, Theme.shade(tint, 0.28f), 0.4f * h + flash));
            c.rectV(x + 1, y + 1, w - 2, hh * 0.45f, 0x22FFFFFF, 0x00FFFFFF);
            c.frame(x, y, w, hh, 1, Theme.lerp(Theme.fade(tint, 0.45f), tint, h));
            if (h > 0.02f) {
                float sweep = (time * 1.4f) % 1.6f - 0.3f;
                float sx = x + sweep * w;
                c.glow(true);
                c.quad(sx - 6, y + 1, 0x00FFFFFF, sx - 2, y + hh - 1, Theme.withAlpha(0xFFFFFF, 0.16f * h),
                        sx + 2, y + hh - 1, Theme.withAlpha(0xFFFFFF, 0.16f * h), sx + 6, y + 1, 0x00FFFFFF);
                c.glow(false);
            }
        }
        int fg = active ? Theme.lerp(Theme.shade(tint, 0.95f), 0xFFFFFFFF, h * 0.6f + flash) : 0xFF3A5055;
        var font = Minecraft.getInstance().font;
        Component label = getMessage();
        boolean hasLabel = !label.getString().isEmpty();
        float iconSize = Math.min(hh - 6, 9);
        if (icon != null && !hasLabel) {
            c.scissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            Icons.draw(c, icon, x + w / 2f, y + hh / 2f, iconSize, fg);
            c.endScissor();
        } else {
            int textW = font.width(label);
            float total = textW + (icon != null ? iconSize + 3 : 0);
            float start = x + (w - total) / 2f;
            if (icon != null) {
                Icons.draw(c, icon, start + iconSize / 2f, y + hh / 2f, iconSize, fg);
                start += iconSize + 3;
            }
            c.text(font, label, start, y + (hh - 8) / 2f + 1, fg);
        }
        c.flush();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
