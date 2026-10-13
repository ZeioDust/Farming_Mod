package com.cultivar.menu;

import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class CompostBinMenu extends MachineMenu {
    public static final int WIDTH = 212;
    public static final int HEIGHT = 206;
    public static final int INV_Y = 124;
    public CompostBinMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(CompostBinBlockEntity.SLOTS), new SimpleContainerData(CompostBinBlockEntity.DATA));
    }

    public CompostBinMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.COMPOST_BIN.get(), id, inventory, container, data, CompostBinBlockEntity.SLOTS);
    }

    @Override
    protected void addMachineSlots() {
        for (int i = 0; i < 3; i++) {
            addSlot(new FilteredSlot(container, i, 16, 30 + i * 22, s -> true));
        }
        addSlot(new OutputSlot(container, CompostBinBlockEntity.SLOT_OUTPUT, 178, 52));
    }

    @Override
    protected int inventoryX() {
        return (WIDTH - 162) / 2;
    }

    @Override
    protected int inventoryY() {
        return INV_Y;
    }

    public int charge() {
        return data(0);
    }

    public float progress() {
        return data(1) / 1000f;
    }
}
