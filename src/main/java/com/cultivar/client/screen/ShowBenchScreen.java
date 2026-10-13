package com.cultivar.client.screen;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.market.GradeLogic;
import com.cultivar.mastery.Certificate;
import com.cultivar.menu.ShowBenchMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class ShowBenchScreen extends CultivarContainerScreen<ShowBenchMenu> {
    private static final int DIAL_X = 74;
    private static final int DIAL_Y = 58;
    private static final int GOLD_COLOR = 0xFFF2C94C;
    private static final int SILVER_COLOR = 0xFFC9D3DA;
    private static final int BRONZE_COLOR = 0xFFD08A4E;
    private final Anim.Smooth needle = new Anim.Smooth(5f).from(0f);
    private @Nullable LabButton judge;

    public ShowBenchScreen(ShowBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ShowBenchMenu.WIDTH, ShowBenchMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_CARD;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.SHIELD;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return switch (slot.getContainerSlot()) {
            case ShowBenchBlockEntity.SLOT_PACKET -> Icon.SEED;
            case ShowBenchBlockEntity.SLOT_PAPER -> Icon.PAPER;
            case ShowBenchBlockEntity.SLOT_RIBBON -> Icon.SHIELD;
            default -> null;
        };
    }

    private Catalog catalog() {
        return minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults();
    }

    private @Nullable ShowBenchBlockEntity bench() {
        return minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(menu.pos()) instanceof ShowBenchBlockEntity b ? b : null;
    }

    private @Nullable Genome genome() {
        return menu.getSlot(ShowBenchBlockEntity.SLOT_PACKET).getItem().get(ModDataComponents.GENOME.get());
    }

    @Override
    protected void init() {
        super.init();
        judge = addRenderableWidget(new LabButton(leftPos + DIAL_X - 30, topPos + 102, 60, 14, Texts.tr(Texts.UI_SB_JUDGE), Icon.CHECK, Theme.ACCENT_CARD,
                in -> clickButton(0)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (judge != null) {
            int cost = CultivarConfig.SHOW_BENCH_PAPER_COST.get();
            ItemStack paper = menu.getSlot(ShowBenchBlockEntity.SLOT_PAPER).getItem();
            judge.active = genome() != null && !menu.getSlot(ShowBenchBlockEntity.SLOT_OUTPUT).hasItem()
                    && (cost <= 0 || (paper.is(Items.PAPER) && paper.getCount() >= cost));
        }
    }

    private static int ribbonColor(String ribbon) {
        return switch (ribbon) {
            case "gold" -> GOLD_COLOR;
            case "silver" -> SILVER_COLOR;
            case "bronze" -> BRONZE_COLOR;
            default -> Theme.TEXT_MUTED;
        };
    }

    private static Component ribbonName(String ribbon) {
        return Texts.tr(switch (ribbon) {
            case "gold" -> Texts.RIBBON_GOLD;
            case "silver" -> Texts.RIBBON_SILVER;
            case "bronze" -> Texts.RIBBON_BRONZE;
            default -> Texts.RIBBON_NONE;
        });
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        float x0 = leftPos;
        float y0 = topPos;
        Catalog catalog = catalog();
        Genome genome = genome();
        int score = genome != null ? ShowBenchBlockEntity.score(genome, catalog) : 0;
        float shown = needle.update(score / 100f, dt);

        c.text(font, Texts.tr(Texts.UI_SB_ENTRY), x0 + 13, y0 + 26, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_SB_PAPER, CultivarConfig.SHOW_BENCH_PAPER_COST.get()), x0 + 13, y0 + 70, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_SB_CERTIFIED), x0 + 205, y0 + 26, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_SB_RIBBON), x0 + 207, y0 + 70, Instruments.MICRO, Theme.TEXT_DIM);

        float cx = x0 + DIAL_X;
        float cy = y0 + DIAL_Y;
        float r = 26;
        float a0 = (float) Math.toRadians(150);
        float span = (float) Math.toRadians(240);
        c.disc(cx, cy, r + 4, 0xFF15201F, 0xFF101817);
        band(c, cx, cy, r, a0, span, 0, ShowBenchBlockEntity.BRONZE, 0x40FFFFFF);
        band(c, cx, cy, r, a0, span, ShowBenchBlockEntity.BRONZE, ShowBenchBlockEntity.SILVER, BRONZE_COLOR);
        band(c, cx, cy, r, a0, span, ShowBenchBlockEntity.SILVER, ShowBenchBlockEntity.GOLD, SILVER_COLOR);
        band(c, cx, cy, r, a0, span, ShowBenchBlockEntity.GOLD, 100, GOLD_COLOR);
        for (int tick = 0; tick <= 10; tick++) {
            float a = a0 + span * tick / 10f;
            c.line(cx + Canvas.cos(a) * (r - 6), cy + Canvas.sin(a) * (r - 6), cx + Canvas.cos(a) * (r - 3), cy + Canvas.sin(a) * (r - 3), 0.6f,
                    0x80FFFFFF);
        }
        float na = a0 + span * shown;
        String ribbon = ShowBenchBlockEntity.ribbon(score);
        int color = genome != null ? ribbonColor(ribbon) : 0x60FFFFFF;
        c.line(cx, cy, cx + Canvas.cos(na) * (r - 4), cy + Canvas.sin(na) * (r - 4), 1.2f, color);
        c.disc(cx, cy, 2.4f, color, Theme.fade(color, 0.4f));
        c.centered(font, Component.literal(genome != null ? Integer.toString(Math.round(shown * 100)) : "--"), cx, cy + 9, 1f, color);
        if (genome != null) {
            c.centered(font, ribbonName(ribbon), cx, cy + 19, 0.55f, color);
            if ("gold".equals(ribbon)) {
                c.bloom(cx, cy, r + 6, Theme.fade(GOLD_COLOR, 0.12f + 0.08f * Anim.pulse(time, 1f)));
            }
        } else {
            c.centered(font, Texts.tr(Texts.UI_SB_EMPTY), cx, cy + 19, 0.5f, Theme.fade(Theme.TEXT_MUTED, 0.7f + 0.3f * Anim.pulse(time, 0.5f)));
        }
        if (mouseX >= cx - r && mouseX < cx + r && mouseY >= cy - r && mouseY < cy + r) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_SB_SCALE), Texts.tr(Texts.UI_SB_THRESHOLDS, ShowBenchBlockEntity.BRONZE, ShowBenchBlockEntity.SILVER,
                    ShowBenchBlockEntity.GOLD)), mouseX, mouseY);
        }

        float bx = x0 + 112;
        float by = y0 + 22;
        Instruments.section(c, font, Texts.tr(Texts.UI_SB_BREAKDOWN), bx, by, 86, Theme.ACCENT_CARD);
        if (genome != null) {
            float genetic = GradeLogic.geneticScore(Phenotype.resolve(genome, catalog.table()), catalog);
            row(c, bx, by + 12, Texts.tr(Texts.UI_SB_GENETIC), Math.round(genetic * 100) + "", Theme.ACCENT_LAB);
            Instruments.bar(c, bx, by + 20, 86, 2, genetic, Theme.ACCENT_LAB, time);
            boolean pure = genome.isTrueBreeding();
            row(c, bx, by + 25, Texts.tr(Texts.UI_SB_TRUE), pure ? "+" + ShowBenchBlockEntity.TRUE_BREEDING_BONUS : "+0", pure ? Theme.GOOD : Theme.TEXT_MUTED);
            row(c, bx, by + 34, Texts.tr(Texts.UI_SB_TOTAL), Integer.toString(score), color);
            int next = score < ShowBenchBlockEntity.BRONZE ? ShowBenchBlockEntity.BRONZE : score < ShowBenchBlockEntity.SILVER
                    ? ShowBenchBlockEntity.SILVER : score < ShowBenchBlockEntity.GOLD ? ShowBenchBlockEntity.GOLD : -1;
            c.text(font, next > 0 ? Texts.tr(Texts.UI_SB_NEXT, next - score) : Texts.tr(Texts.UI_SB_TOP), bx, by + 44, 0.5f, Theme.TEXT_DIM);
        } else {
            c.text(font, Texts.tr(Texts.UI_SB_HOW_1), bx, by + 12, 0.5f, Theme.TEXT_DIM);
            c.text(font, Texts.tr(Texts.UI_SB_HOW_2), bx, by + 19, 0.5f, Theme.TEXT_DIM);
            c.text(font, Texts.tr(Texts.UI_SB_HOW_3), bx, by + 26, 0.5f, Theme.TEXT_DIM);
        }

        ShowBenchBlockEntity bench = bench();
        Optional<Certificate> last = bench != null ? bench.last() : Optional.empty();
        float sx = bx;
        float sy = y0 + 76;
        c.shadowRect(sx, sy, 86, 40, 2, 0x50000000);
        c.round(sx, sy, 86, 40, 2, 0xFFE9E1C8);
        c.stripes(sx, sy + 8, 86, 32, 6, 0, 0x00000000, 0x10406060);
        c.text(font, Texts.tr(Texts.UI_SB_LAST), sx + 4, sy + 3, 0.5f, 0xFF5A5040);
        if (last.isPresent()) {
            Certificate cert = last.get();
            int rc = ribbonColor(cert.ribbon());
            String species = bench.lastSpecies().map(id -> GeneText.species(id).getString()).orElse("?");
            c.text(font, Component.literal(font.plainSubstrByWidth(species, (int) (60 / 0.6f))), sx + 4, sy + 11, 0.6f, 0xFF2E2A22);
            c.text(font, Texts.tr(Texts.UI_SB_SLIP_SCORE, cert.score()), sx + 4, sy + 19, 0.55f, 0xFF2E2A22);
            c.text(font, Texts.tr(Texts.UI_SB_SLIP_BY, cert.breeder(), cert.day()), sx + 4, sy + 26, 0.45f, 0xFF6A6050);
            if (!"none".equals(cert.ribbon())) {
                Icons.draw(c, Icon.SHIELD, sx + 76, sy + 16, 12, rc);
            }
            if (cert.record()) {
                c.text(font, Texts.tr(Texts.TIP_CERTIFIED_RECORD), sx + 4, sy + 33, 0.45f, 0xFFB03A2E);
            }
        } else {
            c.text(font, Texts.tr(Texts.UI_SB_NONE_YET), sx + 4, sy + 16, 0.5f, 0xFF6A6050);
        }
        if (mouseX >= sx && mouseX < sx + 86 && mouseY >= sy && mouseY < sy + 40) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_SB_RECORD_HELP)), mouseX, mouseY);
        }
    }

    private void row(Canvas c, float x, float y, Component label, String value, int color) {
        c.text(font, label, x, y, 0.55f, Theme.TEXT_DIM);
        c.right(font, Component.literal(value), x + 86, y, 0.55f, color);
    }

    private static void band(Canvas c, float cx, float cy, float r, float a0, float span, int from, int to, int color) {
        c.arc(cx, cy, r - 2.5f, r, a0 + span * from / 100f, a0 + span * to / 100f, color, color);
    }
}
