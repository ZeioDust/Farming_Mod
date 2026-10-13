package com.cultivar.item;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class IsolationBagItem extends Item implements CultivarTooltip {
    public IsolationBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CultivarCropBlock) || !(level.getBlockEntity(pos) instanceof CropBlockEntity)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (state.getValue(CultivarCropBlock.BAGGED)) {
            if (player != null && !level.isClientSide()) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_ALREADY_BAGGED));
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CultivarCropBlock.BAGGED, true), Block.UPDATE_ALL);
            context.getItemInHand().consume(1, player);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8f, 1.0f);
            if (player != null) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_BAGGED));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_ISOLATION_BAG).withStyle(ChatFormatting.GRAY));
    }
}
