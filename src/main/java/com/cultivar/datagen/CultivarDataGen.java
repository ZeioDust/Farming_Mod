package com.cultivar.datagen;

import com.cultivar.Cultivar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Cultivar.MODID, value = Dist.CLIENT)
public final class CultivarDataGen {
    private CultivarDataGen() {
    }

    @SubscribeEvent
    static void gather(GatherDataEvent.Client event) {
        event.createDatapackRegistryObjects(CultivarWorldgenProvider.BUILDER);
        event.createProvider(CultivarModelProvider::new);
        event.createProvider(CultivarRecipeProvider.Runner::new);
        event.createProvider(CultivarLootProvider::new);
        event.createBlockAndItemTags(CultivarTagProviders.Blocks::new, CultivarTagProviders.Items::new);
        event.createProvider(CultivarTagProviders.Biomes::new);
        event.createProvider(CultivarAdvancementProvider::new);
        event.createProvider(CultivarLanguageProvider::new);
        event.createProvider(CultivarSoundProvider::new);
        event.createProvider(CultivarParticleProvider::new);
        event.createProvider(CultivarLootModifierProvider::new);
        event.createProvider(CultivarDataMapProvider::new);
    }
}
