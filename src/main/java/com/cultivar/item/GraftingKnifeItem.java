package com.cultivar.item;

import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Milestones;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModSounds;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GraftingKnifeItem extends Item implements CultivarTooltip {
    public GraftingKnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!(context.getLevel().getBlockState(pos).getBlock() instanceof CultivarCropBlock) || player == null) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (!CultivarConfig.GRAFTING_ENABLED.get()) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_GRAFT_DISABLED));
            return InteractionResult.FAIL;
        }
        ItemStack knife = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        if (!(level.getBlockEntity(pos) instanceof CropBlockEntity crop) || !isGraftable(state, crop)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_GRAFT_FAIL));
            return InteractionResult.FAIL;
        }
        crop.ensureGenome(level);
        BlockPos root = knife.get(ModDataComponents.GRAFT_ROOT.get());
        if (root == null || root.equals(pos)) {
            knife.set(ModDataComponents.GRAFT_ROOT.get(), pos.immutable());
            player.sendOverlayMessage(Texts.tr(Texts.MSG_GRAFT_ROOT));
            return InteractionResult.SUCCESS;
        }
        knife.remove(ModDataComponents.GRAFT_ROOT.get());
        boolean adjacent = root.getY() == pos.getY() && root.distManhattan(pos) == 1;
        if (!adjacent || !(level.getBlockEntity(root) instanceof CropBlockEntity rootCrop) || !isGraftable(level.getBlockState(root), rootCrop)
                || rootCrop.species() != crop.species() || rootCrop.genome() == null || crop.genome() == null) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_GRAFT_FAIL));
            return InteractionResult.FAIL;
        }
        var species = crop.species();
        var scionGenome = crop.genome();
        long scionLineage = crop.lineage();
        var rootGenome = rootCrop.genome();
        level.setBlock(root, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ModBlocks.GRAFT_UNION.get().defaultBlockState().setValue(CultivarCropBlock.STAGE, CultivarCropBlock.FLOWERING - 1),
                Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CropBlockEntity union) {
            union.makeGraft(species, scionGenome, scionLineage, rootGenome);
        }
        level.playSound(null, pos, ModSounds.GRAFT_CUT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        knife.hurtAndBreak(1, player, context.getHand() == InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        player.sendOverlayMessage(Texts.tr(Texts.MSG_GRAFT_DONE));
        if (player instanceof ServerPlayer serverPlayer) {
            Guide.milestone(serverPlayer, Milestones.GRAFTED);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isGraftable(BlockState state, CropBlockEntity crop) {
        return state.getBlock() instanceof CultivarCropBlock && !(state.getBlock() instanceof com.cultivar.block.GraftUnionBlock)
                && CultivarCropBlock.stage(state) == CultivarCropBlock.MAX_STAGE && !crop.isBlighted() && !crop.isGrafted();
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        BlockPos root = stack.get(ModDataComponents.GRAFT_ROOT.get());
        builder.accept((root == null ? Texts.tr(Texts.TIP_KNIFE_EMPTY) : Texts.tr(Texts.TIP_KNIFE_ROOT, root.toShortString()))
                .withStyle(ChatFormatting.GRAY));
    }
}
