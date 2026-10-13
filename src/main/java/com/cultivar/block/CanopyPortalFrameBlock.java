package com.cultivar.block;

import com.cultivar.CultivarConfig;
import com.cultivar.registry.ModBlocks;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class CanopyPortalFrameBlock extends Block {
    public static final MapCodec<CanopyPortalFrameBlock> CODEC = simpleCodec(CanopyPortalFrameBlock::new);

    public CanopyPortalFrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CanopyPortalFrameBlock> codec() {
        return CODEC;
    }

    public static boolean tryActivate(Level level, BlockPos clicked, @Nullable Player player) {
        if (!CultivarConfig.CANOPY_ENABLED.get()) {
            if (player != null) {
                player.sendOverlayMessage(Texts.tr(Texts.MSG_PORTAL_DISABLED));
            }
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos center = clicked.offset(dx, 0, dz);
                if (isCompleteRing(level, center)) {
                    level.setBlock(center, ModBlocks.CANOPY_PORTAL.get().defaultBlockState(), Block.UPDATE_ALL);
                    level.playSound(null, center, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 0.8f);
                    if (player != null) {
                        player.sendOverlayMessage(Texts.tr(Texts.MSG_PORTAL_OPENED));
                    }
                    return true;
                }
            }
        }
        if (player != null) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_PORTAL_INCOMPLETE));
        }
        return false;
    }

    public static boolean isCompleteRing(Level level, BlockPos center) {
        if (!level.getBlockState(center).isAir()) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx != 0 || dz != 0) && !(level.getBlockState(center.offset(dx, 0, dz)).getBlock() instanceof CanopyPortalFrameBlock)) {
                    return false;
                }
            }
        }
        return true;
    }
}
