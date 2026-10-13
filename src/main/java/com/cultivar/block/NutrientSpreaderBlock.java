package com.cultivar.block;

import com.cultivar.blockentity.NutrientSpreaderBlockEntity;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NutrientSpreaderBlock extends MachineBlock {
    public static final MapCodec<NutrientSpreaderBlock> CODEC = simpleCodec(NutrientSpreaderBlock::new);

    public NutrientSpreaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<NutrientSpreaderBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.NUTRIENT_SPREADER.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof NutrientSpreaderBlockEntity spreader)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide() && !stack.isEmpty() && spreader.load(stack)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_MACHINE_LOADED, spreader.loaded().getHoverName()));
            return InteractionResult.SUCCESS;
        }
        return level.isClientSide() && com.cultivar.world.Amendment.of(stack) != null ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NutrientSpreaderBlockEntity spreader) {
            ItemStack loaded = spreader.loaded();
            player.sendOverlayMessage(loaded.isEmpty() ? Texts.tr(Texts.MSG_SPREADER_EMPTY)
                    : Texts.tr(Texts.MSG_SPREADER_STATUS, loaded.getHoverName(), loaded.getCount()));
        }
        return InteractionResult.SUCCESS;
    }
}
