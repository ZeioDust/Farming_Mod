package com.cultivar.registry;

import com.cultivar.Cultivar;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Cultivar.MODID);
    public static final Map<String, String> SUBTITLES = new LinkedHashMap<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> POLLEN_PUFF = sound("pollen_puff", "Pollen puffs");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAB_ANALYZE = sound("lab_analyze", "Genome Lab finishes analysis");
    public static final DeferredHolder<SoundEvent, SoundEvent> PROBE_CLICK = sound("probe_click", "Soil probe clicks");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUTAGEN_HISS = sound("mutagen_hiss", "Mutagen Chamber hisses");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLIGHT_SPREAD = sound("blight_spread", "Blight spreads");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLER_SPRAY = sound("sprinkler_spray", "Sprinkler sprays");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANK_DRAWER = sound("bank_drawer", "Seed Bank drawer slides");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAFT_CUT = sound("graft_cut", "Grafting knife cuts");
    public static final DeferredHolder<SoundEvent, SoundEvent> CANOPY_RUSTLE = sound("canopy_rustle", "Canopy leaves rustle");
    public static final DeferredHolder<SoundEvent, SoundEvent> HARVEST_CLATTER = sound("harvest_clatter", "Produce clatters");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name, String subtitle) {
        SUBTITLES.put(name, subtitle);
        return SOUNDS.register(name, SoundEvent::createVariableRangeEvent);
    }
}
