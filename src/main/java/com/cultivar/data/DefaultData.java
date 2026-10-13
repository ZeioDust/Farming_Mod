package com.cultivar.data;

import com.cultivar.Cultivar;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.DefaultGenetics;
import com.cultivar.genetics.Gene;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class DefaultData {
    private DefaultData() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, path);
    }

    public static Map<Identifier, AlleleDefinition> alleles() {
        Map<Identifier, AlleleDefinition> map = new LinkedHashMap<>();
        for (Allele allele : DefaultGenetics.alleles()) {
            map.put(id(allele.id()), AlleleDefinition.of(allele));
        }
        return map;
    }

    public static Map<Identifier, GeneDefinition> genes() {
        Map<Identifier, GeneDefinition> map = new LinkedHashMap<>();
        for (Gene gene : Gene.VALUES) {
            map.put(id(gene.id()), GeneDefinition.defaultFor(gene));
        }
        return map;
    }

    public static Map<Identifier, SpeciesDefinition> species() {
        Map<Identifier, SpeciesDefinition> map = new LinkedHashMap<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            map.put(species.key(), species.defaultDefinition());
        }
        return map;
    }

    public static Map<Identifier, PhBand> phBands() {
        Map<Identifier, PhBand> map = new LinkedHashMap<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            map.put(species.key(), species.defaultPhBand());
        }
        return map;
    }

    public static Map<Identifier, NitrogenFixer> nitrogenFixers() {
        Map<Identifier, NitrogenFixer> map = new LinkedHashMap<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            if (species.isDefaultNitrogenFixer()) {
                map.put(species.key(), new NitrogenFixer(species.key(), species.defaultNitrogenFixation()));
            }
        }
        return map;
    }

    public static Map<Identifier, BlightStrain> blightStrains() {
        Map<Identifier, BlightStrain> map = new LinkedHashMap<>();
        map.put(id("leaf_spot"), new BlightStrain(
                List.of(CropSpecies.WHEATLINE.key(), CropSpecies.MAIZE.key(), CropSpecies.FIBER_FLAX.key()),
                0.55f, 8, 0.004f, List.of("blight_resistant", "blight_wild_canopy"), 0xFF7A6A3A));
        map.put(id("root_rot"), new BlightStrain(
                List.of(CropSpecies.SOYBEAN.key(), CropSpecies.CLOVER.key(), CropSpecies.TUBER.key()),
                0.45f, 10, 0.003f, List.of("blight_wild_canopy", "blight_sentinel"), 0xFF5A4A3A));
        map.put(id("powdery_mildew"), new BlightStrain(
                List.of(CropSpecies.BRASSICA.key(), CropSpecies.GOURD.key(), CropSpecies.MAIZE.key()),
                0.65f, 6, 0.005f, List.of("blight_partial", "blight_sentinel"), 0xFFC8C8B8));
        map.put(id("stem_canker"), new BlightStrain(
                List.of(CropSpecies.WHEATLINE.key(), CropSpecies.TUBER.key(), CropSpecies.GOURD.key(), CropSpecies.BRASSICA.key()),
                0.4f, 12, 0.002f, List.of("blight_tolerant", "blight_wild_canopy"), 0xFF6A3A2A));
        return map;
    }
}
