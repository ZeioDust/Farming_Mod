package com.cultivar.compat.jei;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class MachineCategory extends AbstractRecipeCategory<MachineRecipe> {
    public static final int WIDTH = 176;
    private static final int SLOT = 18;
    private static final int LINE = 12;
    private final int columns;
    private final int rows;
    private final int outputRows;

    public MachineCategory(IGuiHelper gui, IRecipeType<MachineRecipe> type, Component title, ItemLike icon, int columns, int rows, int outputRows,
                           int noteLines) {
        super(type, title, gui.createDrawableItemLike(icon), WIDTH, Math.max(rows, outputRows) * SLOT + 4 + noteLines * LINE);
        this.columns = columns;
        this.rows = rows;
        this.outputRows = outputRows;
    }

    private int gridHeight() {
        return Math.max(rows, outputRows) * SLOT;
    }

    private int arrowX() {
        return columns * SLOT + 8;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        int top = (gridHeight() - rows * SLOT) / 2;
        for (int i = 0; i < recipe.inputs().size(); i++) {
            builder.addInputSlot(1 + (i % columns) * SLOT, 1 + top + (i / columns) * SLOT).setStandardSlotBackground()
                    .addItemStacks(recipe.inputs().get(i));
        }
        int outTop = (gridHeight() - outputRows * SLOT) / 2;
        int outX = arrowX() + 32;
        for (int i = 0; i < recipe.outputs().size(); i++) {
            int col = i / outputRows;
            builder.addOutputSlot(outX + col * SLOT, 1 + outTop + (i % outputRows) * SLOT).setStandardSlotBackground()
                    .addItemStacks(recipe.outputs().get(i));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(arrowX(), (gridHeight() - 17) / 2);
        if (!recipe.notes().isEmpty()) {
            List<FormattedText> lines = new ArrayList<>(recipe.notes());
            builder.addText(lines, WIDTH, getHeight() - gridHeight() - 4).setPosition(0, gridHeight() + 4).setColor(0xFF404040);
        }
    }

    static List<ItemStack> counted(List<ItemStack> stacks, int count) {
        List<ItemStack> out = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            out.add(stack.copyWithCount(count));
        }
        return out;
    }
}
