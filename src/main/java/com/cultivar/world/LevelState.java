package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.CropBlockEntity;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

public final class LevelState {
    private static final Map<ResourceKey<Level>, LevelState> STATES = new HashMap<>();

    private final LongOpenHashSet crops = new LongOpenHashSet();
    private final LongOpenHashSet infected = new LongOpenHashSet();
    private final LongOpenHashSet activePollinationUnits = new LongOpenHashSet();
    private final ClimateIndex climate = new ClimateIndex();
    private final BlightSystem blight = new BlightSystem();

    private long[] growthQueue = new long[0];
    private int growthCursor;
    private int growthPerTick;
    private int growthInterval;

    private LevelState() {
    }

    public static LevelState get(ServerLevel level) {
        return STATES.computeIfAbsent(level.dimension(), k -> new LevelState());
    }

    public static void clearAll() {
        STATES.clear();
    }

    public static void clear(ServerLevel level) {
        STATES.remove(level.dimension());
    }

    public void registerCrop(BlockPos pos) {
        crops.add(pos.asLong());
    }

    public void unregisterCrop(BlockPos pos) {
        crops.remove(pos.asLong());
    }

    public boolean hasCrop(long pos) {
        return crops.contains(pos);
    }

    public int cropCount() {
        return crops.size();
    }

    public long[] cropSnapshot() {
        return crops.toLongArray();
    }

    public void markInfected(BlockPos pos, boolean value) {
        if (value) {
            infected.add(pos.asLong());
        } else {
            infected.remove(pos.asLong());
        }
    }

    public long[] infectedSnapshot() {
        return infected.toLongArray();
    }

    public int infectedCount() {
        return infected.size();
    }

    public void setPollinationUnitActive(BlockPos pos, boolean active) {
        if (active) {
            activePollinationUnits.add(pos.asLong());
        } else {
            activePollinationUnits.remove(pos.asLong());
        }
    }

    public LongOpenHashSet activePollinationUnits() {
        return activePollinationUnits;
    }

    public ClimateIndex climate() {
        return climate;
    }

    public BlightSystem blight() {
        return blight;
    }

    public void tick(ServerLevel level) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("growth");
        tickGrowth(level);
        profiler.popPush("blight");
        long start = System.nanoTime();
        int attempts = blight.tick(level, this);
        if (attempts >= 0) {
            TickStats.record(TickStats.BLIGHT, System.nanoTime() - start, attempts);
        }
        profiler.pop();
    }

    private void tickGrowth(ServerLevel level) {
        int interval = CultivarConfig.SOIL_UPDATE_INTERVAL.get();
        long time = level.getGameTime();
        if (time % interval == 0 || interval != growthInterval) {
            growthInterval = interval;
            growthQueue = crops.toLongArray();
            growthCursor = 0;
            growthPerTick = Math.max(1, (growthQueue.length + interval - 1) / interval);
        }
        if (growthCursor >= growthQueue.length) {
            return;
        }
        long start = System.nanoTime();
        int end = Math.min(growthQueue.length, growthCursor + growthPerTick);
        int processed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = growthCursor; i < end; i++) {
            pos.set(BlockPos.getX(growthQueue[i]), BlockPos.getY(growthQueue[i]), BlockPos.getZ(growthQueue[i]));
            if (!level.isLoaded(pos) || !level.shouldTickBlocksAt(pos)) {
                continue;
            }
            if (level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
                crop.growthUpdate(level, interval);
                processed++;
            }
        }
        growthCursor = end;
        TickStats.record(TickStats.GROWTH, System.nanoTime() - start, processed);
    }
}
