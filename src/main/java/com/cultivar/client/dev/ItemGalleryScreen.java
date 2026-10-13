package com.cultivar.client.dev;

import com.cultivar.client.ui.Canvas;
import com.cultivar.client.ui.Chassis;
import com.cultivar.client.ui.Theme;
import com.cultivar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGalleryScreen extends Screen {
    private static final int CELL = 38;
    private final boolean blocks;

    public ItemGalleryScreen(boolean blocks) {
        super(Component.literal("Cultivar items"));
        this.blocks = blocks;
    }

    private List<Item> items() {
        List<Item> out = new ArrayList<>();
        for (var entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.get();
            boolean isBlock = item instanceof net.minecraft.world.item.BlockItem && !(item instanceof com.cultivar.item.SeedPacketItem);
            if (isBlock == blocks) {
                out.add(item);
            }
        }
        return out;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        List<Item> items = items();
        int cols = Math.max(1, (width - 20) / CELL);
        int rows = (items.size() + cols - 1) / cols;
        int x0 = (width - cols * CELL) / 2;
        int y0 = Math.max(4, (height - rows * CELL) / 2);
        Canvas c = new Canvas(g);
        Chassis.panel(c, x0 - 6, y0 - 6, cols * CELL + 12, rows * CELL + 12, Theme.ACCENT_LAB, null, false, 0f, 10f);
        for (int i = 0; i < items.size(); i++) {
            int x = x0 + (i % cols) * CELL;
            int y = y0 + (i / cols) * CELL;
            c.rect(x + 1, y + 1, CELL - 2, CELL - 2, i % 2 == 0 ? 0xFF8B8B8B : 0xFF7A7A7A);
        }
        c.flush();
        for (int i = 0; i < items.size(); i++) {
            int x = x0 + (i % cols) * CELL + 3;
            int y = y0 + (i / cols) * CELL + 3;
            g.pose().pushMatrix();
            g.pose().translate(x, y);
            g.pose().scale(2f, 2f);
            g.item(new ItemStack(items.get(i)), 0, 0);
            g.pose().popMatrix();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
