package com.cultivar.item;

import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class HandLensItem extends Item implements CultivarTooltip {
    public HandLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof CultivarCropBlock)) {
            if (context.getPlayer() != null && !context.getLevel().isClientSide()) {
                context.getPlayer().sendOverlayMessage(Texts.tr(Texts.MSG_LENS_NOT_CROP));
            }
            return InteractionResult.PASS;
        }
        if (context.getLevel() instanceof ServerLevel level && context.getPlayer() instanceof ServerPlayer player
                && level.getBlockEntity(context.getClickedPos()) instanceof CropBlockEntity crop) {
            crop.ensureGenome(level);
            report(player, crop, state, Catalog.get(level).table());
        }
        return InteractionResult.SUCCESS;
    }

    private static void report(ServerPlayer player, CropBlockEntity crop, BlockState state, AlleleTable table) {
        Phenotype phenotype = crop.phenotype();
        int stage = CultivarCropBlock.stage(state);
        player.sendSystemMessage(Texts.tr(Texts.MSG_LENS_HEADER, GeneText.species(crop.speciesKey()), stage, Texts.stage(stage))
                .withStyle(ChatFormatting.GREEN));
        boolean genotype = CultivarConfig.HAND_LENS_REVEALS_GENOTYPE.get();
        Genome genome = crop.genome();
        for (Gene gene : Gene.VALUES) {
            Component value = Component.literal(gene.format(phenotype.get(gene)));
            if (genotype && genome != null) {
                player.sendSystemMessage(Texts.tr(Texts.MSG_LENS_GENOTYPE, GeneText.gene(gene),
                        GeneText.allele(table.get(gene, genome.first(gene))), GeneText.allele(table.get(gene, genome.second(gene))))
                        .append(" = ").append(value));
            } else {
                player.sendSystemMessage(Texts.tr(Texts.MSG_LENS_LINE, GeneText.gene(gene), value));
            }
        }
        player.sendSystemMessage(Texts.tr(Texts.MSG_LENS_STATUS, Texts.yesNo(crop.isPollinated()), Texts.yesNo(crop.isBlighted()),
                Texts.yesNo(crop.isGrafted())).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_HAND_LENS).withStyle(ChatFormatting.GRAY));
    }
}
