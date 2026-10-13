package com.cultivar.blockentity;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.data.Catalog;
import com.cultivar.market.Contract;
import com.cultivar.market.ContractGenerator;
import com.cultivar.menu.ContractBoardMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ContractBoardBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine {
    public static final int SLOT_DELIVERY = 0;
    public static final int SLOT_PAYMENT = 1;
    public static final int SLOTS = 2;
    public static final int CONTRACTS = 3;

    public record Offer(Supplier<? extends Item> item, int count, int price, int tier) {
        public ItemStack stack() {
            return new ItemStack(item.get(), count);
        }
    }

    public static final List<Offer> OFFERS = List.of(
            new Offer(() -> Items.PAPER, 8, 1, 0),
            new Offer(ModItems.SOY_OIL, 1, 2, 0),
            new Offer(ModItems.MUTAGEN_VIAL, 1, 3, 0),
            new Offer(ModItems.CANOPY_RESIN, 1, 6, 1),
            new Offer(ModItems.SEED_VAULT_DRAWER, 1, 8, 1),
            new Offer(ModItems.EDIT_REAGENT, 1, 12, 2),
            new Offer(ModItems.GENE_EDITOR_BLUEPRINT, 1, 40, 3));

    private final List<Optional<Contract>> contracts = new ArrayList<>(List.of(Optional.empty(), Optional.empty(), Optional.empty()));
    private int reputation;
    private long lastDay = -1;

    public ContractBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTRACT_BOARD.get(), pos, state, SLOTS);
    }

    public List<Optional<Contract>> contracts() {
        return contracts;
    }

    public void post(int index, Contract contract) {
        contracts.set(index, Optional.of(contract));
        markUpdated();
    }

    public int reputation() {
        return reputation;
    }

    public int tier() {
        return ContractGenerator.tier(reputation);
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        long day = level.getOverworldClockTime() / 24000L;
        if (lastDay < 0 || day > lastDay) {
            lastDay = day;
            refill(level);
        }
    }

    public void refill(ServerLevel level) {
        Catalog catalog = Catalog.get(level);
        boolean changed = false;
        for (int i = 0; i < CONTRACTS; i++) {
            if (contracts.get(i).isEmpty()) {
                contracts.set(i, Optional.of(ContractGenerator.generate(catalog, level.getRandom(), tier())));
                changed = true;
            }
        }
        if (changed) {
            markUpdated();
        }
    }

    public boolean deliver(ServerPlayer player, int index) {
        if (index < 0 || index >= CONTRACTS || contracts.get(index).isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        Contract contract = contracts.get(index).get();
        ItemStack stack = items.get(SLOT_DELIVERY);
        Catalog catalog = Catalog.get(serverLevel);
        if (stack.isEmpty() || !contract.accepts(stack, catalog)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_CONTRACT_REJECTED));
            return false;
        }
        int taken = Math.min(stack.getCount(), contract.remaining());
        stack.shrink(taken);
        contract = contract.withProgress(contract.progress() + taken);
        if (contract.complete()) {
            reward(player, contract);
            contracts.set(index, Optional.empty());
            reputation++;
            CultivarTrigger.fire(player, CultivarTrigger.FIRST_CONTRACT);
            serverLevel.playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6f, 1.4f);
        } else {
            contracts.set(index, Optional.of(contract));
            serverLevel.playSound(null, getBlockPos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        markUpdated();
        return true;
    }

    private void reward(ServerPlayer player, Contract contract) {
        give(player, new ItemStack(Items.EMERALD, contract.reward()));
        contract.bonus().ifPresent(template -> give(player, template.create()));
        player.sendSystemMessage(Texts.tr(Texts.MSG_CONTRACT_DONE, contract.reward()));
    }

    public boolean discard(ServerPlayer player, int index) {
        if (index < 0 || index >= CONTRACTS || contracts.get(index).isEmpty() || !pay(1)) {
            return false;
        }
        contracts.set(index, Optional.empty());
        markUpdated();
        return true;
    }

    public boolean buy(ServerPlayer player, int index) {
        if (index < 0 || index >= OFFERS.size()) {
            return false;
        }
        Offer offer = OFFERS.get(index);
        if (tier() < offer.tier() || !pay(offer.price())) {
            return false;
        }
        give(player, offer.stack());
        if (level != null) {
            level.playSound(null, getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5f, 1.6f);
        }
        markUpdated();
        return true;
    }

    private boolean pay(int emeralds) {
        ItemStack payment = items.get(SLOT_PAYMENT);
        if (!payment.is(Items.EMERALD) || payment.getCount() < emeralds) {
            return false;
        }
        payment.shrink(emeralds);
        return true;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_CONTRACTS);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContractBoardMenu(id, inventory, this, getBlockPos());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < CONTRACTS; i++) {
            int slot = i;
            contracts.get(i).ifPresent(c -> output.store("contract_" + slot, Contract.CODEC, c));
        }
        output.putInt("reputation", reputation);
        output.putLong("last_day", lastDay);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < CONTRACTS; i++) {
            contracts.set(i, input.read("contract_" + i, Contract.CODEC));
        }
        reputation = input.getIntOr("reputation", 0);
        lastDay = input.getLongOr("last_day", -1L);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot == SLOT_PAYMENT ? stack.is(Items.EMERALD) : slot == SLOT_DELIVERY;
    }
}
