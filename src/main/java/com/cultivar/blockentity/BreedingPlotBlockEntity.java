package com.cultivar.blockentity;

import com.cultivar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BreedingPlotBlockEntity extends ManagedSoilBlockEntity {
    public static final int EXTRA_SEEDS = 2;

    public BreedingPlotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREEDING_PLOT.get(), pos, state);
    }
}
