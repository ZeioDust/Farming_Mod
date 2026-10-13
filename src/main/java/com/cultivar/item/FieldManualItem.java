package com.cultivar.item;

import com.cultivar.guide.GuideHooks;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FieldManualItem extends Item implements CultivarTooltip {
    public FieldManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            GuideHooks.openManual(null);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_FIELD_MANUAL).withStyle(ChatFormatting.GRAY));
    }
}
