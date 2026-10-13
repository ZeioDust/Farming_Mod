package com.cultivar.menu;

import com.cultivar.blockentity.ContractBoardBlockEntity;
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
import org.jspecify.annotations.Nullable;

public class ContractBoardMenu extends MachineMenu {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 222;
    public static final int INV_Y = 140;
    public static final int DELIVER = 0;
    public static final int DISCARD = 10;
    public static final int BUY = 20;
    private final BlockPos pos;
    private final @Nullable ContractBoardBlockEntity board;

    public ContractBoardMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(ContractBoardBlockEntity.SLOTS), buf.readBlockPos(), null);
    }

    public ContractBoardMenu(int id, Inventory inventory, ContractBoardBlockEntity board, BlockPos pos) {
        this(id, inventory, board, pos, board);
    }

    private ContractBoardMenu(int id, Inventory inventory, Container container, BlockPos pos, @Nullable ContractBoardBlockEntity board) {
        super(ModMenus.CONTRACT_BOARD.get(), id, inventory, container, new SimpleContainerData(0), ContractBoardBlockEntity.SLOTS);
        this.pos = pos;
        this.board = board;
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, ContractBoardBlockEntity.SLOT_DELIVERY, 14, 44, s -> true));
        addSlot(new FilteredSlot(container, ContractBoardBlockEntity.SLOT_PAYMENT, 14, 98, s -> s.is(Items.EMERALD)));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (board == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id >= BUY) {
            return board.buy(serverPlayer, id - BUY);
        }
        if (id >= DISCARD) {
            return board.discard(serverPlayer, id - DISCARD);
        }
        return board.deliver(serverPlayer, id - DELIVER);
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
