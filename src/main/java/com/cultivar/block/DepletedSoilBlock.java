package com.cultivar.block;

import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.Nutrient;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DepletedSoilBlock extends Block {
    public static final MapCodec<DepletedSoilBlock> CODEC = simpleCodec(DepletedSoilBlock::new);
    public static final float RESTORED_LEVEL = 20f;

    public DepletedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DepletedSoilBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!stack.is(ModItems.COMPOST.get())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            restore(level, pos);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.8f, 0.9f);
            player.sendOverlayMessage(Texts.tr(Texts.MSG_SOIL_RESTORED));
        }
        return InteractionResult.SUCCESS;
    }

    public static void restore(Level level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.MANAGED_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ManagedSoilBlockEntity soil) {
            for (Nutrient nutrient : Nutrient.values()) {
                soil.set(nutrient, RESTORED_LEVEL);
            }
            soil.markUpdated();
        }
    }
}
