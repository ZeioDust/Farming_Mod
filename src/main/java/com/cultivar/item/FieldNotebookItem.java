package com.cultivar.item;

import com.cultivar.registry.ModAttachments;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import com.cultivar.world.CrossLog;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FieldNotebookItem extends Item implements CultivarTooltip {
    public static final int PER_PAGE = 6;

    public FieldNotebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            CrossLog log = serverPlayer.getData(ModAttachments.CROSS_LOG);
            List<CrossLog.Entry> entries = log.entries();
            if (entries.isEmpty()) {
                serverPlayer.sendSystemMessage(Texts.tr(Texts.MSG_NOTEBOOK_EMPTY));
                return InteractionResult.SUCCESS;
            }
            int pages = (entries.size() + PER_PAGE - 1) / PER_PAGE;
            int page = stack.getOrDefault(ModDataComponents.NOTEBOOK_PAGE.get(), 0);
            if (player.isSecondaryUseActive()) {
                page = (page + 1) % pages;
                stack.set(ModDataComponents.NOTEBOOK_PAGE.get(), page);
            }
            page = Math.min(page, pages - 1);
            serverPlayer.sendSystemMessage(Texts.tr(Texts.MSG_NOTEBOOK_HEADER, page + 1, pages).withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < PER_PAGE; i++) {
                int index = entries.size() - 1 - (page * PER_PAGE + i);
                if (index < 0) {
                    break;
                }
                CrossLog.Entry e = entries.get(index);
                serverPlayer.sendSystemMessage(Texts.tr(Texts.MSG_NOTEBOOK_ENTRY, e.day(), GeneText.species(e.species()), e.method(),
                        e.mother(), e.father(), String.join(", ", e.offspring())));
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_NOTEBOOK).withStyle(ChatFormatting.GRAY));
    }
}
