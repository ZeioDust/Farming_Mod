package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.block.SeedVaultDrawerBlock;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.item.Planting;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.item.WildSeedItem;
import com.cultivar.menu.SeedBankLine;
import com.cultivar.menu.SeedBankMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import com.cultivar.world.HarvestLogic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.cultivar.menu.ComputedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SeedBankBlockEntity extends InventoryBlockEntity implements MenuProvider {
    public static final int SLOT_DEPOSIT = 0;
    public static final int SLOT_PAPER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOTS = 3;
    public static final int DATA = 2;
    public static final int BASE_LINES = 8;
    public static final int LINES_PER_DRAWER = 8;
    public static final int MAX_DRAWERS = 64;

    private final List<SeedBankLine> lines = new ArrayList<>();
    private int version;
    private final ContainerData data = new ComputedData(DATA, index -> index == 0 ? capacity() : lines.size());

    public SeedBankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEED_BANK.get(), pos, state, SLOTS);
    }

    public static boolean isStorable(ItemStack stack) {
        return stack.getItem() instanceof SeedPacketItem || stack.getItem() instanceof WildSeedItem;
    }

    public List<SeedBankLine> lines() {
        return lines;
    }

    public int version() {
        return version;
    }

    public int drawerCount() {
        if (level == null) {
            return 0;
        }
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(worldPosition);
        seen.add(worldPosition);
        int drawers = 0;
        while (!queue.isEmpty() && drawers < MAX_DRAWERS) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (seen.add(next) && level.getBlockState(next).getBlock() instanceof SeedVaultDrawerBlock) {
                    drawers++;
                    queue.add(next);
                }
            }
        }
        return drawers;
    }

    public int capacity() {
        return Math.min(CultivarConfig.SEED_BANK_MAX_LINES.get(), BASE_LINES + LINES_PER_DRAWER * drawerCount());
    }

    public boolean deposit(Player player) {
        ItemStack stack = items.get(SLOT_DEPOSIT);
        if (!isStorable(stack) || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (lines.size() >= capacity()) {
            player.sendSystemMessage(Texts.tr(Texts.MSG_BANK_FULL, capacity()));
            return false;
        }
        boolean wild = stack.getItem() instanceof WildSeedItem;
        CropSpecies species = wild ? WildSeedItem.speciesOf(stack) : ((SeedPacketItem) stack.getItem()).species();
        Planting.assignNaturalGenome(stack, species, serverLevel);
        Genome genome = stack.get(ModDataComponents.GENOME.get());
        if (genome == null) {
            return false;
        }
        String name = stack.has(DataComponents.CUSTOM_NAME)
                ? stack.getHoverName().getString()
                : species.displayName() + " line " + (lines.size() + 1);
        lines.add(new SeedBankLine(name.length() > 64 ? name.substring(0, 64) : name, species.key(), genome,
                stack.getOrDefault(ModDataComponents.LINEAGE.get(), 0L), serverLevel.getGameTime(), wild));
        stack.shrink(1);
        version++;
        serverLevel.playSound(null, worldPosition, ModSounds.BANK_DRAWER.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
        if (player instanceof ServerPlayer serverPlayer) {
            CultivarTrigger.fire(serverPlayer, CultivarTrigger.BANKED);
        }
        markUpdated();
        return true;
    }

    public boolean withdraw(SeedBankLine requested) {
        int index = lines.indexOf(requested);
        if (index < 0 || !items.get(SLOT_OUTPUT).isEmpty() || level == null) {
            return false;
        }
        int cost = CultivarConfig.SEED_BANK_WITHDRAW_COST.get();
        ItemStack paper = items.get(SLOT_PAPER);
        if (cost > 0 && (!paper.is(Items.PAPER) || paper.getCount() < cost)) {
            return false;
        }
        SeedBankLine line = lines.get(index);
        CropSpecies species = CropSpecies.byKey(line.species());
        if (species == null) {
            return false;
        }
        ItemStack copy = HarvestLogic.seedStack(species, line.genome(), line.lineage(), line.wild());
        copy.set(DataComponents.CUSTOM_NAME, Component.literal(line.name()));
        items.set(SLOT_OUTPUT, copy);
        if (cost > 0) {
            paper.shrink(cost);
        }
        level.playSound(null, worldPosition, ModSounds.BANK_DRAWER.get(), SoundSource.BLOCKS, 0.8f, 1.2f);
        markUpdated();
        return true;
    }

    public boolean delete(SeedBankLine requested) {
        if (!lines.remove(requested)) {
            return false;
        }
        version++;
        markUpdated();
        return true;
    }

    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!lines.isEmpty()) {
            components.set(ModDataComponents.SEED_BANK_LINES.get(), List.copyOf(lines));
        }
    }

    @Override
    protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter components) {
        super.applyImplicitComponents(components);
        List<SeedBankLine> stored = components.get(ModDataComponents.SEED_BANK_LINES.get());
        if (stored != null) {
            lines.clear();
            lines.addAll(stored);
            version++;
        }
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_SEED_BANK);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        version++;
        return new SeedBankMenu(id, inventory, this, data, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("lines", SeedBankLine.CODEC.listOf(), List.copyOf(lines));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        lines.clear();
        input.read("lines", SeedBankLine.CODEC.listOf()).ifPresent(lines::addAll);
        version++;
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot == SLOT_DEPOSIT ? isStorable(stack) : slot == SLOT_PAPER && stack.is(Items.PAPER);
    }
}
