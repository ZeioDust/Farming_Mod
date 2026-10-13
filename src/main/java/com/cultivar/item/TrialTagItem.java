package com.cultivar.item;

import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TrialTagItem extends Item implements CultivarTooltip {
    public TrialTagItem(Properties properties) {
        super(properties);
    }

    public static int nextNumber(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.TRIAL_NUMBER.get(), 1);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_TRIAL_NUMBER, nextNumber(stack)).withStyle(ChatFormatting.GRAY));
    }
}
