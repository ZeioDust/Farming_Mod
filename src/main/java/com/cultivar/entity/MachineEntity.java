package com.cultivar.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class MachineEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_WORKING = SynchedEntityData.defineId(MachineEntity.class, EntityDataSerializers.BOOLEAN);

    protected MachineEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_WORKING, false);
    }

    public boolean isWorking() {
        return entityData.get(DATA_WORKING);
    }

    protected void setWorking(boolean working) {
        entityData.set(DATA_WORKING, working);
    }

    protected boolean moveToward(Vec3 target, double speed) {
        Vec3 delta = target.subtract(position());
        double distance = delta.length();
        if (distance < 0.25) {
            setDeltaMovement(Vec3.ZERO);
            return true;
        }
        Vec3 step = delta.scale(Math.min(speed, distance) / distance);
        setDeltaMovement(step);
        move(MoverType.SELF, step);
        setYRot((float) (Math.atan2(-step.x, step.z) * (180.0 / Math.PI)));
        return false;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isRemoved()) {
            return false;
        }
        onSwitchedOff(level);
        discard();
        return true;
    }

    protected abstract void onSwitchedOff(ServerLevel level);
}
