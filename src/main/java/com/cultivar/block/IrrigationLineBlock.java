package com.cultivar.block;

import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class IrrigationLineBlock extends Block {
    public static final MapCodec<IrrigationLineBlock> CODEC = simpleCodec(IrrigationLineBlock::new);
    public static final BooleanProperty FED = BooleanProperty.create("fed");
    public static final int RADIUS = 4;
    public static final int MAX_NETWORK = 64;
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 4, 11);

    public IrrigationLineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FED, false));
    }

    @Override
    protected MapCodec<IrrigationLineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            refresh(serverLevel, pos, state);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            refresh(serverLevel, pos, state);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refresh(level, pos, state);
    }

    private static void refresh(ServerLevel level, BlockPos pos, BlockState state) {
        boolean fed = networkTouchesWater(level, pos);
        if (state.getValue(FED) != fed) {
            level.setBlock(pos, state.setValue(FED, fed), Block.UPDATE_ALL);
        }
    }

    private static boolean networkTouchesWater(LevelReader level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && seen.size() <= MAX_NETWORK) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (level.getFluidState(next).is(FluidTags.WATER)) {
                    return true;
                }
                if (seen.add(next) && level.getBlockState(next).getBlock() instanceof IrrigationLineBlock) {
                    queue.add(next);
                }
            }
        }
        return false;
    }

    public static boolean isIrrigatedNear(LevelReader level, BlockPos soil) {
        for (BlockPos p : BlockPos.betweenClosed(soil.offset(-RADIUS, 0, -RADIUS), soil.offset(RADIUS, 1, RADIUS))) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() instanceof IrrigationLineBlock && state.getValue(FED)) {
                return true;
            }
        }
        return false;
    }
}
