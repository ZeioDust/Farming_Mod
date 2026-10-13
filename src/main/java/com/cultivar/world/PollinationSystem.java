package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.block.IsolationScreenBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.registry.ModSounds;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class PollinationSystem {
    public static final float UNIT_CHANCE = 0.98f;

    private PollinationSystem() {
    }

    public static void tryWindPollination(ServerLevel level, CropBlockEntity target) {
        long start = System.nanoTime();
        LevelState state = LevelState.get(level);
        int radius = CultivarConfig.POLLINATION_RADIUS.get();
        BlockPos targetPos = target.getBlockPos();
        List<CropBlockEntity> donors = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dz == 0 && dy == 0) {
                        continue;
                    }
                    cursor.set(targetPos.getX() + dx, targetPos.getY() + dy, targetPos.getZ() + dz);
                    if (!state.hasCrop(cursor.asLong())) {
                        continue;
                    }
                    if (level.getBlockEntity(cursor) instanceof CropBlockEntity donor && isEligibleDonor(donor, target)) {
                        donors.add(donor);
                    }
                }
            }
        }
        if (donors.isEmpty()) {
            TickStats.record(TickStats.POLLINATION, System.nanoTime() - start, 0);
            return;
        }
        RandomSource random = level.getRandom();
        boolean boosted = isBoostedByUnit(level, state, targetPos);
        WindVector wind = WindSystem.get(level, targetPos);
        float windStrength = CultivarConfig.WIND_INFLUENCE_STRENGTH.get().floatValue();
        float base = CultivarConfig.POLLINATION_BASE_CHANCE.get().floatValue();
        for (int i = donors.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            CropBlockEntity tmp = donors.get(i);
            donors.set(i, donors.get(j));
            donors.set(j, tmp);
        }
        for (CropBlockEntity donor : donors) {
            BlockPos donorPos = donor.getBlockPos();
            if (isolated(level, donorPos, targetPos)) {
                continue;
            }
            float chance = boosted ? UNIT_CHANCE : chance(base, radius, donorPos, targetPos, wind, windStrength);
            if (random.nextFloat() < chance) {
                if (target.receivePollen(donor.species(), donor.genome(), donor.lineage(), CropBlockEntity.PollinationMethod.WIND, null)) {
                    level.playSound(null, targetPos, ModSounds.POLLEN_PUFF.get(), SoundSource.BLOCKS, 0.4f, 0.9f + random.nextFloat() * 0.2f);
                }
                break;
            }
        }
        TickStats.record(TickStats.POLLINATION, System.nanoTime() - start, donors.size());
    }

    public static boolean isEligibleDonor(CropBlockEntity donor, CropBlockEntity target) {
        return donor != target && donor.species() == target.species() && donor.species() != null && donor.genome() != null
                && donor.isFlowering() && !donor.isBagged() && !donor.isGrafted() && !donor.isBlighted();
    }

    public static float chance(float base, int radius, BlockPos donor, BlockPos target, WindVector wind, float windInfluence) {
        double dx = target.getX() - donor.getX();
        double dz = target.getZ() - donor.getZ();
        double dy = target.getY() - donor.getY();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > radius) {
            return 0f;
        }
        float falloff = (float) (1.0 - distance / (radius + 1.0));
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float alignment = horizontal < 1e-6 ? 0f : (float) ((dx * wind.x() + dz * wind.z()) / horizontal);
        float windFactor = Math.max(0.05f, 1f + windInfluence * wind.strength() * alignment);
        return Mth.clamp(base * falloff * windFactor, 0f, 1f);
    }

    private static boolean isBoostedByUnit(ServerLevel level, LevelState state, BlockPos target) {
        LongIterator it = state.activePollinationUnits().iterator();
        while (it.hasNext()) {
            BlockPos unit = BlockPos.of(it.nextLong());
            if (unit.distSqr(target) <= PollinationUnitBlockEntity.RADIUS * PollinationUnitBlockEntity.RADIUS
                    && level.getBlockEntity(unit) instanceof PollinationUnitBlockEntity be && be.isActive()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isolated(ServerLevel level, BlockPos from, BlockPos to) {
        double sx = from.getX() + 0.5;
        double sy = from.getY() + 0.5;
        double sz = from.getZ() + 0.5;
        double ex = to.getX() + 0.5;
        double ey = to.getY() + 0.5;
        double ez = to.getZ() + 0.5;
        double length = Math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy) + (ez - sz) * (ez - sz));
        int steps = Math.max(1, (int) Math.ceil(length * 3));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long last = Long.MIN_VALUE;
        for (int i = 1; i < steps; i++) {
            double t = i / (double) steps;
            cursor.set(Mth.floor(sx + (ex - sx) * t), Mth.floor(sy + (ey - sy) * t), Mth.floor(sz + (ez - sz) * t));
            long key = cursor.asLong();
            if (key == last) {
                continue;
            }
            last = key;
            if (level.getBlockState(cursor).getBlock() instanceof IsolationScreenBlock) {
                return true;
            }
        }
        return false;
    }
}
