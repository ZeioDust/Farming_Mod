package com.cultivar.client.screen;

import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.client.ui.Anim;
import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Icons;
import com.cultivar.client.ui.Instruments;
import com.cultivar.client.ui.LabButton;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.Catalog;
import com.cultivar.market.Contract;
import com.cultivar.menu.ContractBoardMenu;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class ContractBoardScreen extends CultivarContainerScreen<ContractBoardMenu> {
    private static final int LIST_X = 40;
    private static final int LIST_Y = 34;
    private static final int CARD_H = 32;
    private final LabButton[] deliver = new LabButton[ContractBoardBlockEntity.CONTRACTS];
    private final LabButton[] discard = new LabButton[ContractBoardBlockEntity.CONTRACTS];
    private final LabButton[] buy = new LabButton[ContractBoardBlockEntity.OFFERS.size()];
    private boolean supplies;

    public ContractBoardScreen(ContractBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ContractBoardMenu.WIDTH, ContractBoardMenu.HEIGHT);
    }

    @Override
    protected int accent() {
        return Theme.ACCENT_CARD;
    }

    @Override
    protected @Nullable Icon headerIcon() {
        return Icon.PAPER;
    }

    @Override
    protected boolean working() {
        return board() != null;
    }

    @Override
    protected @Nullable Icon slotHint(Slot slot) {
        return slot.getContainerSlot() == ContractBoardBlockEntity.SLOT_DELIVERY ? Icon.DEPOSIT : Icon.GRID;
    }

    private @Nullable ContractBoardBlockEntity board() {
        return minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(menu.pos()) instanceof ContractBoardBlockEntity b ? b : null;
    }

    private Catalog catalog() {
        return minecraft != null && minecraft.level != null ? Catalog.get(minecraft.level.registryAccess()) : Catalog.defaults();
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + imageWidth - 8;
        for (int i = 0; i < ContractBoardBlockEntity.CONTRACTS; i++) {
            int index = i;
            int y = topPos + LIST_Y + i * CARD_H + 17;
            deliver[i] = addRenderableWidget(new LabButton(x - 74, y, 56, 12, Texts.tr(Texts.UI_CB_DELIVER), Icon.DEPOSIT, Theme.GOOD,
                    in -> clickButton(ContractBoardMenu.DELIVER + index)));
            discard[i] = addRenderableWidget(new LabButton(x - 16, y, 12, 12, Component.empty(), Icon.CROSS, Theme.BAD,
                    in -> clickButton(ContractBoardMenu.DISCARD + index)).danger(true));
        }
        for (int j = 0; j < buy.length; j++) {
            int index = j;
            int col = j % 2;
            int row = j / 2;
            buy[j] = addRenderableWidget(new LabButton(leftPos + LIST_X + col * 126 + 84, topPos + LIST_Y + row * 24 + 5, 34, 12,
                    Texts.tr(Texts.UI_CB_BUY), null, Theme.ACCENT_BANK, in -> clickButton(ContractBoardMenu.BUY + index)));
        }
        refresh();
    }

    private void refresh() {
        ContractBoardBlockEntity board = board();
        for (int i = 0; i < ContractBoardBlockEntity.CONTRACTS; i++) {
            boolean present = board != null && board.contracts().get(i).isPresent();
            deliver[i].visible = !supplies && present;
            discard[i].visible = !supplies && present;
            deliver[i].active = !menu.getSlot(ContractBoardBlockEntity.SLOT_DELIVERY).getItem().isEmpty();
            discard[i].active = menu.getSlot(ContractBoardBlockEntity.SLOT_PAYMENT).getItem().is(Items.EMERALD);
        }
        int tier = board != null ? board.tier() : 0;
        ItemStack payment = menu.getSlot(ContractBoardBlockEntity.SLOT_PAYMENT).getItem();
        for (int j = 0; j < buy.length; j++) {
            ContractBoardBlockEntity.Offer offer = ContractBoardBlockEntity.OFFERS.get(j);
            buy[j].visible = supplies;
            buy[j].active = tier >= offer.tier() && payment.is(Items.EMERALD) && payment.getCount() >= offer.price();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refresh();
    }

    @Override
    protected void extractPanelContents(Canvas c, int mouseX, int mouseY) {
        ContractBoardBlockEntity board = board();
        c.text(font, Texts.tr(Texts.UI_CB_DELIVERY), leftPos + 11, topPos + 34, Instruments.MICRO, Theme.TEXT_DIM);
        c.text(font, Texts.tr(Texts.UI_CB_PAYMENT), leftPos + 11, topPos + 88, Instruments.MICRO, Theme.TEXT_DIM);
        float tx = leftPos + LIST_X;
        float ty = topPos + 20;
        tab(c, tx, ty, Texts.tr(Texts.UI_CB_TAB_CONTRACTS), !supplies, mouseX, mouseY);
        tab(c, tx + 64, ty, Texts.tr(Texts.UI_CB_TAB_SUPPLIES), supplies, mouseX, mouseY);
        int reputation = board != null ? board.reputation() : 0;
        int tier = board != null ? board.tier() : 0;
        float rx = leftPos + imageWidth - 10;
        for (int i = 3; i >= 0; i--) {
            float px = rx - (3 - i) * 8 - 6;
            Icons.draw(c, Icon.SHIELD, px, ty + 5, 7, i <= tier ? Theme.ACCENT_CARD : 0x40FFFFFF);
        }
        c.right(font, Texts.tr(Texts.UI_CB_REPUTATION, reputation, tier + 1), rx - 36, ty + 2.5f, 0.55f, Theme.TEXT_DIM);
        if (mouseX >= rx - 110 && mouseX < rx && mouseY >= ty && mouseY < ty + 10) {
            tooltip(c.g(), List.of(Texts.tr(Texts.UI_CB_TIER_HELP)), mouseX, mouseY);
        }
        for (LabButton button : discard) {
            if (button.visible && button.isHoveredOrFocused()) {
                tooltip(c.g(), List.of(Texts.tr(Texts.UI_CB_DISCARD_HELP)), mouseX, mouseY);
            }
        }
        if (board == null) {
            return;
        }
        if (supplies) {
            supplyList(c, board, mouseX, mouseY);
        } else {
            contractList(c, board, mouseX, mouseY);
        }
    }

    private void contractList(Canvas c, ContractBoardBlockEntity board, int mouseX, int mouseY) {
        Catalog catalog = catalog();
        float x = leftPos + LIST_X;
        float w = imageWidth - LIST_X - 8;
        for (int i = 0; i < ContractBoardBlockEntity.CONTRACTS; i++) {
            float y = topPos + LIST_Y + i * CARD_H;
            Optional<Contract> entry = board.contracts().get(i);
            if (entry.isEmpty()) {
                c.round(x, y, w, CARD_H - 3, 3, 0x0AFFFFFF);
                c.dashed(x + 2, y + (CARD_H - 3) / 2f, x + w - 2, y + (CARD_H - 3) / 2f, 0.6f, 4, 3, 0, 0x30FFFFFF);
                c.centered(font, Texts.tr(Texts.UI_CB_EMPTY), x + w / 2f, y + 10, 0.65f, Theme.fade(Theme.TEXT_MUTED, 0.6f + 0.4f * Anim.pulse(time, 0.5f)));
                continue;
            }
            Contract contract = entry.get();
            int kindColor = switch (contract.kind()) {
                case PRODUCE -> Theme.ACCENT_COMPOST;
                case TRAIT -> Theme.ACCENT_LAB;
                case ALLELE -> Theme.MUTANT;
            };
            c.round(x, y, w, CARD_H - 3, 3, 0x14FFFFFF);
            c.rect(x, y, 1.6f, CARD_H - 3, kindColor);
            Instruments.brackets(c, x - 1, y - 1, w + 2, CARD_H - 1, 3, 0.7f, Theme.fade(kindColor, 0.6f));
            ItemStack icon = contract.kind() == Contract.Kind.PRODUCE ? new ItemStack(ModItems.produce(contract.cropSpecies()))
                    : new ItemStack(ModItems.seedPacket(contract.cropSpecies()));
            c.flush();
            c.g().item(icon, (int) x + 4, (int) y + 3);
            Component title = contract.title(catalog);
            String text = font.plainSubstrByWidth(title.getString(), (int) ((w - 30) / 0.75f));
            c.text(font, Component.literal(text), x + 24, y + 3, 0.75f, Theme.TEXT);
            Component kind = Texts.tr(switch (contract.kind()) {
                case PRODUCE -> Texts.UI_CB_KIND_PRODUCE;
                case TRAIT -> Texts.UI_CB_KIND_TRAIT;
                case ALLELE -> Texts.UI_CB_KIND_ALLELE;
            });
            Instruments.chip(c, font, kind, x + 24, y + 11, kindColor, false);
            if (contract.kind() == Contract.Kind.PRODUCE) {
                float px = x + 24;
                Instruments.bar(c, px + 40, y + 13, 60, 3, contract.progress() / (float) contract.count(), Theme.ACCENT_COMPOST, time);
                c.text(font, Component.literal(contract.progress() + " / " + contract.count()), px + 104, y + 11.5f, 0.55f, Theme.TEXT_DIM);
            }
            float rx = x + 24;
            float ry = y + 20;
            c.flush();
            c.g().pose().pushMatrix();
            c.g().pose().translate(rx, ry);
            c.g().pose().scale(0.5f, 0.5f);
            c.g().item(new ItemStack(Items.EMERALD), 0, 0);
            c.g().pose().popMatrix();
            c.text(font, Texts.tr(Texts.UI_CB_REWARD, contract.reward()), rx + 10, ry + 1.5f, 0.6f, Theme.ACCENT_BANK);
            contract.bonus().ifPresent(template -> {
                ItemStack bonus = template.create();
                float bx = rx + 60;
                c.flush();
                c.g().pose().pushMatrix();
                c.g().pose().translate(bx, ry);
                c.g().pose().scale(0.5f, 0.5f);
                c.g().item(bonus, 0, 0);
                c.g().pose().popMatrix();
                c.text(font, Component.literal("+ " + bonus.getCount() + " " + bonus.getHoverName().getString()), bx + 10, ry + 1.5f, 0.55f,
                        Theme.TEXT_DIM);
                if (mouseX >= bx && mouseX < bx + 9 && mouseY >= ry && mouseY < ry + 9) {
                    c.g().setTooltipForNextFrame(font, bonus, mouseX, mouseY);
                }
            });
            if (mouseX >= x && mouseX < x + w - 78 && mouseY >= y && mouseY < y + 18) {
                tooltip(c.g(), List.of(title, Texts.tr(switch (contract.kind()) {
                    case PRODUCE -> Texts.UI_CB_HELP_PRODUCE;
                    case TRAIT -> Texts.UI_CB_HELP_TRAIT;
                    case ALLELE -> Texts.UI_CB_HELP_ALLELE;
                })), mouseX, mouseY);
            }
        }
    }

    private void supplyList(Canvas c, ContractBoardBlockEntity board, int mouseX, int mouseY) {
        float x0 = leftPos + LIST_X;
        for (int j = 0; j < ContractBoardBlockEntity.OFFERS.size(); j++) {
            ContractBoardBlockEntity.Offer offer = ContractBoardBlockEntity.OFFERS.get(j);
            int col = j % 2;
            int row = j / 2;
            float x = x0 + col * 126;
            float y = topPos + LIST_Y + row * 24;
            boolean locked = board.tier() < offer.tier();
            c.round(x, y, 122, 21, 3, locked ? 0x08FFFFFF : 0x14FFFFFF);
            ItemStack stack = offer.stack();
            c.flush();
            c.g().item(stack, (int) x + 3, (int) y + 2);
            c.g().itemDecorations(font, stack, (int) x + 3, (int) y + 2);
            String name = font.plainSubstrByWidth(stack.getHoverName().getString(), (int) (56 / 0.6f));
            c.text(font, Component.literal(name), x + 22, y + 3, 0.6f, locked ? Theme.TEXT_MUTED : Theme.TEXT);
            if (locked) {
                c.text(font, Texts.tr(Texts.UI_CB_LOCKED, offer.tier() + 1), x + 22, y + 11, 0.55f, Theme.BAD);
            } else {
                c.flush();
                c.g().pose().pushMatrix();
                c.g().pose().translate(x + 22, y + 10);
                c.g().pose().scale(0.5f, 0.5f);
                c.g().item(new ItemStack(Items.EMERALD), 0, 0);
                c.g().pose().popMatrix();
                c.text(font, Texts.tr(Texts.UI_CB_PRICE, offer.price()), x + 32, y + 11.5f, 0.55f, Theme.ACCENT_BANK);
            }
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                c.g().setTooltipForNextFrame(font, stack, mouseX, mouseY);
            }
        }
    }

    private void tab(Canvas c, float x, float y, Component label, boolean active, int mouseX, int mouseY) {
        float w = 60;
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 10;
        c.round(x, y, w, 10, 2, active ? Theme.fade(Theme.ACCENT_CARD, 0.22f) : hover ? 0x18FFFFFF : 0x08FFFFFF);
        if (active) {
            c.rect(x + 2, y + 9.2f, w - 4, 0.8f, Theme.ACCENT_CARD);
        }
        c.centered(font, label, x + w / 2f, y + 2.5f, 0.6f, active ? Theme.ACCENT_CARD : Theme.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        float tx = leftPos + LIST_X;
        float ty = topPos + 20;
        if (event.y() >= ty && event.y() < ty + 10) {
            if (event.x() >= tx && event.x() < tx + 60) {
                supplies = false;
                refresh();
                return true;
            }
            if (event.x() >= tx + 64 && event.x() < tx + 124) {
                supplies = true;
                refresh();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
