package com.cultivar.item;

import com.cultivar.data.CropSpecies;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProduceItem extends Item implements CultivarTooltip {
    public static final int PRIME_REGENERATION_TICKS = 100;
    private final CropSpecies species;

    public ProduceItem(CropSpecies species, Properties properties) {
        super(properties);
        this.species = species;
    }

    public CropSpecies species() {
        return species;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean prime = GradeLogic.of(stack) == Grade.PRIME;
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (prime && !level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PRIME_REGENERATION_TICKS, 0));
        }
        return result;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(gradeLine(stack));
        if (species == CropSpecies.TUBER) {
            builder.accept(Texts.tr(Texts.TIP_TUBER_FIXER).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static Component gradeLine(ItemStack stack) {
        Grade grade = GradeLogic.of(stack);
        return Texts.tr(Texts.TIP_GRADE, Component.translatable(grade.translationKey()).withColor(grade.color() & 0xFFFFFF))
                .withStyle(ChatFormatting.GRAY);
    }
}
