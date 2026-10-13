package com.cultivar.client.guide;

import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.guide.Hint;
import com.cultivar.util.Texts;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class HintToast implements Toast {
    private static final long DURATION_MS = 9000;
    private static final int WIDTH = 210;
    private static final int HEIGHT = 44;
    private final Hint hint;
    private Visibility visibility = Visibility.SHOW;

    public HintToast(Hint hint) {
        this.hint = hint;
    }

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        visibility = fullyVisibleForMs >= DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }

    @Override
    public Object getToken() {
        return hint;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, Font font, long fullyVisibleForMs) {
        ManualContent.Chapter chapter = ManualContent.chapter(hint.chapter());
        int accent = chapter.accent();
        float time = Anim.now();
        Canvas c = new Canvas(g);
        c.round(0, 0, WIDTH, HEIGHT, 3, 0xF20B151A);
        c.rectV(0, 0, WIDTH, 14, Theme.fade(Theme.shade(accent, 0.4f), 0.6f), 0x00000000);
        c.frame(0, 0, WIDTH, HEIGHT, 0.8f, Theme.fade(accent, 0.7f));
        Instruments.brackets(c, -1, -1, WIDTH + 2, HEIGHT + 2, 4, 1f, accent);
        c.rect(0, 0, 2, HEIGHT, accent);
        float pulse = Anim.pulse(time, 1.2f);
        c.bloom(15, 15, 11 + 2 * pulse, Theme.fade(accent, 0.35f));
        c.disc(15, 15, 9, Theme.shade(accent, 0.35f), Theme.shade(accent, 0.2f));
        c.ring(15, 15, 9, 0.8f, accent);
        Icons.draw(c, chapter.icon(), 15, 15, 10, Theme.TEXT);
        c.text(font, Component.translatable(hint.titleKey()), 30, 4, Theme.TEXT);
        List<FormattedCharSequence> lines = font.split(Component.translatable(hint.bodyKey()), (int) ((WIDTH - 36) / 0.7f));
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            c.flush();
            g.pose().pushMatrix();
            g.pose().translate(30, 15 + i * 7);
            g.pose().scale(0.7f, 0.7f);
            g.text(font, lines.get(i), 0, 0, Theme.TEXT_DIM, false);
            g.pose().popMatrix();
        }
        Component footer = GuideClient.keyName() != null ? Texts.tr(Texts.HINT_FOOTER, GuideClient.keyName()) : Texts.tr(Texts.HINT_FOOTER_UNBOUND);
        c.text(font, footer, 30, HEIGHT - 9, 0.6f, accent);
        float left = 1f - Math.min(1f, fullyVisibleForMs / (float) DURATION_MS);
        c.rect(2, HEIGHT - 1.5f, (WIDTH - 2) * left, 1.2f, Theme.fade(accent, 0.8f));
        c.flush();
    }
}
