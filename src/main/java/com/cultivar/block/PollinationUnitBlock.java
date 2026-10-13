package com.cultivar.block;

import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.component.PollenLoad;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PollinationUnitBlock extends MachineBlock {
    public static final MapCodec<PollinationUnitBlock> CODEC = simpleCodec(PollinationUnitBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public PollinationUnitBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<PollinationUnitBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.POLLINATION_UNIT.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PollinationUnitBlockEntity unit)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (stack.is(ModItems.POLLINATION_BRUSH.get())) {
            PollenLoad pollen = stack.get(ModDataComponents.POLLEN.get());
            if (pollen == null) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!level.isClientSide()) {
                if (!pollen.dimension().equals(level.dimension().identifier())) {
                    player.sendOverlayMessage(Texts.tr(Texts.MSG_POLLEN_DIMENSION));
                    return InteractionResult.FAIL;
                }
                if (!unit.inRange(pollen.source())) {
                    player.sendOverlayMessage(Texts.tr(Texts.MSG_UNIT_OUT_OF_RANGE, PollinationUnitBlockEntity.RADIUS));
                    return InteractionResult.FAIL;
                }
                boolean armed = unit.configure(pollen.source());
                stack.remove(ModDataComponents.POLLEN.get());
                player.sendOverlayMessage(armed ? Texts.tr(Texts.MSG_UNIT_TARGET, pollen.source().toShortString())
                        : Texts.tr(Texts.MSG_UNIT_SOURCE, pollen.source().toShortString()));
            }
            return InteractionResult.SUCCESS;
        }
        Component fuelName = stack.getHoverName();
        if (!level.isClientSide() && unit.addFuel(stack)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_MACHINE_LOADED, fuelName));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PollinationUnitBlockEntity unit) {
            if (player.isSecondaryUseActive()) {
                unit.clearCross();
                player.sendOverlayMessage(Texts.tr(Texts.MSG_UNIT_CLEARED));
            } else {
                Component none = Texts.tr(Texts.NONE);
                player.sendSystemMessage(Texts.tr(Texts.MSG_UNIT_STATUS, Texts.tr(unit.isActive() ? Texts.MSG_ACTIVE : Texts.MSG_IDLE),
                        unit.fuel() / 20, unit.source() != null ? Component.literal(unit.source().toShortString()) : none,
                        unit.target() != null ? Component.literal(unit.target().toShortString()) : none));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
