package com.cultivar.world;

import com.cultivar.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;

public final class WindSystem {
    public static final long REFRESH_TICKS = 1200L;

    private WindSystem() {
    }

    public static WindVector get(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        WindVector current = chunk.getData(ModAttachments.WIND);
        long now = level.getGameTime();
        if (current.updated() != Long.MIN_VALUE && now - current.updated() < REFRESH_TICKS) {
            return current;
        }
        long start = System.nanoTime();
        WindVector next = compute(level.getSeed(), chunk.getPos().x(), chunk.getPos().z(), now, current);
        chunk.setData(ModAttachments.WIND, next);
        TickStats.record(TickStats.WIND, System.nanoTime() - start, 1);
        return next;
    }

    static WindVector compute(long seed, int chunkX, int chunkZ, long time, WindVector previous) {
        double day = time / 24000.0;
        double prevailing = (seed % 360L) * Mth.DEG_TO_RAD + day * 0.9 + Math.sin(day * 2.1) * 0.6;
        double deviation = Math.sin(chunkX * 0.37 + day * 3.0) * 0.5 + Math.cos(chunkZ * 0.29 - day * 2.3) * 0.5;
        double angle = prevailing + deviation;
        float strength = (float) Mth.clamp(0.55 + 0.35 * Math.sin(day * 5.0 + chunkX * 0.11 + chunkZ * 0.07), 0.1, 1.0);
        float x = (float) Math.cos(angle);
        float z = (float) Math.sin(angle);
        if (previous.updated() != Long.MIN_VALUE) {
            x = 0.6f * x + 0.4f * previous.x();
            z = 0.6f * z + 0.4f * previous.z();
            float length = (float) Math.sqrt(x * x + z * z);
            if (length > 1e-4f) {
                x /= length;
                z /= length;
            }
            strength = 0.6f * strength + 0.4f * previous.strength();
        }
        return new WindVector(x, z, strength, time);
    }
}
