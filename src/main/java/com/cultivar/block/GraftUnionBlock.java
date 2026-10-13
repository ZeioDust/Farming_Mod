package com.cultivar.block;

import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.entity.FallingProduceEntity;
import com.cultivar.world.HarvestLogic;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GraftUnionBlock extends CultivarCropBlock {
    public static final MapCodec<GraftUnionBlock> GRAFT_CODEC = simpleCodec(GraftUnionBlock::new);

    public GraftUnionBlock(Properties properties) {
        super(null, properties);
    }

    @Override
    protected MapCodec<? extends GraftUnionBlock> codec() {
        return GRAFT_CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(BAGGED) && player.isSecondaryUseActive()) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        if (stage(state) < MAX_STAGE) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            HarvestLogic.Result result = HarvestLogic.harvest(serverLevel, crop, state, player instanceof ServerPlayer sp ? sp : null);
            for (ItemStack stack : result.all()) {
                FallingProduceEntity.spawn(level, pos, stack);
            }
            crop.regrowAfterHarvest();
        }
        return InteractionResult.SUCCESS;
    }
}
