package com.cultivar.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GreenhousePaneBlock extends TransparentBlock {
    public static final MapCodec<GreenhousePaneBlock> CODEC = simpleCodec(GreenhousePaneBlock::new);

    public GreenhousePaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GreenhousePaneBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }
}
