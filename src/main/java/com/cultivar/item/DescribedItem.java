package com.cultivar.item;

import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DescribedItem extends Item implements CultivarTooltip {
    private final String descriptionKey;

    public DescribedItem(String descriptionKey, Properties properties) {
        super(properties);
        this.descriptionKey = descriptionKey;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(descriptionKey).withStyle(ChatFormatting.GRAY));
    }
}
