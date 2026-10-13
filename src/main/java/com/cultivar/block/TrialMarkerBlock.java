package com.cultivar.block;

import com.cultivar.blockentity.TrialMarkerBlockEntity;
import com.cultivar.item.TrialTagItem;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrialMarkerBlock extends MachineBlock {
    public static final MapCodec<TrialMarkerBlock> CODEC = simpleCodec(TrialMarkerBlock::new);
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 14, 10);

    public TrialMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TrialMarkerBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.TRIAL_MARKER.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!stack.is(ModItems.TRIAL_TAG.get()) || !(level.getBlockEntity(pos) instanceof TrialMarkerBlockEntity marker)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            String label = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : TrialMarkerBlockEntity.DEFAULT_LABEL;
            int number = TrialTagItem.nextNumber(stack);
            marker.set(label, number);
            stack.set(ModDataComponents.TRIAL_NUMBER.get(), number + 1);
            player.sendOverlayMessage(Texts.tr(Texts.MSG_TRIAL_SET, label, number));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TrialMarkerBlockEntity marker) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_TRIAL_INFO, marker.label(), marker.number()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean faces() {
        return false;
    }
}
