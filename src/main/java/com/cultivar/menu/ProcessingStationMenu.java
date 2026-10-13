package com.cultivar.menu;

import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class ProcessingStationMenu extends MachineMenu {
    public static final int WIDTH = 220;
    public static final int HEIGHT = 200;
    public static final int INV_Y = 118;
    private final BlockPos pos;

    public ProcessingStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(ProcessingStationBlockEntity.SLOTS), new SimpleContainerData(ProcessingStationBlockEntity.DATA),
                buf.readBlockPos());
    }

    public ProcessingStationMenu(int id, Inventory inventory, Container container, ContainerData data, BlockPos pos) {
        super(ModMenus.PROCESSING_STATION.get(), id, inventory, container, data, ProcessingStationBlockEntity.SLOTS);
        this.pos = pos;
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, ProcessingStationBlockEntity.SLOT_INPUT, 30, 48, s -> true));
        addSlot(new OutputSlot(container, ProcessingStationBlockEntity.SLOT_OUTPUT, 172, 48));
    }

    public float progress() {
        return data(0) / 1000f;
    }

    public int boostSeconds() {
        return data(1);
    }

    @Override
    protected int inventoryX() {
        return (WIDTH - 162) / 2;
    }

    @Override
    protected int inventoryY() {
        return INV_Y;
    }
}
