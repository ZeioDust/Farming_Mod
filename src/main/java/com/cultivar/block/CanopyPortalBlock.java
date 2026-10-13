package com.cultivar.block;

import com.cultivar.CultivarConfig;
import com.cultivar.registry.ModParticles;
import com.cultivar.world.dimension.CanopyTravel;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CanopyPortalBlock extends Block {
    public static final MapCodec<CanopyPortalBlock> CODEC = simpleCodec(CanopyPortalBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public CanopyPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CanopyPortalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
        if (level instanceof ServerLevel serverLevel && CultivarConfig.CANOPY_ENABLED.get() && entity.canUsePortal(false)) {
            CanopyTravel.travel(serverLevel, entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            level.addParticle(ModParticles.POLLEN_MOTE.get(), pos.getX() + random.nextDouble(), pos.getY() + 0.8,
                    pos.getZ() + random.nextDouble(), 0, 0.04, 0);
        }
    }
}
