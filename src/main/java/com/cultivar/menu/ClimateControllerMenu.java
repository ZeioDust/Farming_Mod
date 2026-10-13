package com.cultivar.menu;

import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ClimateControllerMenu extends AbstractContainerMenu {
    public static final int BUTTON_COLDER = 0;
    public static final int BUTTON_WARMER = 1;
    public static final int BUTTON_RESCAN = 2;

    private final ContainerData data;
    private final @Nullable ClimateControllerBlockEntity controller;
    private final BlockPos pos;

    public ClimateControllerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainerData(ClimateControllerBlockEntity.DATA_COUNT), null, buf.readBlockPos());
    }

    public ClimateControllerMenu(int id, Inventory inventory, ContainerData data, @Nullable ClimateControllerBlockEntity controller, BlockPos pos) {
        super(ModMenus.CLIMATE_CONTROLLER.get(), id);
        this.data = data;
        this.controller = controller;
        this.pos = pos;
        addDataSlots(data);
    }

    public int data(int index) {
        return data.get(index);
    }

    public BlockPos pos() {
        return pos;
    }

    public boolean sealed() {
        return data.get(ClimateControllerBlockEntity.D_SEALED) != 0;
    }

    public int volume() {
        return (data.get(ClimateControllerBlockEntity.D_VOLUME_HIGH) << 15) | data.get(ClimateControllerBlockEntity.D_VOLUME_LOW);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (controller == null) {
            return false;
        }
        switch (id) {
            case BUTTON_COLDER -> controller.adjustSetpoint(-0.1f);
            case BUTTON_WARMER -> controller.adjustSetpoint(0.1f);
            case BUTTON_RESCAN -> controller.markDirty();
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return controller == null || (!controller.isRemoved() && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0);
    }
}
