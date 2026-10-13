package com.cultivar.menu;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class MachineMenu extends AbstractContainerMenu {
    public static final int PLAYER_INV_Y = 84;
    protected final Container container;
    protected final ContainerData data;
    protected final int machineSlots;

    protected MachineMenu(@Nullable MenuType<?> type, int id, Inventory inventory, Container container, ContainerData data, int machineSlots) {
        super(type, id);
        checkContainerSize(container, machineSlots);
        checkContainerDataCount(data, data.getCount());
        this.container = container;
        this.data = data;
        this.machineSlots = machineSlots;
        container.startOpen(inventory.player);
        addMachineSlots();
        addStandardInventorySlots(inventory, inventoryX(), inventoryY());
        addDataSlots(data);
    }

    protected abstract void addMachineSlots();

    protected int inventoryX() {
        return 8;
    }

    protected int inventoryY() {
        return PLAYER_INV_Y;
    }

    public int data(int index) {
        return data.get(index);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int i = 0; i < machineSlots && !stack.isEmpty(); i++) {
                Slot target = slots.get(i);
                if (target.mayPlace(stack) && moveItemStackTo(stack, i, i + 1, false)) {
                    moved = true;
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    public static class FilteredSlot extends Slot {
        private final Predicate<ItemStack> filter;

        public FilteredSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
            super(container, index, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return filter.test(stack);
        }
    }

    public static class OutputSlot extends Slot {
        public OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
