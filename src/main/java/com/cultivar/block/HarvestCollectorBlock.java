package com.cultivar.block;

import com.cultivar.blockentity.HarvestCollectorBlockEntity;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HarvestCollectorBlock extends MachineBlock {
    public static final MapCodec<HarvestCollectorBlock> CODEC = simpleCodec(HarvestCollectorBlock::new);

    public HarvestCollectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<HarvestCollectorBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.HARVEST_COLLECTOR.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof HarvestCollectorBlockEntity collector) {
            boolean any = false;
            for (int i = 0; i < collector.getContainerSize(); i++) {
                ItemStack stack = collector.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    any = true;
                    if (!player.getInventory().add(stack)) {
                        Block.popResource(level, pos.above(), stack);
                    }
                }
            }
            collector.markUpdated();
            if (!any) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_COLLECTOR_EMPTY));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
