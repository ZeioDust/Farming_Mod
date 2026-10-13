package com.cultivar.item;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.component.PollenLoad;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class PollinationBrushItem extends Item implements CultivarTooltip {
    public PollinationBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof CultivarCropBlock)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        if (!(serverLevel.getBlockEntity(pos) instanceof CropBlockEntity crop)) {
            return InteractionResult.PASS;
        }
        crop.ensureGenome(serverLevel);
        ItemStack brush = context.getItemInHand();
        PollenLoad pollen = brush.get(ModDataComponents.POLLEN.get());
        if (!crop.isFlowering()) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_NOT_FLOWERING));
            return InteractionResult.FAIL;
        }
        if (pollen == null) {
            if (crop.isGrafted() || crop.isBagged()) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_NO_POLLEN));
                return InteractionResult.FAIL;
            }
            brush.set(ModDataComponents.POLLEN.get(), new PollenLoad(serverLevel.dimension().identifier(), pos.immutable(), crop.speciesKey(),
                    crop.genome(), crop.lineage()));
            player.sendOverlayMessage(Texts.tr(Texts.MSG_POLLEN_COLLECTED, GeneText.species(crop.speciesKey()), pos.toShortString()));
            serverLevel.playSound(null, pos, ModSounds.POLLEN_PUFF.get(), SoundSource.PLAYERS, 0.6f, 1.3f);
            return InteractionResult.SUCCESS;
        }
        if (!pollen.dimension().equals(serverLevel.dimension().identifier())) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_POLLEN_DIMENSION));
            return InteractionResult.FAIL;
        }
        if (!pollen.species().equals(crop.speciesKey())) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_WRONG_SPECIES));
            return InteractionResult.FAIL;
        }
        if (pollen.source().equals(pos)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_SAME_PLANT));
            return InteractionResult.FAIL;
        }
        if (crop.receivePollen(crop.species(), pollen.genome(), pollen.lineage(), CropBlockEntity.PollinationMethod.BRUSH, player.getUUID())) {
            brush.remove(ModDataComponents.POLLEN.get());
            player.sendOverlayMessage(Texts.tr(Texts.MSG_POLLEN_APPLIED, pos.toShortString(), pollen.source().toShortString()));
            serverLevel.playSound(null, pos, ModSounds.POLLEN_PUFF.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
            CultivarTrigger.fire(player, CultivarTrigger.FIRST_CROSS);
        } else {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_BAGGED));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && stack.has(ModDataComponents.POLLEN.get())) {
            stack.remove(ModDataComponents.POLLEN.get());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.POLLEN.get());
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        PollenLoad pollen = stack.get(ModDataComponents.POLLEN.get());
        if (pollen == null) {
            builder.accept(Texts.tr(Texts.TIP_BRUSH_EMPTY).withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Texts.tr(Texts.TIP_POLLEN, GeneText.species(pollen.species()), pollen.source().toShortString())
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
