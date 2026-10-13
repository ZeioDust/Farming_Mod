package com.cultivar.util;

import com.cultivar.genetics.Allele;
import com.cultivar.genetics.Gene;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class GeneText {
    private GeneText() {
    }

    public static MutableComponent gene(Gene gene) {
        return Component.translatable(gene.translationKey());
    }

    public static MutableComponent allele(Allele allele) {
        MutableComponent name = Component.translatable(allele.translationKey());
        if (allele.wild()) {
            name.withStyle(ChatFormatting.DARK_GREEN);
        } else if (allele.mutationOnly()) {
            name.withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return name;
    }

    public static String value(Gene gene, float value) {
        return gene.format(value);
    }

    public static MutableComponent species(Identifier species) {
        return Component.translatable("species." + species.getNamespace() + "." + species.getPath());
    }

    public static MutableComponent strain(Identifier strain) {
        return Component.translatable("strain." + strain.getNamespace() + "." + strain.getPath());
    }
}
