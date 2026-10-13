package com.cultivar.client.screen;

import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.CropSpecies;
import com.cultivar.market.Grade;
import com.cultivar.market.MarketValues;
import com.cultivar.menu.ProduceExchangeMenu;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ProduceExchangeScreen extends CultivarContainerScreen<ProduceExchangeMenu> {
    public static final Map<Identifier, Float> DEMAND = new HashMap<>();
    private static final int BOARD_X = 76;
    private static final int BOARD_Y = 20;
    private static final int ROW = 11;
    private boolean goods;

    public ProduceExchangeScreen(ProduceExchangeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ProduceExchangeMenu.WIDTH, ProduceExchangeMenu.HEIGHT);
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
        for (int i = 0; i < ProduceExchangeBlockEntity.CRATE_SLOTS; i++) {
            if (menu.getSlot(i).hasItem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return slot.getContainerSlot() < ProduceExchangeBlockEntity.CRATE_SLOTS ? Icon.SEED : null;
    }

    private List<Item> rows() {
        List<Item> out = new ArrayList<>();
        if (!goods) {
            for (CropSpecies species : CropSpecies.VALUES) {
                out.add(ModItems.produce(species));
            }
        } else {
            for (Item item : new Item[]{ModItems.FIELD_MEAL.get(), ModItems.SOY_OIL.get(), ModItems.LINEN_CANVAS.get(), ModItems.STARCH.get(),
                    ModItems.BIOPLASTIC.get(), ModItems.PICKLED_BRASSICA.get(), ModItems.GOURD_PUREE.get(), ModItems.BIOFUEL_CAKE.get(),
                    ModItems.RATION_PACK.get()}) {
                out.add(item);
            }
        }
        return out;
    }

    private static float demand(Item item) {
        return DEMAND.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 1f);
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        float x0 = leftPos;
        float y0 = topPos;
        c.text(font, Texts.tr(Texts.UI_EX_CRATE), x0 + 13, y0 + 20, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_EX_PAYOUT), x0 + 31, y0 + 90, Instruments.MICRO, Theme.TEXT_DIM);
        float flow = time * 10f;
        c.dashed(x0 + 40, y0 + 85, x0 + 40, y0 + 98, 1f, 2, 2, -flow, Theme.fade(Theme.ACCENT_BANK, 0.6f));

        float bx = x0 + BOARD_X;
        float by = y0 + BOARD_Y;
        float bw = imageWidth - BOARD_X - 8;
        Instruments.well(c, bx, by, bw, 84);
        tab(c, bx + 2, by - 1 + 2, Texts.tr(Texts.UI_EX_PRODUCE), !goods, mouseX, mouseY);
        tab(c, bx + 50, by - 1 + 2, Texts.tr(Texts.UI_EX_GOODS), goods, mouseX, mouseY);
        c.text(font, Texts.tr(Texts.UI_EX_HEADER_C), bx + bw - 66, by + 4, 0.5f, Theme.TEXT_MUTED);
        c.text(font, Texts.tr(Texts.UI_EX_HEADER_PRIME), bx + bw - 40, by + 4, 0.5f, Grade.PRIME.color());
        List<Item> rows = rows();
        float row = Math.min(ROW, 71f / rows.size());
        for (int i = 0; i < rows.size(); i++) {
            Item item = rows.get(i);
            ItemStack stack = new ItemStack(item);
            float ry = by + 12 + i * row;
            if (i % 2 == 0) {
                c.rect(bx + 1, ry - 1, bw - 2, row, 0x0CFFFFFF);
            }
            c.flush();
            c.g().pose().pushMatrix();
            c.g().pose().translate(bx + 3, ry - 0.5f);
            c.g().pose().scale(0.6f, 0.6f);
            c.g().item(stack, 0, 0);
            c.g().pose().popMatrix();
            String name = font.plainSubstrByWidth(stack.getHoverName().getString(), (int) ((bw - 112) / 0.6f));
            c.text(font, Component.literal(name), bx + 15, ry + 2, 0.6f, Theme.TEXT);
            float d = demand(item);
            float base = MarketValues.base(stack);
            c.right(font, Component.literal(price(base * Grade.C.priceMultiplier() * d)), bx + bw - 46, ry + 2, 0.6f, Theme.TEXT_DIM);
            c.right(font, Component.literal(price(base * Grade.PRIME.priceMultiplier() * d)), bx + bw - 18, ry + 2, 0.6f, Grade.PRIME.color());
            float gx = bx + bw - 15;
            c.rect(gx, ry + 1, 12, 4, 0xFF0D1A1C);
            c.rect(gx, ry + 1, 12 * d, 4, Theme.quality(d));
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= ry - 1 && mouseY < ry + row - 1) {
                tooltip(c.g(), List.of(stack.getHoverName(), Texts.tr(Texts.UI_EX_DEMAND, Math.round(d * 100)),
                        Texts.tr(Texts.UI_EX_GRADES, price(base * d), price(base * Grade.B.priceMultiplier() * d),
                                price(base * Grade.A.priceMultiplier() * d), price(base * Grade.PRIME.priceMultiplier() * d))), mouseX, mouseY);
            }
        }

        float fy = by + 87;
        float estimate = 0;
        for (int i = 0; i < ProduceExchangeBlockEntity.CRATE_SLOTS; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                float base = MarketValues.base(stack);
                estimate += base * com.cultivar.market.GradeLogic.of(stack).priceMultiplier() * demand(stack.getItem()) * stack.getCount();
            }
        }
        c.text(font, Texts.tr(Texts.UI_EX_ESTIMATE, String.format(Locale.ROOT, "%.2f", estimate)), bx, fy, 0.6f, Theme.ACCENT_BANK);
        long clock = minecraft != null && minecraft.level != null ? minecraft.level.getOverworldClockTime() : 0L;
        long ticksLeft = 24000L - (clock % 24000L);
        long seconds = ticksLeft / 20;
        c.right(font, Texts.tr(Texts.UI_EX_DAWN, String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)), bx + bw, fy + 7, 0.5f,
                Theme.fade(Theme.TEXT_DIM, 0.7f + 0.3f * Anim.pulse(time, 0.5f)));
        if (minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.pos()) instanceof ProduceExchangeBlockEntity exchange && exchange.lastSaleItems() > 0) {
            c.text(font, Texts.tr(Texts.UI_EX_LAST, exchange.lastSaleItems(), String.format(Locale.ROOT, "%.2f", exchange.lastSaleValue())), bx,
                    fy + 7, 0.5f, Theme.TEXT_MUTED);
        }
    }

    private static String price(float emeralds) {
        return String.format(Locale.ROOT, emeralds >= 10 ? "%.0f" : "%.2f", emeralds);
    }

    private void tab(Canvas c, float x, float y, Component label, boolean active, int mouseX, int mouseY) {
        float w = 46;
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 8;
        c.round(x, y, w, 8, 2, active ? Theme.fade(Theme.ACCENT_BANK, 0.25f) : hover ? 0x18FFFFFF : 0x08FFFFFF);
        if (active) {
            c.rect(x + 2, y + 7.4f, w - 4, 0.8f, Theme.ACCENT_BANK);
        }
        c.centered(font, label, x + w / 2f, y + 2, 0.55f, active ? Theme.ACCENT_BANK : Theme.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        float bx = leftPos + BOARD_X;
        float by = topPos + BOARD_Y;
        if (event.y() >= by + 1 && event.y() < by + 9) {
            if (event.x() >= bx + 2 && event.x() < bx + 48) {
                goods = false;
                return true;
            }
            if (event.x() >= bx + 50 && event.x() < bx + 96) {
                goods = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
