package com.cultivar.world.dimension;

import com.cultivar.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

public class CanopyChunkGenerator extends ChunkGenerator {
    public static final MapCodec<CanopyChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(i, i.stable(CanopyChunkGenerator::new)));

    public static final int MIN_Y = 0;
    public static final int HEIGHT = 256;
    public static final int FLOOR_TOP = 6;
    public static final int[] PLATFORM_LEVELS = {48, 96, 144, 192};
    public static final int TRUNK_TOP = 200;
    private static final int CELL = 16;

    public CanopyChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static long hash(long x, long z, long salt) {
        long h = x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return h;
    }

    private static double unit(long x, long z, long salt) {
        return (hash(x, z, salt) >>> 11) * 0x1.0p-53;
    }

    private static double noise(int x, int z, int scale, long salt) {
        int cx = Math.floorDiv(x, scale);
        int cz = Math.floorDiv(z, scale);
        double fx = (x - cx * (double) scale) / scale;
        double fz = (z - cz * (double) scale) / scale;
        double sx = fx * fx * (3 - 2 * fx);
        double sz = fz * fz * (3 - 2 * fz);
        double a = unit(cx, cz, salt);
        double b = unit(cx + 1, cz, salt);
        double c = unit(cx, cz + 1, salt);
        double d = unit(cx + 1, cz + 1, salt);
        return (a + (b - a) * sx) + ((c + (d - c) * sx) - (a + (b - a) * sx)) * sz;
    }

    private static int trunkX(int cellX, int cellZ) {
        return cellX * CELL + 3 + (int) (unit(cellX, cellZ, 11) * 10);
    }

    private static int trunkZ(int cellX, int cellZ) {
        return cellZ * CELL + 3 + (int) (unit(cellX, cellZ, 12) * 10);
    }

    private static double[] nearestTrunk(int x, int z) {
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        double best = Double.MAX_VALUE;
        double radius = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int tx = trunkX(cellX + dx, cellZ + dz);
                int tz = trunkZ(cellX + dx, cellZ + dz);
                double d = (double) (x - tx) * (x - tx) + (double) (z - tz) * (z - tz);
                if (d < best) {
                    best = d;
                    radius = 6 + unit(cellX + dx, cellZ + dz, 13) * 5;
                }
            }
        }
        return new double[]{best, radius};
    }

    public static BlockState[] column(int x, int z) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bark = ModBlocks.CANOPY_BARK.get().defaultBlockState();
        BlockState barkY = bark.setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);
        BlockState leaf = ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState();
        BlockState vine = ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState();
        BlockState[] column = new BlockState[HEIGHT];
        java.util.Arrays.fill(column, air);
        for (int y = 0; y <= FLOOR_TOP; y++) {
            column[y] = y < 4 ? bark : leaf;
        }
        column[0] = Blocks.BEDROCK.defaultBlockState();
        double[] trunk = nearestTrunk(x, z);
        double distance = Math.sqrt(trunk[0]);
        boolean isTrunk = distance <= 2.2;
        if (isTrunk) {
            for (int y = FLOOR_TOP + 1; y <= TRUNK_TOP; y++) {
                column[y] = barkY;
            }
        }
        for (int i = 0; i < PLATFORM_LEVELS.length; i++) {
            int level = PLATFORM_LEVELS[i];
            double sheet = noise(x, z, 24, 100 + i);
            boolean aroundTrunk = distance <= trunk[1] + (noise(x, z, 6, 200 + i) - 0.5) * 3;
            boolean bridge = sheet > 0.62;
            if (!isTrunk && (aroundTrunk || bridge)) {
                column[level] = leaf;
                column[level - 1] = leaf;
                if (sheet > 0.8 || distance < trunk[1] * 0.5) {
                    column[level - 2] = leaf;
                }
                if (unit(x, z, 300 + i) < 0.06) {
                    int length = 3 + (int) (unit(x, z, 400 + i) * 9);
                    for (int y = level - 3; y > level - 3 - length && y > FLOOR_TOP; y--) {
                        column[y] = vine;
                    }
                }
            }
        }
        if (!isTrunk && unit(x, z, 500) < 0.03) {
            int length = 2 + (int) (unit(x, z, 501) * 5);
            for (int y = FLOOR_TOP + 1; y <= FLOOR_TOP + length; y++) {
                column[y] = vine;
            }
        }
        return column;
    }

    private static int topSolid(BlockState[] column, Heightmap.Types type) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (type.isOpaque().test(column[y])) {
                return MIN_Y + y + 1;
            }
        }
        return MIN_Y;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int chunkMinY = chunk.getMinY();
        int chunkMaxY = chunk.getMaxY();
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                BlockState[] column = column(minX + lx, minZ + lz);
                for (int i = 0; i < column.length; i++) {
                    BlockState state = column[i];
                    int y = MIN_Y + i;
                    if (state.isAir() || y < chunkMinY || y > chunkMaxY) {
                        continue;
                    }
                    chunk.setBlockState(pos.set(lx, y, lz), state);
                    oceanFloor.update(lx, y, lz, state);
                    worldSurface.update(lx, y, lz, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return topSolid(column(x, z), type);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return new NoiseColumn(MIN_Y, column(x, z));
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return MIN_Y;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {
        double[] trunk = nearestTrunk(feetPos.getX(), feetPos.getZ());
        result.add(String.format(java.util.Locale.ROOT, "Canopy: nearest trunk %.1f blocks, platform radius %.1f", Math.sqrt(trunk[0]), trunk[1]));
    }
}
