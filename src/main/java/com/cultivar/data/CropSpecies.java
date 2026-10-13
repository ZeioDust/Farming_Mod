package com.cultivar.data;

import com.cultivar.Cultivar;
import com.cultivar.genetics.Gene;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;

public enum CropSpecies {
    WHEATLINE("Wheatline", "wheatline_sheaf", "Wheatline Sheaf", 0, 0xD8B850, 0xE8D890, Nutrient.NITROGEN, 1600, 3, 2, 3.0f, 1.5f, 5.8f, 7.2f, 0),
    MAIZE("Maize", "maize_cob", "Maize Cob", 3, 0xE8C838, 0xF0E070, Nutrient.NITROGEN, 1900, 2, 2, 4.0f, 1.8f, 5.6f, 7.0f, 0),
    SOYBEAN("Soybean", "soybean_pod", "Soybean Pod", 2, 0x9CC060, 0xF0F0D0, Nutrient.PHOSPHORUS, 1700, 3, 2, 2.5f, 1.6f, 6.0f, 7.0f, 6.0f),
    CLOVER("Clover", "clover_bundle", "Clover Bundle", 0, 0x58A848, 0xE0A0C8, Nutrient.PHOSPHORUS, 1200, 4, 3, 1.5f, 1.4f, 6.0f, 7.5f, 8.0f),
    TUBER("Tuber", "tuber", "Tuber", 3, 0x6E9A48, 0xC090E0, Nutrient.POTASSIUM, 1800, 3, 2, 3.0f, 1.3f, 5.0f, 6.5f, 4.0f),
    BRASSICA("Brassica", "brassica_head", "Brassica Head", 3, 0x5A9890, 0xF0E060, Nutrient.NITROGEN, 1500, 2, 2, 3.5f, 1.2f, 6.2f, 7.5f, 0),
    GOURD("Gourd", "gourd", "Gourd", 2, 0x709A40, 0xF0A030, Nutrient.POTASSIUM, 2100, 2, 2, 3.5f, 1.9f, 5.5f, 6.8f, 0),
    FIBER_FLAX("Fiber Flax", "flax_fiber", "Flax Fiber", 0, 0x7AA058, 0x6080E0, Nutrient.POTASSIUM, 1500, 3, 2, 2.5f, 1.4f, 5.5f, 7.0f, 0);

    public static final CropSpecies[] VALUES = values();

    private final String displayName;
    private final String produceName;
    private final String produceDisplayName;
    private final int foodNutrition;
    private final int leafColor;
    private final int flowerColor;
    private final Nutrient preferredNutrient;
    private final int ticksPerStage;
    private final int baseYield;
    private final int seedsPerHarvest;
    private final float nutrientUse;
    private final float maxTemperature;
    private final float phMin;
    private final float phMax;
    private final float nitrogenFixation;

    CropSpecies(String displayName, String produceName, String produceDisplayName, int foodNutrition, int leafColor, int flowerColor,
                Nutrient preferredNutrient, int ticksPerStage, int baseYield, int seedsPerHarvest, float nutrientUse, float maxTemperature,
                float phMin, float phMax, float nitrogenFixation) {
        this.displayName = displayName;
        this.produceName = produceName;
        this.produceDisplayName = produceDisplayName;
        this.foodNutrition = foodNutrition;
        this.leafColor = leafColor;
        this.flowerColor = flowerColor;
        this.preferredNutrient = preferredNutrient;
        this.ticksPerStage = ticksPerStage;
        this.baseYield = baseYield;
        this.seedsPerHarvest = seedsPerHarvest;
        this.nutrientUse = nutrientUse;
        this.maxTemperature = maxTemperature;
        this.phMin = phMin;
        this.phMax = phMax;
        this.nitrogenFixation = nitrogenFixation;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Identifier key() {
        return Identifier.fromNamespaceAndPath(Cultivar.MODID, id());
    }

    public String cropBlockName() {
        return id() + "_crop";
    }

    public String seedPacketName() {
        return id() + "_seed_packet";
    }

    public String displayName() {
        return displayName;
    }

    public String produceName() {
        return produceName;
    }

    public String produceDisplayName() {
        return produceDisplayName;
    }

    public int foodNutrition() {
        return foodNutrition;
    }

    public int leafColor() {
        return leafColor;
    }

    public int flowerColor() {
        return flowerColor;
    }

    public boolean isDefaultNitrogenFixer() {
        return nitrogenFixation > 0;
    }

    public float defaultNitrogenFixation() {
        return nitrogenFixation;
    }

    public static CropSpecies byKey(Identifier key) {
        if (key == null || !Cultivar.MODID.equals(key.getNamespace())) {
            return null;
        }
        for (CropSpecies species : VALUES) {
            if (species.id().equals(key.getPath())) {
                return species;
            }
        }
        return null;
    }

    public SpeciesDefinition defaultDefinition() {
        Map<Gene, List<String>> natural = new EnumMap<>(Gene.class);
        int shift = ordinal();
        natural.put(Gene.YIELD, rotate(List.of("yield_standard", "yield_sparse", "yield_ample", "yield_heavy"), shift));
        natural.put(Gene.GROWTH_RATE, rotate(List.of("growth_steady", "growth_slow", "growth_brisk", "growth_vigorous"), shift + 1));
        natural.put(Gene.COLD_TOLERANCE, rotate(List.of("cold_temperate", "cold_tender", "cold_hardy", "cold_frostproof"), shift));
        natural.put(Gene.DROUGHT_TOLERANCE, rotate(List.of("drought_moderate", "drought_thirsty", "drought_thrifty", "drought_xeric"), shift + 2));
        natural.put(Gene.LIGHT_NEED, rotate(List.of("light_average", "light_bright", "light_sun_hungry", "light_shade_tolerant"), shift));
        natural.put(Gene.BLIGHT_RESISTANCE, rotate(List.of("blight_susceptible", "blight_tolerant", "blight_partial", "blight_resistant"), shift / 2));
        natural.put(Gene.NUTRIENT_DRAW, rotate(List.of("nutrient_balanced", "nutrient_hungry", "nutrient_greedy", "nutrient_frugal"), shift + 1));
        natural.put(Gene.MUTATION_RATE, rotate(List.of("mutation_standard", "mutation_stable", "mutation_labile", "mutation_volatile"), shift / 3));
        return new SpeciesDefinition(ticksPerStage, baseYield, seedsPerHarvest, preferredNutrient, nutrientUse, maxTemperature, natural);
    }

    private static List<String> rotate(List<String> list, int shift) {
        String first = list.get(0);
        List<String> rest = list.subList(1, list.size());
        int n = rest.size();
        int s = Math.floorMod(shift, n);
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        out.add(first);
        for (int i = 0; i < n; i++) {
            out.add(rest.get((i + s) % n));
        }
        return List.copyOf(out);
    }

    public PhBand defaultPhBand() {
        return new PhBand(key(), phMin, phMax, 1.5f);
    }
}
