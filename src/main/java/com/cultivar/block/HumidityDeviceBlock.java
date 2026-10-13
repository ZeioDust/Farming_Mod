package com.cultivar.block;

import com.cultivar.entity.MistCloudEntity;
import com.cultivar.registry.ModEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

public class HumidityDeviceBlock extends Block {
    public static final MapCodec<HumidityDeviceBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.BOOL.fieldOf("humidifies").forGetter(HumidityDeviceBlock::humidifies),
            propertiesCodec()
    ).apply(i, HumidityDeviceBlock::new));

    private final boolean humidifies;

    public HumidityDeviceBlock(boolean humidifies, Properties properties) {
        super(properties);
        this.humidifies = humidifies;
    }

    @Override
    protected MapCodec<HumidityDeviceBlock> codec() {
        return CODEC;
    }

    public boolean humidifies() {
        return humidifies;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!humidifies || !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        MistCloudEntity mist = ModEntities.MIST_CLOUD.get().create(level, EntitySpawnReason.TRIGGERED);
        if (mist != null) {
            mist.setPos(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
            mist.drift(new Vec3((random.nextDouble() - 0.5) * 0.02, 0.006, (random.nextDouble() - 0.5) * 0.02));
            level.addFreshEntity(mist);
        }
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
