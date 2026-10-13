package com.cultivar.menu;

import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class GeneEditorMenu extends MachineMenu {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 222;
    public static final int INV_Y = 140;

    public GeneEditorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(GeneEditorBlockEntity.SLOTS), new SimpleContainerData(GeneEditorBlockEntity.DATA));
    }

    public GeneEditorMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.GENE_EDITOR.get(), id, inventory, container, data, GeneEditorBlockEntity.SLOTS);
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, GeneEditorBlockEntity.SLOT_PACKET, 16, 38, s -> s.getItem() instanceof SeedPacketItem));
        addSlot(new FilteredSlot(container, GeneEditorBlockEntity.SLOT_REAGENT, 16, 84, s -> s.is(ModItems.EDIT_REAGENT.get())));
        addSlot(new OutputSlot(container, GeneEditorBlockEntity.SLOT_OUTPUT, 222, 60));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return container instanceof GeneEditorBlockEntity editor && player instanceof ServerPlayer serverPlayer && editor.button(serverPlayer, id);
    }

    public int gene() {
        return data(0);
    }

    public int copy() {
        return data(1);
    }

    public int alleleCode() {
        return data(2);
    }

    public float progress() {
        return data(3) / 1000f;
    }

    public boolean running() {
        return data(4) != 0;
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
