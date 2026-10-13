package com.cultivar.block;

import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.util.Texts;
import com.cultivar.world.Amendment;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ManagedSoilBlock extends Block implements EntityBlock {
    public static final MapCodec<ManagedSoilBlock> CODEC = simpleCodec(ManagedSoilBlock::new);
    public static final IntegerProperty MOISTURE = FarmlandBlock.MOISTURE;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public ManagedSoilBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override
    protected MapCodec<? extends ManagedSoilBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManagedSoilBlockEntity(pos, state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (isHydrated(level, pos)) {
            if (moisture < 7) {
                level.setBlock(pos, state.setValue(MOISTURE, 7), Block.UPDATE_CLIENTS);
            }
        } else if (moisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isHydrated(LevelReader level, BlockPos pos) {
        if (level instanceof Level l && l.isRainingAt(pos.above())) {
            return true;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) {
                return true;
            }
        }
        return IrrigationLineBlock.isIrrigatedNear(level, pos);
    }

    public static void soak(Level level, BlockPos pos, BlockState state) {
        if (state.hasProperty(MOISTURE) && state.getValue(MOISTURE) < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        Amendment amendment = Amendment.of(stack);
        if (amendment == null || !(level.getBlockEntity(pos) instanceof ManagedSoilBlockEntity soil)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            applyAmendment(level, pos, player, stack, amendment, soil);
        }
        return InteractionResult.SUCCESS;
    }

    public static void applyAmendment(Level level, BlockPos pos, Player player, ItemStack stack, Amendment amendment, ManagedSoilBlockEntity soil) {
        amendment.apply(soil);
        stack.consume(1, player);
        level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
        player.sendOverlayMessage(Texts.tr(Texts.MSG_AMENDED, fmt(soil.get(com.cultivar.data.Nutrient.NITROGEN)),
                fmt(soil.get(com.cultivar.data.Nutrient.PHOSPHORUS)), fmt(soil.get(com.cultivar.data.Nutrient.POTASSIUM)),
                String.format(java.util.Locale.ROOT, "%.2f", soil.ph())));
    }

    private static String fmt(float value) {
        return String.valueOf(Math.round(value));
    }
}
