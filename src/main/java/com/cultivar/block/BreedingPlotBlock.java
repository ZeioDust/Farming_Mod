package com.cultivar.block;

import com.cultivar.blockentity.BreedingPlotBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BreedingPlotBlock extends ManagedSoilBlock {
    public static final MapCodec<BreedingPlotBlock> CODEC = simpleCodec(BreedingPlotBlock::new);

    public BreedingPlotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BreedingPlotBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BreedingPlotBlockEntity(pos, state);
    }
}
