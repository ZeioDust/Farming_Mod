package com.cultivar.block;

import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModTags;
import com.cultivar.world.HarvestLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CultivarCropBlock extends Block implements EntityBlock, BonemealableBlock {
    public static final int MAX_STAGE = 6;
    public static final int FLOWERING = 4;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    public static final IntegerProperty VIGOR = IntegerProperty.create("vigor", 0, 2);
    public static final IntegerProperty DENSITY = IntegerProperty.create("density", 0, 2);
    public static final BooleanProperty BLIGHTED = BooleanProperty.create("blighted");
    public static final BooleanProperty BAGGED = BooleanProperty.create("bagged");

    private static final Codec<CropSpecies> SPECIES_CODEC = Codec.STRING.xmap(
            s -> CropSpecies.valueOf(s.toUpperCase(Locale.ROOT)), CropSpecies::id);
    public static final MapCodec<CultivarCropBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SPECIES_CODEC.fieldOf("species").forGetter(b -> b.species),
            propertiesCodec()
    ).apply(i, CultivarCropBlock::new));

    private static final VoxelShape[] SHAPES = new VoxelShape[MAX_STAGE + 1];

    static {
        for (int stage = 0; stage <= MAX_STAGE; stage++) {
            SHAPES[stage] = Block.box(1, 0, 1, 15, Math.min(16, 3 + stage * 2.2), 15);
        }
    }

    private final @Nullable CropSpecies species;

    public CultivarCropBlock(@Nullable CropSpecies species, Properties properties) {
        super(properties);
        this.species = species;
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0).setValue(VIGOR, 1).setValue(DENSITY, 1)
                .setValue(BLIGHTED, false).setValue(BAGGED, false));
    }

    @Override
    protected MapCodec<? extends CultivarCropBlock> codec() {
        return CODEC;
    }

    public @Nullable CropSpecies species() {
        return species;
    }

    public static int stage(BlockState state) {
        return state.hasProperty(STAGE) ? state.getValue(STAGE) : 0;
    }

    public static int vigorFor(float growthRate) {
        return growthRate >= 1.3f ? 2 : growthRate < 0.85f ? 0 : 1;
    }

    public static int densityFor(float yield) {
        return yield >= 1.35f ? 2 : yield < 0.75f ? 0 : 1;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, VIGOR, DENSITY, BLIGHTED, BAGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[stage(state)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModTags.Blocks.CROP_SOIL);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropBlockEntity(pos, state);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (be instanceof CropBlockEntity crop) {
            HarvestLogic.Result result = HarvestLogic.harvest(params.getLevel(), crop, state, breaker instanceof ServerPlayer p ? p : null);
            drops.addAll(result.all());
        }
        if (state.getValue(BAGGED)) {
            drops.add(new ItemStack(ModItems.ISOLATION_BAG.get()));
        }
        return drops;
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                                  net.minecraft.world.entity.player.Player player,
                                                                  net.minecraft.world.phys.BlockHitResult hit) {
        if (state.getValue(BAGGED) && player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(BAGGED, false), Block.UPDATE_ALL);
                if (!player.getInventory().add(new ItemStack(ModItems.ISOLATION_BAG.get()))) {
                    Block.popResource(level, pos, new ItemStack(ModItems.ISOLATION_BAG.get()));
                }
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return species != null ? new ItemStack(ModItems.seedPacket(species)) : ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BLIGHTED)) {
            if (random.nextInt(3) == 0) {
                level.addParticle(ModParticles.SPORE_SPECK.get(), pos.getX() + random.nextDouble(), pos.getY() + 0.3 + random.nextDouble() * 0.6,
                        pos.getZ() + random.nextDouble(), 0, 0.01, 0);
            }
        } else if (stage(state) == FLOWERING && !state.getValue(BAGGED) && random.nextInt(4) == 0) {
            level.addParticle(ModParticles.POLLEN_MOTE.get(), pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.6 + random.nextDouble() * 0.4,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.005, 0);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return stage(state) < MAX_STAGE && !state.getValue(BLIGHTED);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            crop.boostGrowth(level, 800f);
        }
    }
}
