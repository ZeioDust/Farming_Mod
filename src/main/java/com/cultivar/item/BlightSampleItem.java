package com.cultivar.item;

import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BlightSampleItem extends Item implements CultivarTooltip {
    public BlightSampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        Identifier strain = stack.get(ModDataComponents.STRAIN.get());
        builder.accept(Texts.tr(Texts.TIP_STRAIN, strain != null ? GeneText.strain(strain) : Texts.tr(Texts.UNKNOWN))
                .withStyle(ChatFormatting.GRAY));
    }
}
