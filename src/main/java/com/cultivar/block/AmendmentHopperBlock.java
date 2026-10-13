package com.cultivar.block;

import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModParticles;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class AmendmentHopperBlock extends Block {
    public enum Kind implements StringRepresentable {
        LIME, SULFUR;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final MapCodec<AmendmentHopperBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StringRepresentable.fromEnum(Kind::values).fieldOf("kind").forGetter(b -> b.kind),
            propertiesCodec()
    ).apply(i, AmendmentHopperBlock::new));
    public static final int MAX_LEVEL = 8;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVEL);
    public static final int RADIUS = 2;
    public static final int DOSES_PER_ITEM = 1;
    public static final float LIME_TARGET = 7.2f;
    public static final float SULFUR_TARGET = 5.6f;

    private final Kind kind;

    public AmendmentHopperBlock(Kind kind, Properties properties) {
        super(properties.randomTicks());
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<AmendmentHopperBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, Facing.FACING);
    }

    private Item powder() {
        return kind == Kind.LIME ? ModItems.LIME_POWDER.get() : ModItems.SULFUR_POWDER.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!stack.is(powder())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        int current = state.getValue(LEVEL);
        if (current >= MAX_LEVEL) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(LEVEL, Math.min(MAX_LEVEL, current + DOSES_PER_ITEM)), Block.UPDATE_ALL);
            stack.consume(1, player);
            player.sendOverlayMessage(Texts.tr(Texts.MSG_HOPPER_LEVEL, current + DOSES_PER_ITEM, MAX_LEVEL));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_HOPPER_LEVEL, state.getValue(LEVEL), MAX_LEVEL));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(LEVEL);
        if (charge <= 0) {
            return;
        }
        boolean dosed = false;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -2, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
            if (level.getBlockEntity(p) instanceof ManagedSoilBlockEntity soil) {
                if (kind == Kind.LIME && soil.ph() < LIME_TARGET) {
                    soil.setPh(Math.min(LIME_TARGET, soil.ph() + 0.1f));
                    soil.markUpdated();
                    dosed = true;
                } else if (kind == Kind.SULFUR && soil.ph() > SULFUR_TARGET) {
                    soil.setPh(Math.max(SULFUR_TARGET, soil.ph() - 0.1f));
                    soil.markUpdated();
                    dosed = true;
                }
            }
        }
        if (dosed) {
            level.setBlock(pos, state.setValue(LEVEL, charge - 1), Block.UPDATE_ALL);
            level.sendParticles(ModParticles.SOIL_CRUMB.get(), pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 6, 1.0, 0.1, 1.0, 0.01);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return Facing.placed(defaultBlockState(), context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return Facing.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return Facing.mirror(state, mirror);
    }
}
