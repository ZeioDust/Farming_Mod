package com.cultivar.item;

import com.cultivar.block.CanopyPortalFrameBlock;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class CanopyKeyItem extends Item implements CultivarTooltip {
    public CanopyKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof CanopyPortalFrameBlock)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            CanopyPortalFrameBlock.tryActivate(context.getLevel(), context.getClickedPos(), context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_CANOPY_KEY).withStyle(ChatFormatting.GRAY));
    }
}
