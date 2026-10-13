package com.cultivar.block;

import com.cultivar.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WildVineBlock extends Block {
    public static final MapCodec<WildVineBlock> CODEC = simpleCodec(WildVineBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public WildVineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<WildVineBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(400) == 0) {
            level.playLocalSound(pos, ModSounds.CANOPY_RUSTLE.get(), SoundSource.AMBIENT, 0.4f, 0.8f + random.nextFloat() * 0.4f, false);
        }
    }
}
