package com.cultivar.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class SeedVaultDrawerBlock extends Block {
    public static final MapCodec<SeedVaultDrawerBlock> CODEC = simpleCodec(SeedVaultDrawerBlock::new);

    public SeedVaultDrawerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SeedVaultDrawerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(Facing.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return Facing.placed(defaultBlockState(), context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return Facing.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return Facing.mirror(state, mirror);
    }
}
