package com.cultivar.item;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.processing.RationData;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RationPackItem extends Item implements CultivarTooltip {
    public RationPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        RationData data = stack.get(ModDataComponents.RATION.get());
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (data != null && !level.isClientSide()) {
            int strength = data.strength();
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80 * strength, 0));
            if (strength >= 4) {
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0));
            }
            if (strength >= 6) {
                entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0));
            }
            if (entity instanceof ServerPlayer player) {
                CultivarTrigger.fire(player, CultivarTrigger.RATION);
            }
        }
        return result;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        RationData data = stack.get(ModDataComponents.RATION.get());
        if (data == null) {
            builder.accept(Texts.tr(Texts.TIP_RATION_EMPTY).withStyle(ChatFormatting.GRAY));
            return;
        }
        builder.accept(Texts.tr(Texts.TIP_RATION_STRENGTH, data.strength()).withStyle(ChatFormatting.GOLD));
        MutableComponent list = Component.empty();
        for (int i = 0; i < data.species().size(); i++) {
            if (i > 0) {
                list.append(", ");
            }
            list.append(GeneText.species(data.species().get(i)));
        }
        builder.accept(list.withStyle(ChatFormatting.GRAY));
        builder.accept(Texts.tr(Texts.TIP_GRADE, Component.translatable(data.grade().translationKey()).withColor(data.grade().color() & 0xFFFFFF))
                .withStyle(ChatFormatting.GRAY));
    }
}
