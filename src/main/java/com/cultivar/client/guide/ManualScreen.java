package com.cultivar.client.guide;

import com.cultivar.client.guide.ManualContent.Chapter;
import com.cultivar.client.guide.ManualContent.Element;
import com.cultivar.client.guide.ManualContent.Goal;
import com.cultivar.client.guide.ManualContent.Page;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Chassis;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.client.ui.UiParticles;
import com.cultivar.data.Catalog;
import com.cultivar.guide.Hint;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.ItemLike;

public class ManualScreen extends Screen {
    private static final int W = 372;
    private static final int H = 226;
    private static final int RAIL = 106;
    private static final int ROW = 19;
    private static final int RAIL_FOOT = 34;
    private static final float TEXT_SCALE = 0.78f;
    private static final int LINE = 8;

    private Hint.Chapter chapterId;
    private int page;
    private long pageSince = Util.getMillis();
    private int direction = 1;
    private final long openedAt = Util.getMillis();
    private long lastFrame;
    private float dt;
    private float time;
    private int left;
    private int top;
    private LabButton prev;
    private LabButton next;
    private LabButton hints;
    private final UiParticles backdrop = new UiParticles();

    public ManualScreen(Hint.Chapter chapter, int page) {
        super(Texts.tr(Texts.MANUAL_TITLE));
        this.chapterId = chapter;
        this.page = page;
    }

    private Chapter chapter() {
        return ManualContent.chapter(chapterId);
    }

    private Catalog catalog() {
        return minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults();
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        page = Math.max(0, Math.min(page, chapter().pages().size() - 1));
        int px = left + RAIL + 8;
        int pw = W - RAIL - 16;
        prev = addRenderableWidget(new LabButton(px, top + H - 20, 18, 14, Component.empty(), Icon.LEFT, Theme.ACCENT_CARD, in -> turn(-1)));
        next = addRenderableWidget(new LabButton(px + pw - 18, top + H - 20, 18, 14, Component.empty(), Icon.RIGHT, Theme.ACCENT_CARD,
                in -> turn(1)));
        hints = addRenderableWidget(new LabButton(left + 8, top + H - 20, RAIL - 16, 14, hintLabel(), null, Theme.ACCENT_LAB, in -> {
            GuideClient.setHintsOff(!GuideClient.hintsOff());
            hints.setMessage(hintLabel());
        }));
        refreshButtons();
    }

    private Component hintLabel() {
        return Texts.tr(GuideClient.hintsOff() ? Texts.MANUAL_HINTS_OFF : Texts.MANUAL_HINTS_ON);
    }

    private void refreshButtons() {
        int pages = chapter().pages().size();
        prev.active = page > 0 || chapterId.ordinal() > 0;
        next.active = page < pages - 1 || chapterId.ordinal() < Hint.Chapter.values().length - 1;
    }

    private void turn(int delta) {
        int pages = chapter().pages().size();
        int target = page + delta;
        Hint.Chapter[] all = Hint.Chapter.values();
        if (target < 0) {
            if (chapterId.ordinal() == 0) {
                return;
            }
            chapterId = all[chapterId.ordinal() - 1];
            target = chapter().pages().size() - 1;
        } else if (target >= pages) {
            if (chapterId.ordinal() == all.length - 1) {
                return;
            }
            chapterId = all[chapterId.ordinal() + 1];
            target = 0;
        }
        page = target;
        direction = delta;
        pageSince = Util.getMillis();
        refreshButtons();
    }

    private void select(Hint.Chapter chapter) {
        if (chapter == chapterId) {
            return;
        }
        direction = chapter.ordinal() > chapterId.ordinal() ? 1 : -1;
        chapterId = chapter;
        page = 0;
        pageSince = Util.getMillis();
        refreshButtons();
        click();
    }

