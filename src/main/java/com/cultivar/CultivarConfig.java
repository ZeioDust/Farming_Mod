package com.cultivar;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CultivarConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final List<String> KEYS = new ArrayList<>();
    public static final List<String> SECTIONS = new ArrayList<>();
    public static final java.util.Map<String, String> COMMENTS = new java.util.LinkedHashMap<>();

    static {
        push("genetics", "Genome, inheritance and mutation");
    }

    public static final ModConfigSpec.BooleanValue GENETICS_ENABLED = bool("geneticsEnabled", true,
            "Master switch for genetics. When off, every plant behaves as if it had the neutral standard phenotype.");
    public static final ModConfigSpec.IntValue ALLELES_PER_GENE_PER_SPECIES = integer("allelesPerGenePerSpecies", 3, 1, 8,
            "How many alleles of each gene occur naturally in a species' wild-found and starting seeds.");
    public static final ModConfigSpec.DoubleValue MUTATION_RATE_MULTIPLIER = decimal("mutationRateMultiplier", 1.0, 0.0, 50.0,
            "Multiplier applied to every plant's MUTATION_RATE phenotype when offspring are produced.");
    public static final ModConfigSpec.BooleanValue MUTATION_ONLY_ALLELES_ENABLED = bool("mutationOnlyAllelesEnabled", true,
            "Whether mutation can produce the rare alleles flagged mutation-only.");

    static {
        pop();
        push("pollination", "Wind, machine and manual pollination");
    }

    public static final ModConfigSpec.IntValue POLLINATION_RADIUS = integer("pollinationRadius", 4, 1, 16,
            "Radius in blocks within which a flowering plant can receive pollen from another flowering plant.");
    public static final ModConfigSpec.DoubleValue POLLINATION_BASE_CHANCE = decimal("pollinationBaseChance", 0.35, 0.0, 1.0,
            "Chance per growth update that a flowering plant is pollinated by an adjacent donor; falls off with distance.");
    public static final ModConfigSpec.DoubleValue WIND_INFLUENCE_STRENGTH = decimal("windInfluenceStrength", 0.6, 0.0, 1.0,
            "How strongly the per-chunk wind favours downwind pollination targets (0 = wind ignored).");
    public static final ModConfigSpec.BooleanValue SELF_POLLINATION_ENABLED = bool("selfPollinationEnabled", true,
            "Whether unpollinated plants self-pollinate and still yield seed packets at harvest.");

    static {
        pop();
        push("soil", "Soil chemistry and crop rotation");
    }

    public static final ModConfigSpec.BooleanValue SOIL_CHEMISTRY_ENABLED = bool("soilChemistryEnabled", true,
            "Whether crops deplete and respond to soil nitrogen, phosphorus, potassium and pH.");
    public static final ModConfigSpec.DoubleValue NUTRIENT_DEPLETION_MULTIPLIER = decimal("nutrientDepletionMultiplier", 1.0, 0.0, 10.0,
            "Multiplier on nutrients removed from soil per growth stage.");
    public static final ModConfigSpec.IntValue NUTRIENT_FLOOR = integer("nutrientFloor", 10, 0, 100,
            "Below this nutrient level (any of N, P, K) growth stalls.");
    public static final ModConfigSpec.DoubleValue PH_BAND_WIDTH = decimal("phBandWidth", 1.0, 0.1, 5.0,
            "Multiplier on every species' pH band width. Larger values make pH more forgiving.");
    public static final ModConfigSpec.DoubleValue ROTATION_BONUS_MULTIPLIER = decimal("rotationBonusMultiplier", 1.0, 0.0, 10.0,
            "Multiplier on the nitrogen that nitrogen-fixing species return to the soil.");
    public static final ModConfigSpec.IntValue COMPOST_TICKS = integer("compostTicks", 1200, 20, 72000,
            "Ticks a Compost Bin needs to turn a full charge of plant waste into one Compost.");
    public static final ModConfigSpec.IntValue SOIL_UPDATE_INTERVAL = integer("soilUpdateInterval", 100, 20, 2400,
            "Ticks between crop growth and soil chemistry updates. Only soil with a plant above it is processed.");

    static {
        pop();
        push("blight", "Plant disease");
    }

    public static final ModConfigSpec.BooleanValue BLIGHT_ENABLED = bool("blightEnabled", true,
            "Whether blight can appear and spread.");
    public static final ModConfigSpec.IntValue BLIGHT_SPREAD_RADIUS = integer("blightSpreadRadius", 2, 1, 8,
            "Radius in blocks that an infected plant tries to infect each blight tick.");
    public static final ModConfigSpec.IntValue BLIGHT_TICK_INTERVAL = integer("blightTickInterval", 200, 20, 24000,
            "Ticks between blight ticks.");
    public static final ModConfigSpec.IntValue BLIGHT_ATTEMPT_BUDGET = integer("blightAttemptBudget", 128, 1, 4096,
            "Hard cap on infection attempts processed per server tick.");
    public static final ModConfigSpec.DoubleValue MONOCULTURE_MULTIPLIER_MAX = decimal("monocultureMultiplierMax", 4.0, 1.0, 20.0,
            "Transmission multiplier when every plant in the neighbourhood is genetically identical.");
    public static final ModConfigSpec.DoubleValue MOISTURE_TRANSMISSION_BONUS = decimal("moistureTransmissionBonus", 0.75, 0.0, 5.0,
            "Extra transmission at fully wet soil (scaled linearly by soil moisture).");

    static {
        pop();
        push("lab", "Genome Lab and Seed Bank");
    }

    public static final ModConfigSpec.IntValue LAB_ANALYZE_TICKS = integer("labAnalyzeTicks", 200, 1, 72000,
            "Ticks the Genome Lab needs to analyse one sample.");
    public static final ModConfigSpec.BooleanValue HAND_LENS_REVEALS_GENOTYPE = bool("handLensRevealsGenotype", false,
            "If true the Hand Lens shows both alleles; by default it reveals only the expressed phenotype.");
    public static final ModConfigSpec.IntValue SEED_BANK_WITHDRAW_COST = integer("seedBankWithdrawCost", 2, 0, 64,
            "Paper consumed to withdraw one copy of a stored line from the Seed Bank.");
    public static final ModConfigSpec.IntValue SEED_BANK_MAX_LINES = integer("seedBankMaxLines", 64, 1, 1024,
            "Maximum stored lines per Seed Bank. Each attached Seed Vault Drawer adds 8 lines up to this cap.");

    static {
        pop();
        push("greenhouse", "Climate control");
    }

    public static final ModConfigSpec.BooleanValue GREENHOUSE_ENABLED = bool("greenhouseEnabled", true,
            "Whether Climate Controllers seal volumes and override the local climate.");
    public static final ModConfigSpec.IntValue CLIMATE_VOLUME_MAX = integer("climateVolumeMax", 4096, 8, 65536,
            "Maximum number of blocks a Climate Controller flood fill may visit before the volume counts as unsealed.");
    public static final ModConfigSpec.DoubleValue GREENHOUSE_INSULATION_FACTOR = decimal("greenhouseInsulationFactor", 1.0, 0.0, 10.0,
            "Multiplier on how quickly a sealed volume's temperature drifts toward the controller setpoint.");

    static {
        pop();
        push("mutagenesis", "Induced mutation");
    }

    public static final ModConfigSpec.DoubleValue MUTAGEN_SUCCESS_CHANCE = decimal("mutagenSuccessChance", 0.6, 0.0, 1.0,
            "Mutagen Chamber: chance that a seed gains a new random allele.");
    public static final ModConfigSpec.DoubleValue MUTAGEN_DESTROY_CHANCE = decimal("mutagenDestroyChance", 0.2, 0.0, 1.0,
            "Mutagen Chamber: chance that a seed is destroyed. The remainder are unchanged.");
    public static final ModConfigSpec.DoubleValue THERMAL_SHOCK_SUCCESS_CHANCE = decimal("thermalShockSuccessChance", 0.25, 0.0, 1.0,
            "Thermal Shock Unit: chance that a seed gains a new random allele.");
    public static final ModConfigSpec.DoubleValue THERMAL_SHOCK_DESTROY_CHANCE = decimal("thermalShockDestroyChance", 0.08, 0.0, 1.0,
            "Thermal Shock Unit: chance that a seed is destroyed.");

    static {
        pop();
        push("misc", "Grafting, Canopy and global multipliers");
    }

    public static final ModConfigSpec.BooleanValue GRAFTING_ENABLED = bool("graftingEnabled", true,
            "Whether the Grafting Knife can join plants into Graft Unions.");
    public static final ModConfigSpec.BooleanValue CANOPY_ENABLED = bool("canopyEnabled", true,
            "Whether Canopy portals can be activated.");
    public static final ModConfigSpec.DoubleValue WILD_ALLELE_STRENGTH = decimal("wildAlleleStrength", 1.0, 0.25, 3.0,
            "Scales the advantage of wild tolerance and resistance alleles.");
    public static final ModConfigSpec.DoubleValue YIELD_MULTIPLIER = decimal("yieldMultiplier", 1.0, 0.0, 10.0,
            "Global multiplier on harvest drop counts.");
    public static final ModConfigSpec.DoubleValue GROWTH_RATE_MULTIPLIER = decimal("growthRateMultiplier", 1.0, 0.05, 20.0,
            "Global multiplier on crop growth speed.");

    static {
        pop();
        push("market", "Produce grades, the Produce Exchange and contracts");
    }

    public static final ModConfigSpec.DoubleValue GRADE_THRESHOLD_B = decimal("gradeThresholdB", 0.42, 0.0, 1.0,
            "Harvest score (55% genetics, 45% growing conditions) needed for Grade B produce.");
    public static final ModConfigSpec.DoubleValue GRADE_THRESHOLD_A = decimal("gradeThresholdA", 0.58, 0.0, 1.0,
            "Harvest score needed for Grade A produce.");
    public static final ModConfigSpec.DoubleValue GRADE_THRESHOLD_PRIME = decimal("gradeThresholdPrime", 0.74, 0.0, 1.0,
            "Harvest score needed for Prime produce.");
    public static final ModConfigSpec.DoubleValue EXCHANGE_PRICE_MULTIPLIER = decimal("exchangePriceMultiplier", 1.0, 0.0, 100.0,
            "Multiplier on every Produce Exchange price.");
    public static final ModConfigSpec.IntValue EXCHANGE_SATURATION = integer("exchangeSaturation", 96, 1, 100000,
            "Items of one species sold recently that halve that species' price; recent sales fade by a quarter each day.");
    public static final ModConfigSpec.DoubleValue CONTRACT_REWARD_MULTIPLIER = decimal("contractRewardMultiplier", 1.0, 0.0, 100.0,
            "Multiplier on the emeralds paid by Contract Board contracts.");

    static {
        pop();
        push("processing", "The Processing Station and processed goods");
    }

    public static final ModConfigSpec.DoubleValue PROCESSING_SPEED_MULTIPLIER = decimal("processingSpeedMultiplier", 1.0, 0.05, 20.0,
            "Multiplier on Processing Station speed.");
    public static final ModConfigSpec.IntValue SOY_OIL_BOOST_TICKS = integer("soyOilBoostTicks", 2400, 0, 72000,
            "How long one dose of Soy Oil doubles a machine's working speed, in ticks.");

    static {
        pop();
        push("mastery", "The Gene Editor, Show Bench and Allele Atlas");
    }

    public static final ModConfigSpec.IntValue GENE_EDIT_TICKS = integer("geneEditTicks", 400, 20, 72000,
            "Ticks the Gene Editor takes to rewrite one allele.");
    public static final ModConfigSpec.IntValue SHOW_BENCH_PAPER_COST = integer("showBenchPaperCost", 1, 0, 64,
            "Paper consumed when the Show Bench certifies a seed packet.");

    static {
        pop();
        push("guide", "Field Manual and tutorial hints");
    }

    public static final ModConfigSpec.BooleanValue GIVE_FIELD_MANUAL = bool("giveFieldManual", true,
            "Whether each player receives a Field Manual the first time they join.");
    public static final ModConfigSpec.BooleanValue TUTORIAL_HINTS = bool("tutorialHints", true,
            "Whether one-time tutorial hints appear the first time a player meets each Cultivar system.");

    static {
        pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CultivarConfig() {
    }

    private static void push(String name, String comment) {
        SECTIONS.add(name);
        COMMENTS.put(name, comment);
        BUILDER.comment(comment).push(name);
    }

    private static void pop() {
        BUILDER.pop();
    }

    private static ModConfigSpec.BooleanValue bool(String key, boolean def, String comment) {
        KEYS.add(key);
        COMMENTS.put(key, comment);
        return BUILDER.comment(comment).define(key, def);
    }

    private static ModConfigSpec.IntValue integer(String key, int def, int min, int max, String comment) {
        KEYS.add(key);
        COMMENTS.put(key, comment);
        return BUILDER.comment(comment).defineInRange(key, def, min, max);
    }

    private static ModConfigSpec.DoubleValue decimal(String key, double def, double min, double max, String comment) {
        KEYS.add(key);
        COMMENTS.put(key, comment);
        return BUILDER.comment(comment).defineInRange(key, def, min, max);
    }

    public static boolean isLoaded() {
        return SPEC.isLoaded();
    }
}
