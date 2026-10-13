package com.cultivar.menu;

import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class MutagenChamberMenu extends MachineMenu {
    public static final int WIDTH = 232;
    public static final int HEIGHT = 212;
    public static final int INV_Y = 130;
    public MutagenChamberMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(MutagenChamberBlockEntity.SLOTS), new SimpleContainerData(MutagenChamberBlockEntity.DATA));
    }

    public MutagenChamberMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.MUTAGEN_CHAMBER.get(), id, inventory, container, data, MutagenChamberBlockEntity.SLOTS);
    }

    @Override
    protected void addMachineSlots() {
        for (int i = 0; i < 4; i++) {
            addSlot(new FilteredSlot(container, MutagenChamberBlockEntity.FIRST_INPUT + i, 16 + (i % 2) * 18, 28 + (i / 2) * 18,
                    MutagenChamberBlockEntity::isSeed));
        }
        addSlot(new FilteredSlot(container, MutagenChamberBlockEntity.SLOT_VIAL, 25, 84, s -> s.is(ModItems.MUTAGEN_VIAL.get())));
        for (int i = 0; i < 4; i++) {
            addSlot(new OutputSlot(container, MutagenChamberBlockEntity.FIRST_OUTPUT + i, 182 + (i % 2) * 18, 28 + (i / 2) * 18));
        }
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

    public int vialUses() {
        return data(1);
    }
}
