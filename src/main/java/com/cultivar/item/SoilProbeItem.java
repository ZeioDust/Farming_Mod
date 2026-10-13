package com.cultivar.item;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.DepletedSoilBlock;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.Nutrient;
import com.cultivar.entity.SoilProbeRoverEntity;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SoilProbeItem extends Item implements CultivarTooltip {
    public SoilProbeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive() && context.getClickedFace() == Direction.UP) {
            if (level instanceof ServerLevel serverLevel) {
                SoilProbeRoverEntity rover = ModEntities.SOIL_PROBE_ROVER.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
                if (rover != null) {
                    Direction heading = player.getDirection();
                    rover.deploy(pos.above(), heading, player.getUUID());
                    serverLevel.addFreshEntity(rover);
                    player.sendSystemMessage(Texts.tr(Texts.MSG_ROVER_DEPLOYED, heading.getName()));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockState(pos).getBlock() instanceof CultivarCropBlock) {
            pos = pos.below();
        }
        if (!level.isClientSide()) {
            player.sendSystemMessage(reading(level, pos));
            level.playSound(null, pos, ModSounds.PROBE_CLICK.get(), SoundSource.PLAYERS, 0.7f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }

    public static Component reading(Level level, BlockPos soilPos) {
        if (level.getBlockEntity(soilPos) instanceof ManagedSoilBlockEntity soil) {
            return Texts.tr(Texts.MSG_PROBE, Math.round(soil.get(Nutrient.NITROGEN)), Math.round(soil.get(Nutrient.PHOSPHORUS)),
                    Math.round(soil.get(Nutrient.POTASSIUM)), String.format(Locale.ROOT, "%.2f", soil.ph()), soil.moisture())
                    .withStyle(ChatFormatting.GOLD);
        }
        if (level.getBlockState(soilPos).getBlock() instanceof DepletedSoilBlock) {
            return Texts.tr(Texts.MSG_PROBE_DEPLETED).withStyle(ChatFormatting.GRAY);
        }
        return Texts.tr(Texts.MSG_PROBE_UNMANAGED).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, Consumer<Component> builder) {
        builder.accept(Texts.tr(Texts.TIP_SOIL_PROBE).withStyle(ChatFormatting.GRAY));
    }
}
