package com.cultivar.blockentity;

import com.cultivar.block.BurnPanBlock;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModTags;
import com.cultivar.world.LevelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BurnPanBlockEntity extends InventoryBlockEntity implements TickingMachine {
    public static final int RADIUS = 3;
    public static final int INTERVAL = 40;
    public static final int BURN_PER_FUEL = 3;

    private int burns;

    public BurnPanBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BURN_PAN.get(), pos, state, 9);
    }

    public int burnsLeft() {
        return burns;
    }

    public boolean addFuel(ItemStack stack) {
        if (!stack.is(ModTags.Items.BURN_FUEL)) {
            return false;
        }
        burns += BURN_PER_FUEL;
        stack.shrink(1);
        markUpdated();
        return true;
    }

    public ItemStack takeSample() {
        for (int i = 0; i < items.size(); i++) {
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
        boolean lit = burns > 0;
        if (state.getValue(BurnPanBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(BurnPanBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
        if (!lit || (level.getGameTime() + pos.asLong()) % INTERVAL != 0) {
            return;
        }
        LevelState index = LevelState.get(level);
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -1, -RADIUS), pos.offset(RADIUS, 1, RADIUS))) {
            if (!index.hasCrop(p.asLong()) || !(level.getBlockEntity(p) instanceof CropBlockEntity crop) || !crop.isBlighted()) {
                continue;
            }
            Identifier strain = crop.strain();
            BlockPos target = p.immutable();
            level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.FLAME, target.getX() + 0.5, target.getY() + 0.3, target.getZ() + 0.5, 12, 0.25, 0.25, 0.25, 0.01);
            level.sendParticles(ParticleTypes.SMOKE, target.getX() + 0.5, target.getY() + 0.6, target.getZ() + 0.5, 8, 0.2, 0.3, 0.2, 0.01);
            level.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 0.8f);
            if (strain != null) {
                ItemStack sample = new ItemStack(ModItems.BLIGHT_SAMPLE.get());
                sample.set(ModDataComponents.STRAIN.get(), strain);
                insert(sample, 0, items.size());
            }
            burns--;
            markUpdated();
            return;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("burns", burns);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burns = input.getIntOr("burns", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return false;
    }
}
