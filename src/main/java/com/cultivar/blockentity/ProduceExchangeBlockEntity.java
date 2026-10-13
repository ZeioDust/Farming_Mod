package com.cultivar.blockentity;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.market.MarketData;
import com.cultivar.market.MarketValues;
import com.cultivar.menu.ProduceExchangeMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.util.Texts;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ProduceExchangeBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine {
    public static final int CRATE_SLOTS = 9;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOTS = 10;
    private float credit;
    private long lastDay = -1;
    private int lastSaleItems;
    private float lastSaleValue;
    private final Set<UUID> sellers = new LinkedHashSet<>();

    public ProduceExchangeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRODUCE_EXCHANGE.get(), pos, state, SLOTS);
    }

    public float credit() {
        return credit;
    }

    public int lastSaleItems() {
        return lastSaleItems;
    }

    public float lastSaleValue() {
        return lastSaleValue;
    }

    public void addSeller(UUID player) {
        if (sellers.add(player)) {
            setChanged();
        }
    }

    public static boolean sellable(ItemStack stack) {
        return MarketValues.base(stack) > 0;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        long day = level.getOverworldClockTime() / 24000L;
        if (lastDay < 0) {
            lastDay = day;
            setChanged();
        }
        if (day > lastDay) {
            lastDay = day;
            sell(level, pos);
        }
        payOut();
    }

    public void sell(ServerLevel level, BlockPos pos) {
        MarketData market = MarketData.get(level.getServer());
        int sold = 0;
        float value = 0;
        for (int i = 0; i < CRATE_SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && sellable(stack)) {
                value += market.sell(stack);
                sold += stack.getCount();
                items.set(i, ItemStack.EMPTY);
            }
        }
        if (sold > 0) {
            credit += value;
            lastSaleItems = sold;
            lastSaleValue = value;
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS, 0.6f, 1.2f);
            for (UUID seller : sellers) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(seller);
                if (player != null) {
                    CultivarTrigger.fire(player, CultivarTrigger.FIRST_SALE);
                }
            }
            sellers.clear();
        }
        payOut();
        markUpdated();
    }

    private void payOut() {
        if (credit < 1f) {
            return;
        }
        ItemStack out = items.get(SLOT_OUTPUT);
        int room = out.isEmpty() ? 64 : out.is(Items.EMERALD) ? 64 - out.getCount() : 0;
        int whole = Math.min(room, (int) Math.floor(credit));
        if (whole <= 0) {
            return;
        }
        if (out.isEmpty()) {
            items.set(SLOT_OUTPUT, new ItemStack(Items.EMERALD, whole));
        } else {
            out.grow(whole);
        }
        credit -= whole;
        markUpdated();
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_EXCHANGE);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ProduceExchangeMenu(id, inventory, this, getBlockPos());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("credit", credit);
        output.putLong("last_day", lastDay);
        output.putInt("last_items", lastSaleItems);
        output.putFloat("last_value", lastSaleValue);
        output.store("sellers", UUIDUtil.CODEC.listOf(), List.copyOf(sellers));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        credit = input.getFloatOr("credit", 0f);
        lastDay = input.getLongOr("last_day", -1L);
        lastSaleItems = input.getIntOr("last_items", 0);
        lastSaleValue = input.getFloatOr("last_value", 0f);
        sellers.clear();
        sellers.addAll(input.read("sellers", UUIDUtil.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot < CRATE_SLOTS && sellable(stack);
    }
}
