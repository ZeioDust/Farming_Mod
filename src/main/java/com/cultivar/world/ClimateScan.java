package com.cultivar.world;

import com.cultivar.block.GreenhousePaneBlock;
import com.cultivar.block.GrowLightBlock;
import com.cultivar.block.HumidityDeviceBlock;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public final class ClimateScan {
    public static final int HORIZONTAL_RANGE = 24;
    public static final int VERTICAL_RANGE = 16;

    public record Result(boolean sealed, LongOpenHashSet volume, LongOpenHashSet boundary, float insulation, int growLights,
                         int humidifiers, int dehumidifiers) {
        public static Result unsealed() {
            return new Result(false, new LongOpenHashSet(), new LongOpenHashSet(), 0f, 0, 0, 0);
        }

        public static Result leaked(LongOpenHashSet visited, LongOpenHashSet boundary) {
            return new Result(false, visited, boundary, 0f, 0, 0, 0);
        }
    }

    public static final class Snapshot {
        private final int minSectionX;
        private final int minSectionY;
        private final int minSectionZ;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final java.util.List<@Nullable PalettedContainer<BlockState>> sections;
        final BlockPos origin;
        final int maxBlocks;

        private Snapshot(BlockPos origin, int maxBlocks, int minSectionX, int minSectionY, int minSectionZ, int sizeX, int sizeY, int sizeZ) {
            this.origin = origin;
            this.maxBlocks = maxBlocks;
            this.minSectionX = minSectionX;
            this.minSectionY = minSectionY;
            this.minSectionZ = minSectionZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sections = new java.util.ArrayList<>(java.util.Collections.nCopies(sizeX * sizeY * sizeZ, null));
        }

        @Nullable BlockState get(int x, int y, int z) {
            int sx = SectionPos.blockToSectionCoord(x) - minSectionX;
            int sy = SectionPos.blockToSectionCoord(y) - minSectionY;
            int sz = SectionPos.blockToSectionCoord(z) - minSectionZ;
            if (sx < 0 || sy < 0 || sz < 0 || sx >= sizeX || sy >= sizeY || sz >= sizeZ) {
                return null;
            }
            PalettedContainer<BlockState> container = sections.get((sx * sizeY + sy) * sizeZ + sz);
            if (container == null) {
                return null;
            }
            return container.get(x & 15, y & 15, z & 15);
        }

        boolean inRange(int x, int y, int z) {
            return Math.abs(x - origin.getX()) <= HORIZONTAL_RANGE && Math.abs(y - origin.getY()) <= VERTICAL_RANGE
                    && Math.abs(z - origin.getZ()) <= HORIZONTAL_RANGE;
        }
    }

    private ClimateScan() {
    }

    public static Snapshot snapshot(ServerLevel level, BlockPos origin, int maxBlocks) {
        int minSX = SectionPos.blockToSectionCoord(origin.getX() - HORIZONTAL_RANGE);
        int maxSX = SectionPos.blockToSectionCoord(origin.getX() + HORIZONTAL_RANGE);
        int minSY = SectionPos.blockToSectionCoord(Math.max(level.getMinY(), origin.getY() - VERTICAL_RANGE));
        int maxSY = SectionPos.blockToSectionCoord(Math.min(level.getMaxY(), origin.getY() + VERTICAL_RANGE));
        int minSZ = SectionPos.blockToSectionCoord(origin.getZ() - HORIZONTAL_RANGE);
        int maxSZ = SectionPos.blockToSectionCoord(origin.getZ() + HORIZONTAL_RANGE);
        Snapshot snapshot = new Snapshot(origin.immutable(), maxBlocks, minSX, minSY, minSZ, maxSX - minSX + 1, maxSY - minSY + 1, maxSZ - minSZ + 1);
        for (int sx = minSX; sx <= maxSX; sx++) {
            for (int sz = minSZ; sz <= maxSZ; sz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(sx, sz);
                if (chunk == null) {
                    continue;
                }
                for (int sy = minSY; sy <= maxSY; sy++) {
                    int index = chunk.getSectionIndexFromSectionY(sy);
                    if (index < 0 || index >= chunk.getSections().length) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(index);
                    snapshot.sections.set(((sx - minSX) * snapshot.sizeY + (sy - minSY)) * snapshot.sizeZ + (sz - minSZ), section.getStates().copy());
                }
            }
        }
        return snapshot;
    }

    public static Result floodFill(Snapshot snapshot) {
        LongOpenHashSet volume = new LongOpenHashSet();
        LongOpenHashSet boundary = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        BlockPos origin = snapshot.origin;
        boundary.add(origin.asLong());
        for (Direction direction : Direction.values()) {
            BlockPos start = origin.relative(direction);
            BlockState state = snapshot.get(start.getX(), start.getY(), start.getZ());
            if (state == null) {
                return Result.unsealed();
            }
            if (!seals(state) && volume.add(start.asLong())) {
                queue.enqueue(start.asLong());
            } else if (seals(state)) {
                boundary.add(start.asLong());
            }
        }
        if (volume.isEmpty()) {
            return Result.unsealed();
        }
        int panes = 0;
        int glass = 0;
        int solid = 0;
        int growLights = 0;
        int humidifiers = 0;
        int dehumidifiers = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty()) {
            long current = queue.dequeueLong();
            int cx = BlockPos.getX(current);
            int cy = BlockPos.getY(current);
            int cz = BlockPos.getZ(current);
            for (Direction direction : Direction.values()) {
                cursor.set(cx + direction.getStepX(), cy + direction.getStepY(), cz + direction.getStepZ());
                long key = cursor.asLong();
                if (volume.contains(key) || boundary.contains(key)) {
                    continue;
                }
                if (!snapshot.inRange(cursor.getX(), cursor.getY(), cursor.getZ())) {
                    return Result.leaked(volume, boundary);
                }
                BlockState state = snapshot.get(cursor.getX(), cursor.getY(), cursor.getZ());
                if (state == null) {
                    return Result.leaked(volume, boundary);
                }
                if (seals(state)) {
                    boundary.add(key);
                    if (state.getBlock() instanceof GreenhousePaneBlock) {
                        panes++;
                    } else if (state.is(Tags.Blocks.GLASS_BLOCKS)) {
                        glass++;
                    } else {
                        solid++;
                    }
                    if (state.getBlock() instanceof GrowLightBlock) {
                        growLights++;
                    } else if (state.getBlock() instanceof HumidityDeviceBlock device) {
                        if (device.humidifies()) {
                            humidifiers++;
                        } else {
                            dehumidifiers++;
                        }
                    }
                } else {
                    volume.add(key);
                    if (volume.size() > snapshot.maxBlocks) {
                        return Result.leaked(volume, boundary);
                    }
                    queue.enqueue(key);
                }
            }
        }
        int sealingFaces = panes + glass + solid;
        float insulation = sealingFaces == 0 ? 0f : (panes * 1.0f + solid * 0.8f + glass * 0.45f) / sealingFaces;
        return new Result(true, volume, boundary, insulation, growLights, humidifiers, dehumidifiers);
    }

    public static boolean seals(BlockState state) {
        if (state.getBlock() instanceof GreenhousePaneBlock) {
            return true;
        }
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock) {
            return !state.getValue(BlockStateProperties.OPEN);
        }
        return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }
}
