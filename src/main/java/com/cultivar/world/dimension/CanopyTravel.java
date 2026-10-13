package com.cultivar.world.dimension;

import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class CanopyTravel {
    public static final int COOLDOWN_TICKS = 100;

    private CanopyTravel() {
    }

    public static void travel(ServerLevel from, Entity entity) {
        if (entity.isOnPortalCooldown() || entity.isPassenger() || entity.isVehicle()) {
            return;
        }
        boolean toCanopy = !from.dimension().equals(CultivarDimensions.CANOPY_LEVEL);
        ServerLevel target = from.getServer().getLevel(toCanopy ? CultivarDimensions.CANOPY_LEVEL : Level.OVERWORLD);
        if (target == null) {
            return;
        }
        int x = entity.getBlockX();
        int z = entity.getBlockZ();
        target.getChunk(x >> 4, z >> 4);
        BlockPos landing;
        if (toCanopy) {
            landing = canopyLanding(target, x, z);
            buildReturnRing(target, landing.offset(2, -1, 0));
        } else {
            landing = new BlockPos(x, target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z).offset(2, 0, 0);
        }
        entity.setPortalCooldown(COOLDOWN_TICKS);
        entity.teleport(new TeleportTransition(target, Vec3.atBottomCenterOf(landing), Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.DO_NOTHING));
        target.playSound(null, landing, ModSounds.CANOPY_RUSTLE.get(), SoundSource.AMBIENT, 1.0f, 0.9f);
    }

    static BlockPos canopyLanding(ServerLevel canopy, int x, int z) {
        int top = canopy.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (top > canopy.getMinY() + 4 && top < canopy.getMaxY() - 2) {
            return new BlockPos(x, top, z);
        }
        int y = CanopyChunkGenerator.PLATFORM_LEVELS[1] + 1;
        BlockPos base = new BlockPos(x, y - 1, z);
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, 0, -3), base.offset(3, 0, 3))) {
            canopy.setBlock(p, ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, 1, -3), base.offset(3, 3, 3))) {
            canopy.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return new BlockPos(x, y, z);
    }

    static void buildReturnRing(ServerLevel level, BlockPos center) {
        if (level.getBlockState(center).is(ModBlocks.CANOPY_PORTAL.get())) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                level.setBlock(p.below(), ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(p, dx == 0 && dz == 0 ? ModBlocks.CANOPY_PORTAL.get().defaultBlockState()
                        : ModBlocks.CANOPY_PORTAL_FRAME.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
