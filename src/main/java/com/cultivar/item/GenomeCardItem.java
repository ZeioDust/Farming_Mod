package com.cultivar.item;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.component.CardData;
import com.cultivar.component.GenomeTooltip;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.menu.GenomeCardMenu;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GenomeCardItem extends Item implements CultivarTooltip {
    public GenomeCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CardData card = stack.get(ModDataComponents.CARD.get());
        if (card == null) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new GenomeCardMenu(id, inventory, card),
                    Texts.tr(Texts.SCREEN_GENOME_CARD)), buf -> CardData.STREAM_CODEC.encode(buf, card));
            CultivarTrigger.fire(serverPlayer, CultivarTrigger.READ_THE_CODE);
            if (card.genome().isPresent()) {
                Guide.hint(serverPlayer, Hint.CARD);
                AlleleAtlas.record(serverPlayer, card.genome().get(), Catalog.get(level).table());
            }
            if (card.genome().isPresent() && hidesRecessive(card.genome().get(), Catalog.get(level).table())) {
                CultivarTrigger.fire(serverPlayer, CultivarTrigger.DOMINANT);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        CardData card = stack.get(ModDataComponents.CARD.get());
        return card != null && card.genome().isPresent() ? Optional.of(new GenomeTooltip(card.genome().get())) : Optional.empty();
    }

    public static boolean hidesRecessive(Genome genome, AlleleTable table) {
        for (Gene gene : Gene.VALUES) {
            int a = genome.first(gene);
            int b = genome.second(gene);
            if (a != b && table.get(gene, a).dominance() != table.get(gene, b).dominance()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        CardData card = stack.get(ModDataComponents.CARD.get());
        if (card == null) {
            builder.accept(Texts.tr(Texts.TIP_BLANK_CARD).withStyle(ChatFormatting.GRAY));
            return;
        }
        card.species().ifPresent(s -> builder.accept(Texts.tr(Texts.TIP_CARD_SPECIES, GeneText.species(s)).withStyle(ChatFormatting.GRAY)));
        card.strain().ifPresent(s -> builder.accept(Texts.tr(Texts.TIP_CARD_STRAIN, GeneText.strain(s)).withStyle(ChatFormatting.GRAY)));
        if (card.lineage() > 0) {
            builder.accept(Texts.tr(Texts.TIP_LINEAGE, card.lineage()).withStyle(ChatFormatting.DARK_GRAY));
        }
        card.genome().ifPresent(g -> {
            builder.accept(Texts.tr(Texts.TIP_GENOME_CODE, g.toHex()).withStyle(ChatFormatting.DARK_GRAY));
            if (g.isTrueBreeding()) {
                builder.accept(Texts.tr(Texts.TIP_TRUE_BREEDING).withStyle(ChatFormatting.GREEN));
            }
        });
        builder.accept(Texts.tr(Texts.TIP_CARD_OPEN).withStyle(ChatFormatting.YELLOW));
    }
}
