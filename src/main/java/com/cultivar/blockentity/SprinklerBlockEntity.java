package com.cultivar.blockentity;

import com.cultivar.block.IrrigationLineBlock;
import com.cultivar.block.ManagedSoilBlock;
import com.cultivar.entity.WaterJetEntity;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SprinklerBlockEntity extends SyncedBlockEntity implements TickingMachine {
    public static final int RADIUS = 4;
    public static final int INTERVAL = 100;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPRINKLER.get(), pos, state);
    }

    public static boolean isFed(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos n = pos.relative(direction);
            if (level.getFluidState(n).is(FluidTags.WATER)) {
                return true;
            }
            BlockState state = level.getBlockState(n);
            if (state.getBlock() instanceof IrrigationLineBlock && state.getValue(IrrigationLineBlock.FED)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if ((level.getGameTime() + pos.asLong()) % INTERVAL != 0 || !isFed(level, pos)) {
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -2, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
            BlockState soil = level.getBlockState(p);
            if (soil.getBlock() instanceof ManagedSoilBlock || soil.getBlock() instanceof FarmlandBlock) {
                if (soil.getValue(FarmlandBlock.MOISTURE) < 7) {
                    ManagedSoilBlock.soak(level, p.immutable(), soil);
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            WaterJetEntity jet = ModEntities.WATER_JET.get().create(level, EntitySpawnReason.TRIGGERED);
            if (jet != null) {
                double angle = level.getRandom().nextDouble() * Math.PI * 2;
                jet.setPos(pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5);
                jet.launch(new Vec3(Math.cos(angle) * 0.22, 0.28, Math.sin(angle) * 0.22));
                level.addFreshEntity(jet);
            }
        }
        level.playSound(null, pos, ModSounds.SPRINKLER_SPRAY.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
    }
}
