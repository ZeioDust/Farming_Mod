package com.cultivar.blockentity;

import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModParticles;
import com.cultivar.world.Amendment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class NutrientSpreaderBlockEntity extends InventoryBlockEntity implements TickingMachine {
    public static final int RADIUS = 4;
    public static final int INTERVAL = 100;

    public NutrientSpreaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUTRIENT_SPREADER.get(), pos, state, 1);
    }

    public ItemStack loaded() {
        return items.get(0);
    }

    public boolean load(ItemStack stack) {
        if (Amendment.of(stack) == null) {
            return false;
        }
        ItemStack rest = insert(stack, 0, 1);
        if (rest.getCount() == stack.getCount()) {
            return false;
        }
        stack.setCount(rest.getCount());
        markUpdated();
        return true;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % INTERVAL != 0) {
            return;
        }
        ItemStack stack = items.get(0);
        Amendment amendment = Amendment.of(stack);
        if (amendment == null) {
            return;
        }
        ManagedSoilBlockEntity target = neediest(level, pos, amendment);
        if (target == null) {
            return;
        }
        amendment.apply(target);
        stack.shrink(1);
        BlockPos t = target.getBlockPos();
        level.sendParticles(ModParticles.SOIL_CRUMB.get(), t.getX() + 0.5, t.getY() + 1.0, t.getZ() + 0.5, 6, 0.3, 0.05, 0.3, 0.02);
        level.playSound(null, t, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.4f, 1.1f);
        markUpdated();
    }

    private static @Nullable ManagedSoilBlockEntity neediest(ServerLevel level, BlockPos center, Amendment amendment) {
        ManagedSoilBlockEntity best = null;
        float bestNeed = 0.5f;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-RADIUS, -2, -RADIUS), center.offset(RADIUS, 1, RADIUS))) {
            if (level.getBlockEntity(p) instanceof ManagedSoilBlockEntity soil) {
                float need = amendment.need(soil);
                if (need > bestNeed) {
                    bestNeed = need;
                    best = soil;
                }
            }
        }
        return best;
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return Amendment.of(stack) != null;
    }
}
