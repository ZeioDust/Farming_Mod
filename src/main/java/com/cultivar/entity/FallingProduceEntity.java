package com.cultivar.entity;

import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class FallingProduceEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(FallingProduceEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final int MAX_AGE = 200;

    public FallingProduceEntity(EntityType<? extends FallingProduceEntity> type, Level level) {
        super(type, level);
    }

    public static void spawn(Level level, BlockPos pos, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return;
        }
        FallingProduceEntity entity = ModEntities.FALLING_PRODUCE.get().create(serverLevel, EntitySpawnReason.EVENT);
        if (entity == null) {
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
            return;
        }
        entity.setItem(stack);
        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
        entity.setDeltaMovement((serverLevel.getRandom().nextDouble() - 0.5) * 0.12, 0.18, (serverLevel.getRandom().nextDouble() - 0.5) * 0.12);
        serverLevel.addFreshEntity(entity);
    }

    public ItemStack getItem() {
        return entityData.get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        entityData.set(DATA_ITEM, stack.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        super.tick();
        applyGravity();
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.96, 0.98, 0.96));
        if (level() instanceof ServerLevel serverLevel && (onGround() || tickCount > MAX_AGE)) {
            land(serverLevel);
        }
    }

    private void land(ServerLevel level) {
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            ItemEntity item = new ItemEntity(level, getX(), getY() + 0.1, getZ(), stack.copy(), 0, 0.05, 0);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
            level.playSound(null, getX(), getY(), getZ(), ModSounds.HARVEST_CLATTER.get(), SoundSource.BLOCKS, 0.5f,
                    0.9f + level.getRandom().nextFloat() * 0.2f);
        }
        discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setItem(input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("item", ItemStack.OPTIONAL_CODEC, getItem());
    }
}
