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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class EffectEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(EffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(EffectEntity.class, EntityDataSerializers.INT);

    protected EffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COLOR, 0xFFFFFFFF);
        builder.define(DATA_LIFETIME, defaultLifetime());
    }

    protected abstract int defaultLifetime();

    protected abstract void clientEffects();

    public int color() {
        return entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        entityData.set(DATA_COLOR, color);
    }

    public int lifetime() {
        return entityData.get(DATA_LIFETIME);
    }

    public void setLifetime(int lifetime) {
        entityData.set(DATA_LIFETIME, lifetime);
    }

    public float age(float partialTick) {
        return Math.min(1f, (tickCount + partialTick) / Math.max(1, lifetime()));
    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, getDeltaMovement());
        if (level().isClientSide()) {
            clientEffects();
        } else if (tickCount >= lifetime()) {
            discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setColor(input.getIntOr("color", 0xFFFFFFFF));
        setLifetime(input.getIntOr("lifetime", defaultLifetime()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("color", color());
        output.putInt("lifetime", lifetime());
    }
}