    private void click() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
        }
    }

    @Override
    public void removed() {
        GuideClient.remember(chapterId, page);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        long now = Util.getMillis();
        dt = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        time = Anim.now();
        float age = (now - openedAt) / 1000f;
        Chapter chapter = chapter();
        int accent = chapter.accent();
        Canvas c = new Canvas(g);
        backdrop.update(dt);
        backdrop.emit(9f, dt, () -> {
            var r = backdrop.random();
            backdrop.spawn(UiParticles.Kind.MOTE, r.nextFloat() * width, height + 4, (r.nextFloat() - 0.5f) * 6f, -6f - r.nextFloat() * 10f,
                    6f + r.nextFloat() * 6f, 1.5f + r.nextFloat() * 2.5f, Theme.fade(accent, 0.18f + r.nextFloat() * 0.2f));
        });
        backdrop.draw(c);
        Chassis.backdrop(c, left, top, W, H, accent, time);
        Chassis.panel(c, left, top, W, H, accent, Icon.BOOK, false, time, age);
        c.glowText(font, Texts.tr(Texts.MANUAL_TITLE), left + 20, top + 4, Theme.TEXT, accent);
        c.text(font, Texts.tr(Texts.MANUAL_SUBTITLE), left + 22 + font.width(Texts.tr(Texts.MANUAL_TITLE)), top + 5.5f, 0.6f, Theme.TEXT_MUTED);
        rail(c, mouseX, mouseY);
        pageArea(c, mouseX, mouseY);
        c.flush();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        Canvas c = new Canvas(g);
        Chassis.opening(c, left, top, W, H, chapter().accent(), (Util.getMillis() - openedAt) / 1000f);
        c.flush();
    }

    private void rail(Canvas c, int mouseX, int mouseY) {
        float x = left + 4;
        float y = top + Chassis.HEADER + 6;
        c.rect(left + RAIL, top + Chassis.HEADER + 2, 0.6f, H - Chassis.HEADER - 6, Theme.fade(Theme.ACCENT_CARD, 0.3f));
        List<Chapter> chapters = ManualContent.chapters();
        float row = railRow(chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            Chapter ch = chapters.get(i);
            float ry = y + i * row;
            boolean active = ch.id() == chapterId;
            boolean hover = mouseX >= x && mouseX < x + RAIL - 8 && mouseY >= ry && mouseY < ry + (row - 2);
            int done = 0;
            for (Goal goal : ch.goals()) {
                if (GuideClient.done(goal.milestone())) {
                    done++;
                }
            }
            boolean complete = done == ch.goals().size();
            c.round(x, ry, RAIL - 8, (row - 2), 2, active ? Theme.fade(ch.accent(), 0.2f) : hover ? 0x14FFFFFF : 0x06FFFFFF);
            if (active) {
                c.rect(x, ry, 1.6f, (row - 2), ch.accent());
                c.bloom(x + 1, ry + ((row - 2)) / 2f, 8, Theme.fade(ch.accent(), 0.35f));
            }
            Icons.draw(c, ch.icon(), x + 9, ry + ((row - 2)) / 2f, 9, active || hover ? ch.accent() : Theme.fade(ch.accent(), 0.6f));
            String title = font.plainSubstrByWidth(Component.translatable(ch.titleKey()).getString(), (int) ((RAIL - 34) / 0.7f));
            c.text(font, Component.literal(title), x + 17, ry + Math.min(3f, row - 12f), 0.7f, active ? Theme.TEXT : Theme.TEXT_DIM);
            for (int gIdx = 0; gIdx < ch.goals().size(); gIdx++) {
                boolean ok = GuideClient.done(ch.goals().get(gIdx).milestone());
                float px = x + 18 + gIdx * 6;
                c.rect(px, ry + row - 4.6f, 4, 2.2f, ok ? Theme.GOOD : 0x30FFFFFF);
            }
            if (complete) {
                Icons.draw(c, Icon.CHECK, x + RAIL - 15, ry + ((row - 2)) / 2f, 7, Theme.GOOD);
            }
        }
        Component key = GuideClient.keyName();
        if (key != null) {
            c.centered(font, Texts.tr(Texts.MANUAL_KEY, key), left + RAIL / 2f, top + H - 28, 0.55f, Theme.TEXT_MUTED);
        }
    }

    private void pageArea(Canvas c, int mouseX, int mouseY) {
        Chapter chapter = chapter();
        Page current = chapter.pages().get(page);
        int accent = chapter.accent();
        float px = left + RAIL + 8;
        float pw = W - RAIL - 16;
        float k = Anim.easeOutCubic((Util.getMillis() - pageSince) / 280f);
        c.text(font, Component.translatable(chapter.titleKey()), px, top + Chassis.HEADER + 4, 0.55f,
                Theme.fade(accent, 0.8f));
        c.right(font, Texts.tr(Texts.MANUAL_PAGE, page + 1, chapter.pages().size()), px + pw, top + Chassis.HEADER + 4, 0.55f,
                Theme.TEXT_MUTED);
        float saved = c.alpha();
        c.alpha(k);
        c.g().pose().pushMatrix();
        c.g().pose().translate((1f - k) * 14f * direction, 0);
        c.glowText(font, Component.translatable(current.titleKey()), px, top + Chassis.HEADER + 11, Theme.TEXT, accent);
        c.rectH(px, top + Chassis.HEADER + 22, pw, 0.6f, Theme.fade(accent, 0.6f), Theme.fade(accent, 0f));
        float y = top + Chassis.HEADER + 27;
        for (Element element : current.elements()) {
            y = element(c, element, px, y, pw, accent, mouseX, mouseY) + 4;
        }
        c.flush();
        c.g().pose().popMatrix();
        c.alpha(saved);
        footer(c, chapter, px, pw, mouseX, mouseY);
    }

    private float element(Canvas c, Element element, float x, float y, float w, int accent, int mouseX, int mouseY) {
        return switch (element) {
            case ManualContent.Para para -> paragraph(c, Component.translatable(para.key()), x, y, w, Theme.TEXT);
            case ManualContent.Diagram diagram -> {
                Diagrams.draw(diagram.kind(), c, font, catalog(), x, y, w, diagram.height(), time);
                yield y + diagram.height();
            }
            case ManualContent.Recipe recipe -> recipe(c, recipe.item(), x, y, w, accent, mouseX, mouseY);
            case ManualContent.Showcase showcase -> showcase(c, showcase.items(), x, y, mouseX, mouseY);
            case ManualContent.Tip tip -> tip(c, tip.key(), x, y, w, accent);
            case ManualContent.Flow flow -> flow(c, flow, x, y, w, accent, mouseX, mouseY);
        };
    }

    private float paragraph(Canvas c, Component text, float x, float y, float w, int color) {
        List<FormattedCharSequence> lines = font.split(text, (int) (w / TEXT_SCALE));
        c.flush();
        for (int i = 0; i < lines.size(); i++) {
            c.g().pose().pushMatrix();
            c.g().pose().translate(x, y + i * LINE);
            c.g().pose().scale(TEXT_SCALE, TEXT_SCALE);
            c.g().text(font, lines.get(i), 0, 0, Theme.fade(color, c.alpha()), false);
            c.g().pose().popMatrix();
        }
        return y + lines.size() * LINE;
    }

    private float tip(Canvas c, String key, float x, float y, float w, int accent) {
        List<FormattedCharSequence> lines = font.split(Component.translatable(key), (int) ((w - 22) / 0.72f));
        float h = lines.size() * 7 + 8;
        c.round(x, y, w, h, 3, Theme.fade(Theme.shade(accent, 0.35f), 0.35f));
        c.rect(x, y, 1.6f, h, accent);
        Icons.draw(c, Icon.LIGHT, x + 9, y + h / 2f, 8, Theme.WARN);
        c.flush();
        for (int i = 0; i < lines.size(); i++) {
            c.g().pose().pushMatrix();
            c.g().pose().translate(x + 18, y + 4.5f + i * 7);
            c.g().pose().scale(0.72f, 0.72f);
            c.g().text(font, lines.get(i), 0, 0, Theme.fade(Theme.TEXT, c.alpha()), false);
            c.g().pose().popMatrix();
        }
        return y + h;
    }

    private float showcase(Canvas c, List<Supplier<? extends ItemLike>> items, float x, float y, int mouseX, int mouseY) {
        float ix = x;
        for (Supplier<? extends ItemLike> item : items) {
            ItemStack stack = new ItemStack(item.get());
            boolean hover = mouseX >= ix && mouseX < ix + 18 && mouseY >= y && mouseY < y + 18;
            Instruments.well(c, ix, y, 18, 18);
            if (hover) {
                c.haloRect(ix, y, 18, 18, 3, Theme.fade(Theme.ACCENT_CARD, 0.4f));
            }
            c.flush();
            c.g().item(stack, (int) ix + 1, (int) y + 1);
            if (hover) {
                c.g().setTooltipForNextFrame(font, stack, mouseX, mouseY);
            }
            ix += 22;
        }
        return y + 18;
    }

    private float flow(Canvas c, ManualContent.Flow flow, float x, float y, float w, int accent, int mouseX, int mouseY) {
        int n = flow.stacks().size();
        float step = (w - 18) / Math.max(1, n - 1);
        for (int i = 0; i < n; i++) {
            float ix = x + i * step;
            ItemStack stack = flow.stacks().get(i).get();
            Instruments.well(c, ix, y, 18, 18);
            float pulse = Anim.pulse(time - i * 0.35f, 0.5f);
            c.haloRect(ix, y, 18, 18, 2 + 2 * pulse, Theme.fade(accent, 0.15f + 0.25f * pulse));
            c.flush();
            c.g().item(stack, (int) ix + 1, (int) y + 1);
            if (mouseX >= ix && mouseX < ix + 18 && mouseY >= y && mouseY < y + 18) {
                c.g().setTooltipForNextFrame(font, stack, mouseX, mouseY);
            }
            if (i < n - 1) {
                float ax0 = ix + 21;
                float ax1 = ix + step - 3;
                c.dashed(ax0, y + 9, ax1 - 3, y + 9, 1f, 3, 2, -time * 12f, Theme.fade(accent, 0.7f));
                c.tri(ax1 - 4, y + 6, accent, ax1 - 4, y + 12, accent, ax1, y + 9, accent);
                if (i < flow.labels().size()) {
                    c.centered(font, Component.translatable(flow.labels().get(i)), (ax0 + ax1) / 2f, y + 12, 0.55f, Theme.TEXT_DIM);
                }
            }
        }
        return y + 22;
    }

    private float recipe(Canvas c, Supplier<? extends ItemLike> item, float x, float y, float w, int accent, int mouseX, int mouseY) {
        Instruments.section(c, font, Texts.tr(Texts.MANUAL_RECIPE), x, y, w, accent);
        float gy = y + 7;
        ItemStack target = new ItemStack(item.get());
        if (minecraft == null || minecraft.level == null) {
            c.text(font, Texts.tr(Texts.MANUAL_RECIPE_OFFLINE), x, gy + 4, 0.65f, Theme.TEXT_MUTED);
            return gy + 12;
        }
        List<RecipeDisplay> displays = GuideClient.recipes(target.getItem());
        if (displays == null) {
            c.text(font, Texts.tr(Texts.MANUAL_RECIPE_LOADING), x, gy + 4, 0.65f, Theme.fade(Theme.TEXT_DIM, 0.5f + 0.5f * Anim.pulse(time, 1.5f)));
            return gy + 12;
        }
        if (displays.isEmpty()) {
            c.text(font, Texts.tr(Texts.MANUAL_RECIPE_NONE), x, gy + 4, 0.65f, Theme.TEXT_MUTED);
            return gy + 12;
        }
        RecipeDisplay display = displays.get((int) (time / 4f) % displays.size());
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        List<SlotDisplay> slots = new ArrayList<>();
        int width = 3;
        boolean shapeless = false;
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            width = shaped.width();
            slots.addAll(shaped.ingredients());
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapelessDisplay) {
            shapeless = true;
            slots.addAll(shapelessDisplay.ingredients());
        }
        float cell = 17;
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            float sx = x + col * cell;
            float sy = gy + row * cell;
            c.rect(sx, sy, cell - 1, cell - 1, Theme.WELL);
            c.frame(sx, sy, cell - 1, cell - 1, 0.6f, Theme.WELL_EDGE);
        }
        c.flush();
        for (int i = 0; i < slots.size(); i++) {
            int row = shapeless ? i / 3 : i / width;
            int col = shapeless ? i % 3 : i % width;
            List<ItemStack> options = slots.get(i).resolveForStacks(context);
            if (options.isEmpty() || row > 2 || col > 2) {
                continue;
            }
            ItemStack shown = options.get((int) time % options.size());
            float sx = x + col * cell;
            float sy = gy + row * cell;
            c.g().item(shown, (int) sx, (int) sy);
            if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
                c.g().setTooltipForNextFrame(font, shown, mouseX, mouseY);
            }
        }
        float ax = x + cell * 3 + 6;
        float ay = gy + cell * 1.5f - 1;
        c.dashed(ax, ay, ax + 14, ay, 1.2f, 3, 2, -time * 10f, Theme.fade(accent, 0.8f));
        c.tri(ax + 14, ay - 3, accent, ax + 14, ay + 3, accent, ax + 19, ay, accent);
        ItemStack result = display.result().resolveForFirstStack(context);
        float rx = ax + 24;
        float ry = gy + cell - 2;
        Instruments.well(c, rx - 1, ry - 1, 20, 20);
        c.haloRect(rx - 1, ry - 1, 20, 20, 3 + Anim.pulse(time, 0.8f) * 2, Theme.fade(accent, 0.3f));
        c.flush();
        c.g().item(result, (int) rx + 1, (int) ry + 1);
        c.g().itemDecorations(font, result, (int) rx + 1, (int) ry + 1);
        if (mouseX >= rx && mouseX < rx + 18 && mouseY >= ry && mouseY < ry + 18) {
            c.g().setTooltipForNextFrame(font, result, mouseX, mouseY);
        }
        float tx = rx + 26;
        c.text(font, result.getHoverName(), tx, ry + 2, 0.75f, Theme.TEXT);
        if (shapeless) {
            c.text(font, Texts.tr(Texts.MANUAL_SHAPELESS), tx, ry + 10, 0.55f, Theme.TEXT_MUTED);
        }
        if (displays.size() > 1) {
            for (int i = 0; i < displays.size(); i++) {
                c.rect(tx + i * 5, ry + 17, 3, 1.5f, displays.get(i) == display ? accent : 0x40FFFFFF);
            }
        }
        return gy + cell * 3;
    }

    private void footer(Canvas c, Chapter chapter, float px, float pw, int mouseX, int mouseY) {
        float fy = top + H - 17;
        int pages = chapter.pages().size();
        float dotsW = pages * 7;
        float dx = px + 24;
        for (int i = 0; i < pages; i++) {
            boolean on = i == page;
            c.round(dx + i * 7, fy + 2.5f, on ? 5 : 3, 3, 1.5f, on ? chapter.accent() : 0x40FFFFFF);
        }
        float gx = px + 30 + dotsW;
        Instruments.section(c, font, Texts.tr(Texts.MANUAL_TASKS), gx, fy + 2, 46, chapter.accent());
        float chipX = gx + 50;
        List<Component> tip = new ArrayList<>();
        tip.add(Texts.tr(Texts.MANUAL_TASKS));
        boolean hover = false;
        for (Goal goal : chapter.goals()) {
            boolean ok = GuideClient.done(goal.milestone());
            Icons.draw(c, ok ? Icon.CHECK : Icon.MINUS, chipX + 4, fy + 5, 7, ok ? Theme.GOOD : Theme.TEXT_MUTED);
            if (mouseX >= chipX && mouseX < chipX + 9 && mouseY >= fy && mouseY < fy + 10) {
                hover = true;
            }
            tip.add(Component.literal(ok ? "[x] " : "[ ] ").append(Component.translatable(goal.labelKey())));
            chipX += 10;
        }
        if (mouseX >= gx && mouseX < gx + 50 && mouseY >= fy && mouseY < fy + 10) {
            hover = true;
        }
        if (hover) {
            c.g().setComponentTooltipForNextFrame(font, tip, mouseX, mouseY);
        }
    }

    private static float railRow(int chapters) {
        return Math.min(ROW, (H - Chassis.HEADER - 6 - RAIL_FOOT) / (float) Math.max(1, chapters));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        float x = left + 4;
        float y = top + Chassis.HEADER + 6;
        List<Chapter> chapters = ManualContent.chapters();
        float row = railRow(chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            float ry = y + i * row;
            if (event.x() >= x && event.x() < x + RAIL - 8 && event.y() >= ry && event.y() < ry + (row - 2)) {
                select(chapters.get(i).id());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (x >= left + RAIL && x < left + W && y >= top && y < top + H && scrollY != 0) {
            turn(scrollY < 0 ? 1 : -1);
            click();
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isRight()) {
            turn(1);
            click();
            return true;
        }
        if (event.isLeft()) {
            turn(-1);
            click();
            return true;
        }
        if (event.isDown() && chapterId.ordinal() < Hint.Chapter.values().length - 1) {
            select(Hint.Chapter.values()[chapterId.ordinal() + 1]);
            return true;
        }
        if (event.isUp() && chapterId.ordinal() > 0) {
            select(Hint.Chapter.values()[chapterId.ordinal() - 1]);
            return true;
        }
        if (GuideClient.OPEN_MANUAL.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    public void show(Hint.Chapter chapter, int pageIndex) {
        chapterId = chapter;
        page = Math.max(0, Math.min(pageIndex, chapter().pages().size() - 1));
        pageSince = Util.getMillis() - 1000;
        refreshButtons();
    }
}
