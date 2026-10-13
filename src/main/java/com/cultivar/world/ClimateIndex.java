package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public final class ClimateIndex {
    private final Long2ObjectOpenHashMap<LongOpenHashSet> volumeOwners = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<LongOpenHashSet> boundaryOwners = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<LongOpenHashSet[]> byController = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet unsealed = new LongOpenHashSet();

    public void put(BlockPos controller, LongOpenHashSet volume, LongOpenHashSet boundary, boolean sealed) {
        remove(controller);
        long owner = controller.asLong();
        add(volumeOwners, volume, owner);
        add(boundaryOwners, boundary, owner);
        byController.put(owner, new LongOpenHashSet[]{volume, boundary});
        if (sealed) {
            unsealed.remove(owner);
        } else {
            unsealed.add(owner);
        }
    }

    private static void add(Long2ObjectOpenHashMap<LongOpenHashSet> map, LongOpenHashSet positions, long owner) {
        LongIterator it = positions.iterator();
        while (it.hasNext()) {
            map.computeIfAbsent(it.nextLong(), k -> new LongOpenHashSet(2)).add(owner);
        }
    }

    private static void subtract(Long2ObjectOpenHashMap<LongOpenHashSet> map, LongOpenHashSet positions, long owner) {
        LongIterator it = positions.iterator();
        while (it.hasNext()) {
            long key = it.nextLong();
            LongOpenHashSet owners = map.get(key);
            if (owners != null) {
                owners.remove(owner);
                if (owners.isEmpty()) {
                    map.remove(key);
                }
            }
        }
    }

    public void remove(BlockPos controller) {
        long owner = controller.asLong();
        unsealed.remove(owner);
        LongOpenHashSet[] sets = byController.remove(owner);
        if (sets == null) {
            return;
        }
        subtract(volumeOwners, sets[0], owner);
        subtract(boundaryOwners, sets[1], owner);
    }

    public void onBlockChanged(ServerLevel level, BlockPos pos) {
        if (byController.isEmpty()) {
            return;
        }
        long key = pos.asLong();
        markOwners(level, volumeOwners.get(key));
        markOwners(level, boundaryOwners.get(key));
    }

    private static void markOwners(ServerLevel level, @Nullable LongOpenHashSet owners) {
        if (owners == null) {
            return;
        }
        LongIterator it = owners.iterator();
        while (it.hasNext()) {
            BlockPos controllerPos = BlockPos.of(it.nextLong());
            if (level.isLoaded(controllerPos) && level.getBlockEntity(controllerPos) instanceof ClimateControllerBlockEntity controller) {
                controller.markDirty();
            }
        }
    }

    public void onChunkLoaded(ServerLevel level, ChunkPos chunk) {
        if (unsealed.isEmpty()) {
            return;
        }
        int reach = SectionPos.blockToSectionCoord(ClimateScan.HORIZONTAL_RANGE) + 1;
        LongIterator it = unsealed.iterator();
        while (it.hasNext()) {
            BlockPos controllerPos = BlockPos.of(it.nextLong());
            if (Math.abs(SectionPos.blockToSectionCoord(controllerPos.getX()) - chunk.x()) <= reach
                    && Math.abs(SectionPos.blockToSectionCoord(controllerPos.getZ()) - chunk.z()) <= reach
                    && level.isLoaded(controllerPos) && level.getBlockEntity(controllerPos) instanceof ClimateControllerBlockEntity controller) {
                controller.markDirty();
            }
        }
    }

    public static @Nullable ClimateControllerBlockEntity controllerFor(ServerLevel level, BlockPos pos) {
        if (!CultivarConfig.GREENHOUSE_ENABLED.get()) {
            return null;
        }
        LongOpenHashSet owners = LevelState.get(level).climate().volumeOwners.get(pos.asLong());
        if (owners == null) {
            return null;
        }
        LongIterator it = owners.iterator();
        while (it.hasNext()) {
            BlockPos controllerPos = BlockPos.of(it.nextLong());
            if (level.isLoaded(controllerPos) && level.getBlockEntity(controllerPos) instanceof ClimateControllerBlockEntity controller
                    && controller.isSealed()) {
                return controller;
            }
        }
        return null;
    }
}
