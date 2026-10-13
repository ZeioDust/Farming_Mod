package com.cultivar.world;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.BlightStrain;
import com.cultivar.data.Catalog;
import com.cultivar.entity.SporeDriftEntity;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Inheritance;
import com.cultivar.genetics.Phenotype;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlightSystem {
    public static final int OUTBREAK_SAMPLES = 16;
    public static final int SIMILARITY_REFRESH_BLIGHT_TICKS = 3;
    public static final int MAX_SPORE_DRIFT = 24;

    private record Attempt(BlockPos source, BlockPos target, Identifier strain) {
    }

    private final ArrayDeque<Attempt> queue = new ArrayDeque<>();
    private long nextBlightTick = Long.MIN_VALUE;
    private int lastAttempts;
    private int lastInfections;

    public int queued() {
        return queue.size();
    }

    public int lastAttempts() {
        return lastAttempts;
    }

    public int lastInfections() {
        return lastInfections;
    }

    int tick(ServerLevel level, LevelState state) {
        if (!CultivarConfig.BLIGHT_ENABLED.get()) {
            queue.clear();
            return -1;
        }
        long now = level.getGameTime();
        int interval = CultivarConfig.BLIGHT_TICK_INTERVAL.get();
        boolean didWork = false;
        if (nextBlightTick == Long.MIN_VALUE) {
            nextBlightTick = now + interval;
        }
        if (now >= nextBlightTick) {
            nextBlightTick = now + interval;
            startBlightTick(level, state);
            didWork = true;
        }
        if (queue.isEmpty()) {
            return didWork ? 0 : -1;
        }
        int budget = CultivarConfig.BLIGHT_ATTEMPT_BUDGET.get();
        int processed = 0;
        while (processed < budget && !queue.isEmpty()) {
            processAttempt(level, queue.poll());
            processed++;
        }
        lastAttempts = processed;
        return processed;
    }

    private void startBlightTick(ServerLevel level, LevelState state) {
        Catalog catalog = Catalog.get(level);
        int radius = CultivarConfig.BLIGHT_SPREAD_RADIUS.get();
        lastInfections = 0;
        int sporeCount = level.getEntities(ModEntities.SPORE_DRIFT.get(), e -> true).size();
        for (long packed : state.infectedSnapshot()) {
            BlockPos pos = BlockPos.of(packed);
            if (!level.isLoaded(pos) || !level.shouldTickBlocksAt(pos)) {
                continue;
            }
            if (!(level.getBlockEntity(pos) instanceof CropBlockEntity crop) || !crop.isBlighted()) {
                state.markInfected(pos, false);
                continue;
            }
            Identifier strainId = crop.strain();
            BlightStrain strain = catalog.strain(strainId).orElse(null);
            int lethal = strain != null ? strain.lethalTicks() : 8;
            if (crop.advanceBlight(lethal)) {
                level.playSound(null, pos, ModSounds.BLIGHT_SPREAD.get(), SoundSource.BLOCKS, 0.6f, 0.7f);
                crop.die(level, true);
                continue;
            }
            refreshSimilarityIfStale(level, crop, radius);
            for (CropBlockEntity neighbour : neighbours(level, state, crop, radius)) {
                if (!neighbour.isBlighted()) {
                    queue.add(new Attempt(pos, neighbour.getBlockPos(), strainId));
                }
            }
            if (sporeCount < MAX_SPORE_DRIFT && level.getRandom().nextFloat() < 0.5f) {
                spawnSporeDrift(level, pos, strain != null ? strain.color() : 0xFF8A7A50);
                sporeCount++;
            }
        }
        spontaneousOutbreaks(level, state, catalog, radius);
    }

    private void processAttempt(ServerLevel level, Attempt attempt) {
        if (!level.isLoaded(attempt.target()) || !level.isLoaded(attempt.source())) {
            return;
        }
        if (!(level.getBlockEntity(attempt.source()) instanceof CropBlockEntity source) || !source.isBlighted()) {
            return;
        }
        if (!(level.getBlockEntity(attempt.target()) instanceof CropBlockEntity target) || target.isBlighted() || target.genome() == null) {
            return;
        }
        Catalog catalog = Catalog.get(level);
        BlightStrain strain = catalog.strain(attempt.strain()).orElse(null);
        if (strain == null || target.species() == null || !strain.infects(target.speciesKey())) {
            return;
        }
        RandomSource random = level.getRandom();
        if (isImmune(target, strain, catalog.table())) {
            return;
        }
        float resistance = target.phenotype().get(Gene.BLIGHT_RESISTANCE);
        if (random.nextFloat() < resistance) {
            return;
        }
        if (PollinationSystem.isolated(level, attempt.source(), attempt.target())) {
            return;
        }
        float probability = transmission(level, source, target, strain);
        if (random.nextFloat() < probability) {
            target.infect(attempt.strain());
            lastInfections++;
            level.playSound(null, attempt.target(), ModSounds.BLIGHT_SPREAD.get(), SoundSource.BLOCKS, 0.3f, 1.1f);
        }
    }

    public static float transmission(ServerLevel level, CropBlockEntity source, CropBlockEntity target, BlightStrain strain) {
        float similarity = Math.max(0f, source.neighbourhoodSimilarity());
        float mono = monocultureMultiplier(similarity);
        float moisture = soilMoisture(level, target.getBlockPos());
        float moistureFactor = 1f + CultivarConfig.MOISTURE_TRANSMISSION_BONUS.get().floatValue() * moisture;
        float density = localDensity(level, target, CultivarConfig.BLIGHT_SPREAD_RADIUS.get());
        float densityFactor = 1f + 0.5f * density;
        return Mth.clamp(strain.transmission() * mono * moistureFactor * densityFactor, 0f, 0.95f);
    }

    public static float monocultureMultiplier(float similarity) {
        float max = CultivarConfig.MONOCULTURE_MULTIPLIER_MAX.get().floatValue();
        return 1f + (max - 1f) * similarity * similarity;
    }

    public static boolean isImmune(CropBlockEntity target, BlightStrain strain, AlleleTable table) {
        Genome genome = target.isGrafted() && target.rootGenome() != null ? target.rootGenome() : target.genome();
        if (genome == null) {
            return false;
        }
        Phenotype phenotype = Phenotype.resolve(genome, table);
        for (int slot = 0; slot < 2; slot++) {
            if (phenotype.isExpressed(Gene.BLIGHT_RESISTANCE, slot)) {
                Allele allele = table.get(Gene.BLIGHT_RESISTANCE, genome.allele(Gene.BLIGHT_RESISTANCE, slot));
                if (strain.defeatedBy().contains(allele.id())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static float soilMoisture(ServerLevel level, BlockPos crop) {
        BlockState below = level.getBlockState(crop.below());
        if (below.hasProperty(FarmlandBlock.MOISTURE)) {
            return below.getValue(FarmlandBlock.MOISTURE) / 7f;
        }
        return level.getBlockEntity(crop.below()) instanceof ManagedSoilBlockEntity soil ? soil.moisture() / 7f : 0.3f;
    }

    public static float localDensity(ServerLevel level, CropBlockEntity center, int radius) {
        int side = radius * 2 + 1;
        int possible = side * side - 1;
        if (possible <= 0) {
            return 0f;
        }
        return Math.min(1f, neighbours(level, LevelState.get(level), center, radius).size() / (float) possible);
    }

    public static List<CropBlockEntity> neighbours(ServerLevel level, LevelState state, CropBlockEntity center, int radius) {
        List<CropBlockEntity> out = new ArrayList<>();
        BlockPos c = center.getBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    cursor.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
                    if (state.hasCrop(cursor.asLong()) && level.getBlockEntity(cursor) instanceof CropBlockEntity other
                            && other.species() == center.species()) {
                        out.add(other);
                    }
                }
            }
        }
        return out;
    }

    public static void refreshSimilarityIfStale(ServerLevel level, CropBlockEntity crop, int radius) {
        long now = level.getGameTime();
        long maxAge = (long) CultivarConfig.BLIGHT_TICK_INTERVAL.get() * SIMILARITY_REFRESH_BLIGHT_TICKS;
        long last = crop.similarityComputedAt();
        if (crop.genome() == null || (last != Long.MIN_VALUE && now - last < maxAge)) {
            return;
        }
        crop.setNeighbourhoodSimilarity(crop.neighbourhoodSimilarity(), now);
        Genome center = crop.genome();
        List<Genome> others = new ArrayList<>();
        for (CropBlockEntity n : neighbours(level, LevelState.get(level), crop, radius)) {
            if (n.genome() != null) {
                others.add(n.genome());
            }
        }
        BlockPos pos = crop.getBlockPos().immutable();
        AsyncWork.submit("blight-similarity", () -> meanSimilarity(center, others), similarity -> {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof CropBlockEntity target) {
                target.setNeighbourhoodSimilarity(similarity, level.getGameTime());
            }
        });
    }

    public static float meanSimilarity(Genome center, List<Genome> neighbours) {
        if (neighbours.isEmpty()) {
            return 0f;
        }
        float sum = 0f;
        for (Genome g : neighbours) {
            sum += Inheritance.similarity(center, g);
        }
        return sum / neighbours.size();
    }

    private void spontaneousOutbreaks(ServerLevel level, LevelState state, Catalog catalog, int radius) {
        long[] crops = state.cropSnapshot();
        if (crops.length == 0) {
            return;
        }
        RandomSource random = level.getRandom();
        int samples = Math.min(OUTBREAK_SAMPLES, crops.length);
        for (int i = 0; i < samples; i++) {
            BlockPos pos = BlockPos.of(crops[random.nextInt(crops.length)]);
            if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof CropBlockEntity crop) || crop.isBlighted()
                    || crop.genome() == null || crop.species() == null) {
                continue;
            }
            if (crop.neighbourhoodSimilarity() < 0f) {
                refreshSimilarityIfStale(level, crop, radius);
                continue;
            }
            List<Identifier> strains = catalog.strainsFor(crop.speciesKey());
            if (strains.isEmpty()) {
                continue;
            }
            Identifier strainId = strains.get(random.nextInt(strains.size()));
            BlightStrain strain = catalog.strain(strainId).orElseThrow();
            float density = localDensity(level, crop, radius);
            float chance = strain.spontaneousChance() * monocultureMultiplier(crop.neighbourhoodSimilarity()) * (1f + density)
                    * (1f + CultivarConfig.MOISTURE_TRANSMISSION_BONUS.get().floatValue() * soilMoisture(level, pos));
            if (random.nextFloat() < chance && !isImmune(crop, strain, catalog.table())
                    && random.nextFloat() >= crop.phenotype().get(Gene.BLIGHT_RESISTANCE)) {
                crop.infect(strainId);
                lastInfections++;
            }
        }
    }

    private static void spawnSporeDrift(ServerLevel level, BlockPos pos, int color) {
        SporeDriftEntity drift = ModEntities.SPORE_DRIFT.get().create(level, EntitySpawnReason.EVENT);
        if (drift == null) {
            return;
        }
        WindVector wind = WindSystem.get(level, pos);
        drift.setPos(pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5);
        drift.configure(wind.x() * 0.03f * (0.5f + wind.strength()), wind.z() * 0.03f * (0.5f + wind.strength()), color);
        level.addFreshEntity(drift);
    }
}
