package com.cultivar.entity;

import com.cultivar.registry.ModParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MistCloudEntity extends EffectEntity {
    public MistCloudEntity(EntityType<? extends MistCloudEntity> type, Level level) {
        super(type, level);
    }

    public void drift(Vec3 velocity) {
        setDeltaMovement(velocity);
        setColor(0xC0E8F4FF);
    }

    @Override
    protected int defaultLifetime() {
        return 120;
    }

    @Override
    protected void clientEffects() {
        if (random.nextInt(2) == 0) {
            level().addParticle(ModParticles.MIST_WISP.get(), getX() + (random.nextDouble() - 0.5) * 1.2, getY() + random.nextDouble() * 0.5,
                    getZ() + (random.nextDouble() - 0.5) * 1.2, 0, 0.003, 0);
        }
    }
}
