package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModTags;
import com.cultivar.world.InducedMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ThermalShockUnitBlockEntity extends InventoryBlockEntity implements TickingMachine, ProgressMachine {
    public static final int INPUTS = 4;
    public static final int SLOTS = 8;
    public static final int TICKS_PER_SEED = 160;
    public static final int COOLANT_PER_ITEM = 2;

    private int progress;
    private int coolant;

    public ThermalShockUnitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THERMAL_SHOCK_UNIT.get(), pos, state, SLOTS);
    }

    public int coolant() {
        return coolant;
    }

    public int queued() {
        int n = 0;
        for (int i = 0; i < INPUTS; i++) {
            n += items.get(i).getCount();
        }
        return n;
    }

    public int finished() {
        int n = 0;
        for (int i = INPUTS; i < SLOTS; i++) {
            n += items.get(i).getCount();
        }
        return n;
    }

    @Override
    public float progressFraction() {
        return progress / (float) TICKS_PER_SEED;
    }

    public boolean load(ItemStack stack) {
        if (stack.is(ModTags.Items.COOLANTS)) {
            coolant += COOLANT_PER_ITEM;
            stack.shrink(1);
            markUpdated();
            return true;
        }
        if (InducedMutation.isTreatable(stack)) {
            ItemStack rest = insert(stack.copyWithCount(1), 0, INPUTS);
            if (rest.isEmpty()) {
                stack.shrink(1);
                markUpdated();
                return true;
            }
        }
        return false;
    }

    public ItemStack takeOne() {
        for (int i = INPUTS; i < SLOTS; i++) {
            if (!items.get(i).isEmpty()) {
                ItemStack out = items.get(i).copy();
                items.set(i, ItemStack.EMPTY);
                markUpdated();
                return out;
            }
        }
        for (int i = 0; i < INPUTS; i++) {
            if (!items.get(i).isEmpty()) {
                ItemStack out = items.get(i).copy();
                items.set(i, ItemStack.EMPTY);
                markUpdated();
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        int input = -1;
        for (int i = 0; i < INPUTS; i++) {
            if (InducedMutation.isTreatable(items.get(i))) {
                input = i;
                break;
            }
        }
        boolean room = false;
        for (int i = INPUTS; i < SLOTS; i++) {
            room |= items.get(i).isEmpty();
        }
        if (input < 0 || coolant <= 0 || !room) {
            if (progress != 0) {
                progress = 0;
                markUpdated();
            }
            return;
        }
        progress++;
        if (progress % 20 == 0) {
            markUpdated();
            level.sendParticles(ModParticles.MIST_WISP.get(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 2, 0.2, 0.05, 0.2, 0.0);
        }
        if (progress < TICKS_PER_SEED) {
            return;
        }
        progress = 0;
        coolant--;
        ItemStack seed = items.get(input);
        InducedMutation.Result result = InducedMutation.treat(level, seed,
                CultivarConfig.THERMAL_SHOCK_SUCCESS_CHANCE.get(), CultivarConfig.THERMAL_SHOCK_DESTROY_CHANCE.get());
        seed.shrink(1);
        if (!result.stack().isEmpty()) {
            insert(result.stack(), INPUTS, SLOTS);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.4f);
        markUpdated();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
        output.putInt("coolant", coolant);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("progress", 0);
        coolant = input.getIntOr("coolant", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot < INPUTS && InducedMutation.isTreatable(stack);
    }
}
