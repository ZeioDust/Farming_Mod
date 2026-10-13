package com.cultivar.entity;

import com.cultivar.registry.ModParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SporeDriftEntity extends EffectEntity {
    public SporeDriftEntity(EntityType<? extends SporeDriftEntity> type, Level level) {
        super(type, level);
    }

    public void configure(float dx, float dz, int color) {
        setDeltaMovement(new Vec3(dx, 0.004, dz));
        setColor(color);
    }

    @Override
    protected int defaultLifetime() {
        return 100;
    }

    @Override
    protected void clientEffects() {
        if (random.nextInt(3) == 0) {
            level().addParticle(ModParticles.SPORE_SPECK.get(), getX() + (random.nextDouble() - 0.5) * 0.6, getY() + random.nextDouble() * 0.4,
                    getZ() + (random.nextDouble() - 0.5) * 0.6, getDeltaMovement().x * 0.5, 0.002, getDeltaMovement().z * 0.5);
        }
    }
}
