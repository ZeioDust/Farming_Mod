package com.cultivar.menu;

import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.market.MarketData;
import com.cultivar.market.MarketValues;
import com.cultivar.network.MarketQuotesPayload;
import com.cultivar.registry.ModMenus;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ProduceExchangeMenu extends MachineMenu {
    public static final int WIDTH = 248;
    public static final int HEIGHT = 214;
    public static final int INV_Y = 132;
    private final BlockPos pos;
    private final Player player;
    private int sinceQuote = -1;
    private int lastCrateCount = -1;

    public ProduceExchangeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(ProduceExchangeBlockEntity.SLOTS), buf.readBlockPos());
    }

    public ProduceExchangeMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(ModMenus.PRODUCE_EXCHANGE.get(), id, inventory, container, new SimpleContainerData(0), ProduceExchangeBlockEntity.SLOTS);
        this.pos = pos;
        this.player = inventory.player;
    }

    public BlockPos pos() {
        return pos;
    }

    private int crateCount() {
        int n = 0;
        for (int i = 0; i < ProduceExchangeBlockEntity.CRATE_SLOTS; i++) {
            n += container.getItem(i).getCount();
        }
        return n;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int count = crateCount();
        if (lastCrateCount >= 0 && count > lastCrateCount && container instanceof ProduceExchangeBlockEntity exchange) {
            exchange.addSeller(serverPlayer.getUUID());
        }
        lastCrateCount = count;
        if (sinceQuote < 0 || ++sinceQuote >= 200) {
            sinceQuote = 0;
            MarketData market = MarketData.get(serverPlayer.level().getServer());
            Map<Identifier, Float> demand = new HashMap<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (MarketValues.base(new ItemStack(item)) > 0) {
                    Identifier id = BuiltInRegistries.ITEM.getKey(item);
                    demand.put(id, market.demand(id));
                }
            }
            if (serverPlayer.connection.hasChannel(MarketQuotesPayload.TYPE)) {
                PacketDistributor.sendToPlayer(serverPlayer, new MarketQuotesPayload(demand));
            }
        }
    }

    @Override
    protected void addMachineSlots() {
        for (int i = 0; i < ProduceExchangeBlockEntity.CRATE_SLOTS; i++) {
            addSlot(new FilteredSlot(container, i, 14 + (i % 3) * 18, 30 + (i / 3) * 18, ProduceExchangeBlockEntity::sellable));
        }
        addSlot(new OutputSlot(container, ProduceExchangeBlockEntity.SLOT_OUTPUT, 32, 100));
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
