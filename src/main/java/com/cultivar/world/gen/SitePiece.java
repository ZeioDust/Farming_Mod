package com.cultivar.world.gen;

import com.cultivar.block.AmendmentHopperBlock;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.TrialMarkerBlockEntity;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class SitePiece extends StructurePiece {
    private static final int CLEAR_HEIGHT = 6;
    private static final int FILL_DEPTH = 6;
    private final SiteKind kind;

    public SitePiece(SiteKind kind, BlockPos origin) {
        super(ModWorldgen.SITE_PIECE.get(), 0, new BoundingBox(origin.getX(), origin.getY() - FILL_DEPTH, origin.getZ(),
                origin.getX() + kind.width() - 1, origin.getY() + Math.max(kind.height(), CLEAR_HEIGHT), origin.getZ() + kind.depth() - 1));
        this.kind = kind;
    }

    public SitePiece(CompoundTag tag) {
        super(ModWorldgen.SITE_PIECE.get(), tag);
        this.kind = SiteKind.byId(tag.getStringOr("kind", SiteKind.ABANDONED_TRIAL_FIELD.id()));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("kind", kind.id());
    }

    private int groundY() {
        return boundingBox.minY() + FILL_DEPTH;
    }

    private void set(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, BlockState state) {
        BlockPos pos = new BlockPos(boundingBox.minX() + lx, groundY() + ly, boundingBox.minZ() + lz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    private BlockPos world(int lx, int ly, int lz) {
        return new BlockPos(boundingBox.minX() + lx, groundY() + ly, boundingBox.minZ() + lz);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource chunkRandom,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        RandomSource random = RandomSource.create(net.minecraft.util.Mth.getSeed(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ()));
        prepareFootprint(level, chunkBox);
        switch (kind) {
            case ABANDONED_TRIAL_FIELD -> trialField(level, chunkBox, random);
            case RUINED_GREENHOUSE -> ruinedGreenhouse(level, chunkBox, random);
            case OLD_SEED_STORE -> seedStore(level, chunkBox, random);
            case COMPOST_YARD -> compostYard(level, chunkBox, random);
            case BLIGHT_SCAR -> blightScar(level, chunkBox, random);
            case CANOPY_PLATFORM -> canopyPlatform(level, chunkBox, random);
            case WILD_STAND -> wildStand(level, chunkBox, random);
            case CANOPY_BREACH -> canopyBreach(level, chunkBox, random);
        }
    }

    private void prepareFootprint(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState support = isCanopySite() ? ModBlocks.CANOPY_BARK.get().defaultBlockState() : Blocks.DIRT.defaultBlockState();
        for (int lx = 0; lx < kind.width(); lx++) {
            for (int lz = 0; lz < kind.depth(); lz++) {
                for (int ly = 1; ly <= CLEAR_HEIGHT; ly++) {
                    set(level, chunkBox, lx, ly, lz, Blocks.AIR.defaultBlockState());
                }
                for (int ly = -1; ly >= -FILL_DEPTH; ly--) {
                    BlockPos pos = world(lx, ly, lz);
                    if (!chunkBox.isInside(pos) || !level.getBlockState(pos).canBeReplaced()) {
                        break;
                    }
                    level.setBlock(pos, support, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private boolean isCanopySite() {
        return kind == SiteKind.CANOPY_PLATFORM || kind == SiteKind.WILD_STAND;
    }

    private void chest(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, int lx, int ly, int lz) {
        BlockPos pos = world(lx, ly, lz);
        if (chunkBox.isInside(pos)) {
            createChest(level, chunkBox, random, pos, kind.lootTable(), null);
        }
    }

    private static BlockState crop(CropSpecies species, int stage) {
        return ModBlocks.crop(species).defaultBlockState().setValue(CultivarCropBlock.STAGE, stage);
    }

    private static CropSpecies randomSpecies(RandomSource random) {
        return CropSpecies.VALUES[random.nextInt(CropSpecies.VALUES.length)];
    }

    private void floor(WorldGenLevel level, BoundingBox chunkBox, BlockState state) {
        for (int lx = 0; lx < kind.width(); lx++) {
            for (int lz = 0; lz < kind.depth(); lz++) {
                set(level, chunkBox, lx, 0, lz, state);
            }
        }
    }

    private void trialField(WorldGenLevel level, BoundingBox box, RandomSource random) {
        floor(level, box, Blocks.COARSE_DIRT.defaultBlockState());
        int plot = 1;
        for (int lz = 1; lz < kind.depth() - 1; lz += 2) {
            CropSpecies species = randomSpecies(random);
            for (int lx = 1; lx < kind.width() - 1; lx++) {
                set(level, box, lx, 0, lz, ModBlocks.MANAGED_SOIL.get().defaultBlockState());
                if (random.nextFloat() < 0.7f) {
                    set(level, box, lx, 1, lz, crop(species, 3 + random.nextInt(4)));
                }
            }
            set(level, box, 0, 1, lz, ModBlocks.TRIAL_MARKER.get().defaultBlockState());
            if (level.getBlockEntity(world(0, 1, lz)) instanceof TrialMarkerBlockEntity marker && box.isInside(world(0, 1, lz))) {
                marker.set("Plot", plot);
            }
            plot++;
        }
        for (int i = 0; i < kind.width(); i++) {
            if (random.nextFloat() < 0.6f) {
                set(level, box, i, 1, 0, Blocks.OAK_FENCE.defaultBlockState());
            }
            if (random.nextFloat() < 0.6f) {
                set(level, box, i, 1, kind.depth() - 1, Blocks.OAK_FENCE.defaultBlockState());
            }
        }
        chest(level, box, random, kind.width() - 1, 1, kind.depth() - 1);
    }

    private void ruinedGreenhouse(WorldGenLevel level, BoundingBox box, RandomSource random) {
        floor(level, box, Blocks.STONE_BRICKS.defaultBlockState());
        int w = kind.width() - 1;
        int d = kind.depth() - 1;
        for (int lx = 1; lx < w; lx++) {
            for (int lz = 1; lz < d; lz++) {
                set(level, box, lx, 0, lz, ModBlocks.MANAGED_SOIL.get().defaultBlockState());
                if (random.nextFloat() < 0.35f) {
                    set(level, box, lx, 1, lz, crop(randomSpecies(random), 1 + random.nextInt(6)));
                }
            }
        }
        BlockState pane = ModBlocks.GREENHOUSE_PANE.get().defaultBlockState();
        for (int ly = 1; ly <= 4; ly++) {
            for (int i = 0; i <= w; i++) {
                if (random.nextFloat() > 0.3f) {
                    set(level, box, i, ly, 0, pane);
                }
                if (random.nextFloat() > 0.3f) {
                    set(level, box, i, ly, d, pane);
                }
                if (random.nextFloat() > 0.3f) {
                    set(level, box, 0, ly, i, pane);
                }
                if (random.nextFloat() > 0.3f) {
                    set(level, box, w, ly, i, pane);
                }
            }
        }
        for (int lx = 0; lx <= w; lx++) {
            for (int lz = 0; lz <= d; lz++) {
                if (random.nextFloat() > 0.55f) {
                    set(level, box, lx, 5, lz, pane);
                }
            }
        }
        set(level, box, w / 2, 1, 1, ModBlocks.IRRIGATION_LINE.get().defaultBlockState());
        set(level, box, w / 2 + 1, 1, 1, ModBlocks.IRRIGATION_LINE.get().defaultBlockState());
        chest(level, box, random, 1, 1, d - 1);
    }

    private void seedStore(WorldGenLevel level, BoundingBox box, RandomSource random) {
        floor(level, box, Blocks.COBBLESTONE.defaultBlockState());
        int w = kind.width() - 1;
        int d = kind.depth() - 1;
        for (int ly = 1; ly <= 4; ly++) {
            for (int i = 0; i <= w; i++) {
                set(level, box, i, ly, 0, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, box, i, ly, d, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, box, 0, ly, i, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, box, w, ly, i, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
        for (int lx = 0; lx <= w; lx++) {
            for (int lz = 0; lz <= d; lz++) {
                set(level, box, lx, 5, lz, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }
        set(level, box, w / 2, 1, 0, Blocks.AIR.defaultBlockState());
        set(level, box, w / 2, 2, 0, Blocks.AIR.defaultBlockState());
        for (int lx = 1; lx < w; lx++) {
            set(level, box, lx, 1, d - 1, ModBlocks.SEED_VAULT_DRAWER.get().defaultBlockState());
            if (random.nextBoolean()) {
                set(level, box, lx, 2, d - 1, ModBlocks.SEED_VAULT_DRAWER.get().defaultBlockState());
            }
        }
        set(level, box, 1, 1, 1, Blocks.COBWEB.defaultBlockState());
        chest(level, box, random, w - 1, 1, 1);
    }

    private void compostYard(WorldGenLevel level, BoundingBox box, RandomSource random) {
        floor(level, box, Blocks.COARSE_DIRT.defaultBlockState());
        for (int i = 0; i < 3; i++) {
            set(level, box, 2 + i * 2, 1, 2, ModBlocks.COMPOST_BIN.get().defaultBlockState());
        }
        for (int lx = 1; lx < kind.width() - 1; lx++) {
            for (int lz = 4; lz < kind.depth() - 1; lz++) {
                if (random.nextFloat() < 0.3f) {
                    set(level, box, lx, 1, lz, Blocks.HAY_BLOCK.defaultBlockState());
                } else if (random.nextFloat() < 0.15f) {
                    set(level, box, lx, 0, lz, Blocks.PODZOL.defaultBlockState());
                }
            }
        }
        set(level, box, 1, 1, kind.depth() - 2, ModBlocks.LIME_HOPPER.get().defaultBlockState()
                .setValue(AmendmentHopperBlock.LEVEL, 2 + random.nextInt(4)));
        chest(level, box, random, kind.width() - 2, 1, 1);
    }

    private void blightScar(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int cx = kind.width() / 2;
        int cz = kind.depth() / 2;
        CropSpecies species = randomSpecies(random);
        for (int lx = 0; lx < kind.width(); lx++) {
            for (int lz = 0; lz < kind.depth(); lz++) {
                int dx = lx - cx;
                int dz = lz - cz;
                if (dx * dx + dz * dz > cx * cx) {
                    continue;
                }
                set(level, box, lx, 0, lz, ModBlocks.DEPLETED_SOIL.get().defaultBlockState());
                float roll = random.nextFloat();
                if (roll < 0.25f) {
                    set(level, box, lx, 1, lz, crop(species, 4 + random.nextInt(3)).setValue(CultivarCropBlock.BLIGHTED, true));
                } else if (roll < 0.4f) {
                    set(level, box, lx, 1, lz, Blocks.DEAD_BUSH.defaultBlockState());
                }
            }
        }
        chest(level, box, random, cx, 1, cz);
    }

    private void canopyPlatform(WorldGenLevel level, BoundingBox box, RandomSource random) {
        BlockState bark = ModBlocks.CANOPY_BARK.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leaf = ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState();
        int r = kind.width() / 2;
        int deck = 3;
        for (int lx = 0; lx < kind.width(); lx++) {
            for (int lz = 0; lz < kind.depth(); lz++) {
                int dx = lx - r;
                int dz = lz - r;
                if (dx * dx + dz * dz <= r * r) {
                    set(level, box, lx, deck, lz, leaf);
                    if (dx * dx + dz * dz >= (r - 1) * (r - 1) && (lx + lz) % 2 == 0) {
                        set(level, box, lx, deck + 1, lz, Blocks.SPRUCE_FENCE.defaultBlockState());
                    }
                }
            }
        }
        for (int[] post : new int[][]{{r - 3, r - 3}, {r + 3, r - 3}, {r - 3, r + 3}, {r + 3, r + 3}}) {
            for (int ly = 0; ly < deck; ly++) {
                set(level, box, post[0], ly, post[1], bark);
            }
            set(level, box, post[0], deck - 1, post[1] + 1, ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState());
        }
        set(level, box, r - 1, deck + 1, r, Blocks.LADDER.defaultBlockState());
        chest(level, box, random, r, deck + 1, r);
    }

    private void wildStand(WorldGenLevel level, BoundingBox box, RandomSource random) {
        BlockState leaf = ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState();
        floor(level, box, leaf);
        CropSpecies first = randomSpecies(random);
        CropSpecies second = randomSpecies(random);
        for (int lx = 1; lx < kind.width() - 1; lx++) {
            for (int lz = 1; lz < kind.depth() - 1; lz++) {
                if (random.nextFloat() < 0.6f) {
                    set(level, box, lx, 1, lz, crop(lx < kind.width() / 2 ? first : second, 3 + random.nextInt(4)));
                }
            }
        }
        for (int i = 0; i < kind.width(); i += 2) {
            set(level, box, i, 1, 0, ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState());
            set(level, box, i, 1, kind.depth() - 1, ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState());
        }
        chest(level, box, random, 0, 1, kind.depth() / 2);
    }

    private void canopyBreach(WorldGenLevel level, BoundingBox box, RandomSource random) {
        floor(level, box, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        BlockState bark = ModBlocks.CANOPY_BARK.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        int c = kind.width() / 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    set(level, box, c + dx, 1, c + dz, ModBlocks.CANOPY_PORTAL_FRAME.get().defaultBlockState());
                }
            }
        }
        for (int[] corner : new int[][]{{1, 1}, {kind.width() - 2, 1}, {1, kind.depth() - 2}, {kind.width() - 2, kind.depth() - 2}}) {
            for (int ly = 1; ly <= 4; ly++) {
                set(level, box, corner[0], ly, corner[1], bark);
            }
            set(level, box, corner[0], 5, corner[1], ModBlocks.CANOPY_LEAFMASS.get().defaultBlockState());
            set(level, box, corner[0] + (corner[0] < c ? 1 : -1), 4, corner[1], ModBlocks.WILD_VINE_BLOCK.get().defaultBlockState());
        }
        if (random.nextFloat() < 0.5f) {
            chest(level, box, random, kind.width() - 1, 1, c);
        }
    }
}
