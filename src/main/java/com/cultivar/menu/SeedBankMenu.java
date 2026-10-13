package com.cultivar.menu;

import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.network.SeedBankLinesPayload;
import com.cultivar.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class SeedBankMenu extends MachineMenu {
    public static final int WITHDRAW_BASE = 1000;
    public static final int DELETE_BASE = 3000;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 214;
    public static final int INV_Y = 132;

    private final @Nullable SeedBankBlockEntity bank;
    private final Player player;
    private List<SeedBankLine> clientLines = new ArrayList<>();
    private int clientCapacity;
    private int sentVersion = -1;
    private List<SeedBankLine> sentLines = List.of();

    public SeedBankMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(SeedBankBlockEntity.SLOTS), new SimpleContainerData(SeedBankBlockEntity.DATA), null);
    }

    public SeedBankMenu(int id, Inventory inventory, Container container, ContainerData data, @Nullable SeedBankBlockEntity bank) {
        super(ModMenus.SEED_BANK.get(), id, inventory, container, data, SeedBankBlockEntity.SLOTS);
        this.bank = bank;
        this.player = inventory.player;
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new FilteredSlot(container, SeedBankBlockEntity.SLOT_DEPOSIT, 14, 30, SeedBankBlockEntity::isStorable));
        addSlot(new FilteredSlot(container, SeedBankBlockEntity.SLOT_PAPER, 14, 64, s -> s.is(Items.PAPER)));
        addSlot(new OutputSlot(container, SeedBankBlockEntity.SLOT_OUTPUT, 14, 98));
    }

    @Override
    protected int inventoryX() {
        return (WIDTH - 162) / 2;
    }

    @Override
    protected int inventoryY() {
        return INV_Y;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (bank != null && player instanceof ServerPlayer serverPlayer && bank.version() != sentVersion) {
            sentVersion = bank.version();
            sentLines = List.copyOf(bank.lines());
            PacketDistributor.sendToPlayer(serverPlayer, new SeedBankLinesPayload(containerId, sentLines, bank.capacity()));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (bank == null) {
            return false;
        }
        if (id == 0) {
            return bank.deposit(player);
        }
        if (id >= DELETE_BASE) {
            SeedBankLine line = seen(id - DELETE_BASE);
            return line != null && bank.delete(line);
        }
        if (id >= WITHDRAW_BASE) {
            SeedBankLine line = seen(id - WITHDRAW_BASE);
            return line != null && bank.withdraw(line);
        }
        return false;
    }

    private @Nullable SeedBankLine seen(int index) {
        return index >= 0 && index < sentLines.size() ? sentLines.get(index) : null;
    }

    public void setClientLines(List<SeedBankLine> lines, int capacity) {
        this.clientLines = new ArrayList<>(lines);
        this.clientCapacity = capacity;
    }

    public List<SeedBankLine> clientLines() {
        return clientLines;
    }

    public int clientCapacity() {
        return clientCapacity;
    }
}
