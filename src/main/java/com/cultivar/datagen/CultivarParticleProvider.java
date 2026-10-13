package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.registry.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class CultivarParticleProvider extends ParticleDescriptionProvider {
    public CultivarParticleProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(ModParticles.POLLEN_MOTE.get(), Identifier.fromNamespaceAndPath(Cultivar.MODID, "pollen_mote"), 2, false);
        spriteSet(ModParticles.SPORE_SPECK.get(), Identifier.fromNamespaceAndPath(Cultivar.MODID, "spore_speck"), 2, false);
        spriteSet(ModParticles.WATER_DROPLET.get(), Identifier.fromNamespaceAndPath(Cultivar.MODID, "water_droplet"), 2, false);
        spriteSet(ModParticles.MIST_WISP.get(), Identifier.fromNamespaceAndPath(Cultivar.MODID, "mist_wisp"), 2, false);
        spriteSet(ModParticles.SOIL_CRUMB.get(), Identifier.fromNamespaceAndPath(Cultivar.MODID, "soil_crumb"), 2, false);
    }
}
