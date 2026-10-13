package com.cultivar.menu;

import com.cultivar.blockentity.GenomeLabBlockEntity;
import com.cultivar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Items;

public class GenomeLabMenu extends MachineMenu {
    public static final int WIDTH = 232;
    public static final int HEIGHT = 206;
    public static final int INV_Y = 124;
    public GenomeLabMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(GenomeLabBlockEntity.SLOTS), new SimpleContainerData(GenomeLabBlockEntity.DATA));
    }

    public GenomeLabMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.GENOME_LAB.get(), id, inventory, container, data, GenomeLabBlockEntity.SLOTS);
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, GenomeLabBlockEntity.SLOT_SAMPLE, 14, 42, GenomeLabBlockEntity::isSample));
        addSlot(new FilteredSlot(container, GenomeLabBlockEntity.SLOT_PAPER, 14, 82, s -> s.is(Items.PAPER)));
        addSlot(new OutputSlot(container, GenomeLabBlockEntity.SLOT_OUTPUT, 202, 62));
    }

    @Override
    protected int inventoryX() {
        return (WIDTH - 162) / 2;
    }

    @Override
    protected int inventoryY() {
        return INV_Y;
    }

    public float progress() {
        return data(0) / 1000f;
    }
}
