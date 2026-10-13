package com.cultivar.item;

import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import com.cultivar.world.AsyncWork;
import com.cultivar.world.PedigreeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PedigreeSheetItem extends Item implements CultivarTooltip {
    public static final int DEPTH = 4;

    public PedigreeSheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack sheet = player.getItemInHand(hand);
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        Long line = other.get(ModDataComponents.LINEAGE.get());
        if (line != null && line > 0) {
            sheet.set(ModDataComponents.PEDIGREE_LINE.get(), line);
        } else {
            line = sheet.get(ModDataComponents.PEDIGREE_LINE.get());
        }
        if (line == null || line <= 0) {
            serverPlayer.sendSystemMessage(Texts.tr(Texts.MSG_PEDIGREE_NEED_PACKET));
            return InteractionResult.FAIL;
        }
        long root = line;
        Map<Long, PedigreeData.Record> snapshot = PedigreeData.get(serverPlayer.level().getServer()).snapshotAncestry(root, DEPTH);
        AsyncWork.submit("pedigree", () -> format(root, snapshot), lines -> {
            serverPlayer.sendSystemMessage(Texts.tr(Texts.MSG_PEDIGREE_HEADER, root).withStyle(ChatFormatting.GOLD));
            lines.forEach(serverPlayer::sendSystemMessage);
        });
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.2f);
        return InteractionResult.SUCCESS;
    }

    static List<Component> format(long root, Map<Long, PedigreeData.Record> records) {
        List<Component> lines = new ArrayList<>();
        appendNode(lines, records, root, 0, DEPTH);
        return lines;
    }

    private static void appendNode(List<Component> lines, Map<Long, PedigreeData.Record> records, long id, int depth, int maxDepth) {
        String indent = "  ".repeat(depth) + (depth > 0 ? "+ " : "");
        PedigreeData.Record record = records.get(id);
        if (record == null) {
            lines.add(Texts.tr(Texts.MSG_PEDIGREE_UNKNOWN, indent, id).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        MutableComponent node = Texts.tr(Texts.MSG_PEDIGREE_NODE, indent, id, GeneText.species(record.species()),
                record.kind().toLowerCase(java.util.Locale.ROOT), record.gameTime() / 24000L);
        lines.add(node);
        if (depth >= maxDepth) {
            return;
        }
        if (record.mother() > 0) {
            appendNode(lines, records, record.mother(), depth + 1, maxDepth);
        }
        if (record.father() > 0 && record.father() != record.mother()) {
            appendNode(lines, records, record.father(), depth + 1, maxDepth);
        }
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        Long line = stack.get(ModDataComponents.PEDIGREE_LINE.get());
        builder.accept((line != null ? Texts.tr(Texts.TIP_PEDIGREE_LINE, line) : Texts.tr(Texts.TIP_PEDIGREE_EMPTY)).withStyle(ChatFormatting.GRAY));
    }
}
