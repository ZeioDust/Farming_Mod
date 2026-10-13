package com.cultivar.block;

import com.cultivar.blockentity.ThermalShockUnitBlockEntity;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ThermalShockUnitBlock extends MachineBlock {
    public static final MapCodec<ThermalShockUnitBlock> CODEC = simpleCodec(ThermalShockUnitBlock::new);

    public ThermalShockUnitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ThermalShockUnitBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.THERMAL_SHOCK_UNIT.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ThermalShockUnitBlockEntity unit && !level.isClientSide()) {
            ItemStack shown = stack.copyWithCount(1);
            if (unit.load(stack)) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_MACHINE_LOADED, shown.getHoverName()));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ThermalShockUnitBlockEntity unit) {
            if (player.isSecondaryUseActive()) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_MACHINE_STATUS, getName(), unit.queued(), unit.finished(), unit.coolant()));
            } else {
                ItemStack out = unit.takeOne();
                if (!out.isEmpty() && !player.getInventory().add(out)) {
                    Block.popResource(level, pos.above(), out);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
