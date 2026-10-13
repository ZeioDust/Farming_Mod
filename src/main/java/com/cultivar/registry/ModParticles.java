package com.cultivar.registry;

import com.cultivar.Cultivar;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Cultivar.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POLLEN_MOTE = PARTICLES.register("pollen_mote", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPORE_SPECK = PARTICLES.register("spore_speck", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_DROPLET = PARTICLES.register("water_droplet", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MIST_WISP = PARTICLES.register("mist_wisp", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOIL_CRUMB = PARTICLES.register("soil_crumb", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
