package com.cultivar.blockentity;

import com.cultivar.block.PollinationUnitBlock;
import com.cultivar.entity.PollinatorDroneEntity;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModTags;
import com.cultivar.world.LevelState;
import com.cultivar.world.PollinationSystem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class PollinationUnitBlockEntity extends SyncedBlockEntity implements TickingMachine {
    public static final int RADIUS = 6;
    public static final int FUEL_PER_ITEM = 1200;
    public static final int DISPATCH_INTERVAL = 40;
    public static final int DRONE_TIMEOUT = PollinatorDroneEntity.MAX_AGE + 100;

    private int fuel;
    private @Nullable BlockPos source;
    private @Nullable BlockPos target;
    private boolean active;
    private @Nullable UUID drone;
    private long droneSentAt;

    public PollinationUnitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLLINATION_UNIT.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public int fuel() {
        return fuel;
    }

    public @Nullable BlockPos source() {
        return source;
    }

    public @Nullable BlockPos target() {
        return target;
    }

    public boolean addFuel(ItemStack stack) {
        if (!stack.is(ModTags.Items.BURN_FUEL)) {
            return false;
        }
        fuel += FUEL_PER_ITEM;
        stack.shrink(1);
        markUpdated();
        return true;
    }

    public boolean inRange(BlockPos plant) {
        return plant.distSqr(worldPosition) <= (double) RADIUS * RADIUS;
    }

    public boolean configure(BlockPos plant) {
        if (source == null || target != null) {
            source = plant.immutable();
            target = null;
            markUpdated();
            return false;
        }
        target = plant.immutable();
        markUpdated();
        return true;
    }

    public void clearCross() {
        source = null;
        target = null;
        markUpdated();
    }

    public void droneReturned() {
        drone = null;
        setChanged();
    }

    private boolean droneOut(ServerLevel level) {
        if (drone == null) {
            return false;
        }
        long age = level.getGameTime() - droneSentAt;
        if (age > DRONE_TIMEOUT || (age > 20 && level.getEntity(drone) == null)) {
            drone = null;
            setChanged();
            return false;
        }
        return true;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean nowActive = powered || fuel > 0;
        if (fuel > 0 && !powered) {
            fuel--;
        }
        if (nowActive != active) {
            active = nowActive;
            LevelState.get(level).setPollinationUnitActive(pos, active);
            if (state.hasProperty(PollinationUnitBlock.ACTIVE)) {
                level.setBlock(pos, state.setValue(PollinationUnitBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
            }
            markUpdated();
        }
        if (!active || level.getGameTime() % DISPATCH_INTERVAL != 0 || droneOut(level)) {
            return;
        }
        if (source != null && target != null) {
            if (!inRange(source) || !inRange(target)) {
                clearCross();
            } else if (dispatchIfReady(level, pos, source, target, true)) {
                clearCross();
                return;
            }
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LevelState index = LevelState.get(level);
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (!index.hasCrop(cursor.asLong()) || cursor.equals(target) || !level.isLoaded(cursor)
                            || !(level.getBlockEntity(cursor) instanceof CropBlockEntity crop)) {
                        continue;
                    }
                    if (crop.isFlowering() && !crop.isPollinated() && !crop.isBagged() && !crop.isGrafted()) {
                        CropBlockEntity donor = nearestDonor(level, index, crop);
                        if (donor != null && dispatchIfReady(level, pos, donor.getBlockPos(), crop.getBlockPos(), false)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private @Nullable CropBlockEntity nearestDonor(ServerLevel level, LevelState index, CropBlockEntity target) {
        CropBlockEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos t = target.getBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    cursor.set(t.getX() + dx, t.getY() + dy, t.getZ() + dz);
                    if (index.hasCrop(cursor.asLong()) && level.isLoaded(cursor) && level.getBlockEntity(cursor) instanceof CropBlockEntity donor
                            && PollinationSystem.isEligibleDonor(donor, target)) {
                        double d = cursor.distSqr(t);
                        if (d < bestDistance) {
                            bestDistance = d;
                            best = donor;
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean dispatchIfReady(ServerLevel level, BlockPos home, BlockPos donorPos, BlockPos targetPos, boolean controlled) {
        if (!level.isLoaded(donorPos) || !level.isLoaded(targetPos)) {
            return false;
        }
        if (!(level.getBlockEntity(donorPos) instanceof CropBlockEntity donor) || !(level.getBlockEntity(targetPos) instanceof CropBlockEntity target)
                || !donor.isFlowering() || !target.isFlowering() || donor.species() != target.species()) {
            return false;
        }
        PollinatorDroneEntity entity = ModEntities.POLLINATOR_DRONE.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return false;
        }
        entity.assign(home, donorPos, targetPos, controlled);
        level.addFreshEntity(entity);
        drone = entity.getUUID();
        droneSentAt = level.getGameTime();
        setChanged();
        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            LevelState.get(serverLevel).setPollinationUnitActive(worldPosition, false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && active) {
            LevelState.get(serverLevel).setPollinationUnitActive(worldPosition, true);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("fuel", fuel);
        output.storeNullable("source", BlockPos.CODEC, source);
        output.storeNullable("target", BlockPos.CODEC, target);
        output.putBoolean("active", active);
        output.storeNullable("drone", UUIDUtil.CODEC, drone);
        output.putLong("drone_sent_at", droneSentAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fuel = input.getIntOr("fuel", 0);
        source = input.read("source", BlockPos.CODEC).orElse(null);
        target = input.read("target", BlockPos.CODEC).orElse(null);
        active = input.getBooleanOr("active", false);
        drone = input.read("drone", UUIDUtil.CODEC).orElse(null);
        droneSentAt = input.getLongOr("drone_sent_at", 0L);
    }
}
