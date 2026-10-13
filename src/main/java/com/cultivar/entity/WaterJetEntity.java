package com.cultivar.entity;

import com.cultivar.registry.ModParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WaterJetEntity extends EffectEntity {
    public WaterJetEntity(EntityType<? extends WaterJetEntity> type, Level level) {
        super(type, level);
    }

    public void launch(Vec3 velocity) {
        setDeltaMovement(velocity);
    }

    @Override
    protected int defaultLifetime() {
        return 18;
    }

    @Override
    public void tick() {
        setDeltaMovement(getDeltaMovement().add(0, -0.03, 0));
        super.tick();
    }

    @Override
    protected void clientEffects() {
        level().addParticle(ModParticles.WATER_DROPLET.get(), getX(), getY(), getZ(), 0, -0.02, 0);
    }
}
