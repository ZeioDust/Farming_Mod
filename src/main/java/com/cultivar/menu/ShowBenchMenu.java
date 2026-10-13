package com.cultivar.menu;

import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Items;

public class ShowBenchMenu extends MachineMenu {
    public static final int WIDTH = 248;
    public static final int HEIGHT = 214;
    public static final int INV_Y = 132;
    private final BlockPos pos;

    public ShowBenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(ShowBenchBlockEntity.SLOTS), buf.readBlockPos());
    }

    public ShowBenchMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(ModMenus.SHOW_BENCH.get(), id, inventory, container, new SimpleContainerData(0), ShowBenchBlockEntity.SLOTS);
        this.pos = pos;
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, ShowBenchBlockEntity.SLOT_PACKET, 16, 36, s -> s.getItem() instanceof SeedPacketItem));
        addSlot(new FilteredSlot(container, ShowBenchBlockEntity.SLOT_PAPER, 16, 80, s -> s.is(Items.PAPER)));
        addSlot(new OutputSlot(container, ShowBenchBlockEntity.SLOT_OUTPUT, 214, 36));
        addSlot(new OutputSlot(container, ShowBenchBlockEntity.SLOT_RIBBON, 214, 80));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == 0 && container instanceof ShowBenchBlockEntity bench && player instanceof ServerPlayer serverPlayer && bench.judge(serverPlayer);
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
