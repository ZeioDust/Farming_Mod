package com.cultivar.block;

import com.cultivar.blockentity.InventoryBlockEntity;
import com.cultivar.blockentity.TickingMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class MachineBlock extends BaseEntityBlock {
    protected MachineBlock(Properties properties) {
        super(properties);
    }

    protected abstract BlockEntityType<?> blockEntityType();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (faces()) {
            builder.add(Facing.FACING);
        }
    }

    protected boolean faces() {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && faces() ? Facing.placed(state, context) : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return faces() ? Facing.rotate(state, rotation) : state;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return faces() ? Facing.mirror(state, mirror) : state;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != blockEntityType()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof TickingMachine machine && tickLevel instanceof ServerLevel serverLevel) {
                machine.serverTick(serverLevel, pos, tickState);
                if (be instanceof InventoryBlockEntity inventory && !be.isRemoved() && inventory.consumeBoost()) {
                    machine.serverTick(serverLevel, pos, serverLevel.getBlockState(pos));
                }
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MenuProvider provider) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
