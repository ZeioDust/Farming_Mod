package com.cultivar.item;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.DepletedSoilBlock;
import com.cultivar.block.ManagedSoilBlock;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import com.cultivar.world.Amendment;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AmendmentItem extends Item implements CultivarTooltip {
    public AmendmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CultivarCropBlock)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos soilPos = pos.below();
        BlockState soilState = level.getBlockState(soilPos);
        if (soilState.getBlock() instanceof DepletedSoilBlock && stack.is(ModItems.COMPOST.get())) {
            if (!level.isClientSide()) {
                DepletedSoilBlock.restore(level, soilPos);
                stack.consume(1, player);
                if (player != null) {
                    player.sendOverlayMessage(Texts.tr(Texts.MSG_SOIL_RESTORED));
                }
            }
            return InteractionResult.SUCCESS;
        }
        Amendment amendment = Amendment.of(stack);
        if (amendment != null && player != null && level.getBlockEntity(soilPos) instanceof ManagedSoilBlockEntity soil) {
            if (!level.isClientSide()) {
                ManagedSoilBlock.applyAmendment(level, soilPos, player, stack, amendment, soil);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_AMENDMENT).withStyle(ChatFormatting.GRAY));
    }
}
