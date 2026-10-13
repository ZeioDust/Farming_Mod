package com.cultivar.item;

import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class WildSeedItem extends Item implements CultivarTooltip {
    public WildSeedItem(Properties properties) {
        super(properties);
    }

    public static CropSpecies speciesOf(ItemStack stack) {
        Identifier id = stack.get(ModDataComponents.SPECIES.get());
        CropSpecies species = CropSpecies.byKey(id);
        return species != null ? species : CropSpecies.WHEATLINE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return Planting.plant(context, speciesOf(context.getItemInHand()), true);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_WILD_SPECIES, GeneText.species(speciesOf(stack).key())).withStyle(ChatFormatting.DARK_GREEN));
        SeedPacketItem.appendGenomeTooltip(stack, builder);
    }
}
