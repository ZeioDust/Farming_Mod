package com.cultivar.data;

import com.cultivar.Cultivar;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = Cultivar.MODID)
public final class CultivarRegistries {
    public static final ResourceKey<Registry<GeneDefinition>> GENE = key("gene");
    public static final ResourceKey<Registry<AlleleDefinition>> ALLELE = key("allele");
    public static final ResourceKey<Registry<SpeciesDefinition>> SPECIES = key("species");
    public static final ResourceKey<Registry<PhBand>> PH_BAND = key("ph_band");
    public static final ResourceKey<Registry<NitrogenFixer>> NITROGEN_FIXER = key("nitrogen_fixer");
    public static final ResourceKey<Registry<BlightStrain>> BLIGHT_STRAIN = key("blight_strain");

    private CultivarRegistries() {
    }

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Cultivar.MODID, name));
    }

    @SubscribeEvent
    static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GENE, GeneDefinition.CODEC, GeneDefinition.CODEC);
        event.dataPackRegistry(ALLELE, AlleleDefinition.CODEC, AlleleDefinition.CODEC);
        event.dataPackRegistry(SPECIES, SpeciesDefinition.CODEC, SpeciesDefinition.CODEC);
        event.dataPackRegistry(PH_BAND, PhBand.CODEC, PhBand.CODEC);
        event.dataPackRegistry(NITROGEN_FIXER, NitrogenFixer.CODEC, NitrogenFixer.CODEC);
        event.dataPackRegistry(BLIGHT_STRAIN, BlightStrain.CODEC, BlightStrain.CODEC);
    }
}
