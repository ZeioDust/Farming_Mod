package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CultivarSoundProvider extends SoundDefinitionsProvider {
    public CultivarSoundProvider(PackOutput output) {
        super(output, Cultivar.MODID);
    }

    private void define(DeferredHolder<SoundEvent, SoundEvent> event, String name, float pitch, float volume, String... files) {
        SoundDefinition definition = definition().subtitle(Texts.subtitle(name));
        for (String file : files) {
            definition.with(sound(Identifier.withDefaultNamespace(file)).pitch(pitch).volume(volume));
        }
        add(event, definition);
    }

    @Override
    public void registerSounds() {
        define(ModSounds.POLLEN_PUFF, "pollen_puff", 1.4f, 0.6f, "block/azalea_leaves/break1", "block/azalea_leaves/break2");
        define(ModSounds.LAB_ANALYZE, "lab_analyze", 1.0f, 0.8f, "block/brewing_stand/brew1", "block/brewing_stand/brew2");
        define(ModSounds.PROBE_CLICK, "probe_click", 1.3f, 0.7f, "random/click");
        define(ModSounds.MUTAGEN_HISS, "mutagen_hiss", 0.8f, 0.7f, "random/fizz");
        define(ModSounds.BLIGHT_SPREAD, "blight_spread", 0.6f, 0.6f, "block/moss/break1", "block/moss/break2");
        define(ModSounds.SPRINKLER_SPRAY, "sprinkler_spray", 1.4f, 0.4f, "random/splash");
        define(ModSounds.BANK_DRAWER, "bank_drawer", 1.2f, 0.7f, "block/barrel/open1", "block/barrel/open2");
        define(ModSounds.GRAFT_CUT, "graft_cut", 1.3f, 0.8f, "item/axe/strip1", "item/axe/strip2");
        define(ModSounds.CANOPY_RUSTLE, "canopy_rustle", 0.8f, 0.5f, "block/vine/break1", "block/vine/break2", "block/big_dripleaf/break1");
        define(ModSounds.HARVEST_CLATTER, "harvest_clatter", 1.1f, 0.6f, "block/bamboo/place1", "block/bamboo/place2");
    }
}
