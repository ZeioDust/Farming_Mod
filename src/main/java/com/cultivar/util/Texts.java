package com.cultivar.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Texts {
    public static final Map<String, String> ALL = new LinkedHashMap<>();

    private Texts() {
    }

    private static String k(String key, String english) {
        ALL.put(key, english);
        return key;
    }

    public static MutableComponent tr(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static final String ITEM_GROUP = k("itemGroup.cultivar", "Cultivar");
    public static final String UNKNOWN = k("text.cultivar.unknown", "Unknown");
    public static final String NONE = k("text.cultivar.none", "None");

    public static final String TIP_GENOME_UNREAD = k("tooltip.cultivar.genome_unread", "Genome not yet analysed. Use a Genome Lab.");
    public static final String TIP_GENOME_CODE = k("tooltip.cultivar.genome_code", "Genome code: %s");
    public static final String TIP_UNSET_GENOME = k("tooltip.cultivar.unset_genome", "Natural seed: genome assigned when planted or carried");
    public static final String TIP_LINEAGE = k("tooltip.cultivar.lineage", "Line #%s");
    public static final String TIP_TRUE_BREEDING = k("tooltip.cultivar.true_breeding", "True-breeding line");
    public static final String TIP_WILD_SPECIES = k("tooltip.cultivar.wild_species", "Wild %s");
    public static final String TIP_CARD_SPECIES = k("tooltip.cultivar.card_species", "Analysis of %s");
    public static final String TIP_CARD_STRAIN = k("tooltip.cultivar.card_strain", "Strain report: %s");
    public static final String TIP_CARD_OPEN = k("tooltip.cultivar.card_open", "Use to read the full report");
    public static final String TIP_BLANK_CARD = k("tooltip.cultivar.blank_card", "Blank. Produced by a Genome Lab.");
    public static final String TIP_STRAIN = k("tooltip.cultivar.strain", "Strain: %s");
    public static final String TIP_POLLEN = k("tooltip.cultivar.pollen", "Carrying pollen from %s at %s");
    public static final String TIP_BRUSH_EMPTY = k("tooltip.cultivar.brush_empty", "Use on a flowering plant to collect pollen, then on another to cross them");
    public static final String TIP_KNIFE_ROOT = k("tooltip.cultivar.knife_root", "Rootstock selected at %s");
    public static final String TIP_KNIFE_EMPTY = k("tooltip.cultivar.knife_empty", "Use on a mature rootstock plant, then on an adjacent scion of the same species");
    public static final String TIP_TRIAL_NUMBER = k("tooltip.cultivar.trial_number", "Next plot number: %s");
    public static final String TIP_PEDIGREE_LINE = k("tooltip.cultivar.pedigree_line", "Bound to line #%s");
    public static final String TIP_PEDIGREE_EMPTY = k("tooltip.cultivar.pedigree_empty", "Hold a seed packet in the off hand and use to print its ancestry");
    public static final String TIP_AMENDMENT = k("tooltip.cultivar.amendment", "Use on Managed Soil, a crop, or load into a Nutrient Spreader");
    public static final String TIP_HAND_LENS = k("tooltip.cultivar.hand_lens", "Reveals a planted crop's expressed traits");
    public static final String TIP_SOIL_PROBE = k("tooltip.cultivar.soil_probe", "Reads soil chemistry. Sneak-use on the ground to deploy a Soil Probe Rover");
    public static final String TIP_ISOLATION_BAG = k("tooltip.cultivar.isolation_bag", "Bag a plant to force self-pollination");
    public static final String TIP_NOTEBOOK = k("tooltip.cultivar.notebook", "Records every cross you harvest. Sneak-use to turn the page");
    public static final String TIP_MUTAGEN = k("tooltip.cultivar.mutagen", "Chemical mutagen for the Mutagen Chamber; treats 8 seed packets");
    public static final String TIP_CANOPY_KEY = k("tooltip.cultivar.canopy_key", "Use on a complete Canopy Portal Frame ring");
    public static final String TIP_PRODUCE = k("tooltip.cultivar.produce", "Harvested produce");
    public static final String TIP_TUBER_FIXER = k("tooltip.cultivar.tuber_fixer", "A leguminous groundnut-type tuber; fixes nitrogen");

    public static final String[] STAGES = {
            k("stage.cultivar.0", "sprout"), k("stage.cultivar.1", "seedling"), k("stage.cultivar.2", "vegetative"),
            k("stage.cultivar.3", "budding"), k("stage.cultivar.4", "flowering"), k("stage.cultivar.5", "ripening"),
            k("stage.cultivar.6", "mature")};

    public static MutableComponent stage(int stage) {
        return tr(STAGES[Math.max(0, Math.min(STAGES.length - 1, stage))]);
    }

    public static final String YES = k("text.cultivar.yes", "yes");
    public static final String NO = k("text.cultivar.no", "no");

    public static MutableComponent yesNo(boolean value) {
        return tr(value ? YES : NO);
    }

    public static final String GENE_ROW = k("text.cultivar.gene_row", "%s: %s");
    public static final String EXPRESSED = k("text.cultivar.expressed", "expressed");
    public static final String HIDDEN = k("text.cultivar.hidden", "hidden");
    public static final String CODOMINANT = k("text.cultivar.codominant", "codominant");
    public static final String DOMINANCE = k("text.cultivar.dominance", "dom %s");
    public static final String FLAG_WILD = k("text.cultivar.flag_wild", "wild");
    public static final String FLAG_MUTANT = k("text.cultivar.flag_mutant", "mutant");

    public static final String MSG_LENS_HEADER = k("message.cultivar.lens.header", "%s, stage %s (%s): expressed traits");
    public static final String MSG_LENS_LINE = k("message.cultivar.lens.line", "  %s: %s");
    public static final String MSG_LENS_GENOTYPE = k("message.cultivar.lens.genotype", "  %s: %s / %s");
    public static final String MSG_LENS_STATUS = k("message.cultivar.lens.status", "  Pollinated: %s   Blighted: %s   Grafted: %s");
    public static final String MSG_LENS_NOT_CROP = k("message.cultivar.lens.not_crop", "The Hand Lens only works on Cultivar crops.");
    public static final String MSG_PROBE = k("message.cultivar.probe", "Soil: N %s  P %s  K %s  pH %s  moisture %s/7");
    public static final String MSG_PROBE_DEPLETED = k("message.cultivar.probe.depleted", "Depleted Soil: no nutrients. Apply Compost to restore it.");
    public static final String MSG_PROBE_UNMANAGED = k("message.cultivar.probe.unmanaged", "Not managed soil. Plant a Seed Packet on farmland to convert it.");
    public static final String MSG_ROVER_DEPLOYED = k("message.cultivar.rover.deployed", "Soil Probe Rover deployed heading %s.");
    public static final String MSG_ROVER_READING = k("message.cultivar.rover.reading", "Rover %s m: N %s P %s K %s pH %s");
    public static final String MSG_ROVER_NO_SOIL = k("message.cultivar.rover.no_soil", "Rover %s m: no managed soil");
    public static final String MSG_ROVER_DONE = k("message.cultivar.rover.done", "Rover survey complete: %s readings, mean N %s P %s K %s pH %s");
    public static final String MSG_POLLEN_COLLECTED = k("message.cultivar.pollen.collected", "Collected pollen from the %s at %s.");
    public static final String MSG_POLLEN_APPLIED = k("message.cultivar.pollen.applied", "Cross made: %s pollinated with pollen from %s.");
    public static final String MSG_NOT_FLOWERING = k("message.cultivar.not_flowering", "This plant is not flowering.");
    public static final String MSG_WRONG_SPECIES = k("message.cultivar.wrong_species", "Pollen of a different species cannot cross with this plant.");
    public static final String MSG_SAME_PLANT = k("message.cultivar.same_plant", "Choose a different plant as the seed parent.");
    public static final String MSG_NO_POLLEN = k("message.cultivar.no_pollen", "Grafted and bagged plants shed no pollen.");
    public static final String MSG_POLLEN_DIMENSION = k("message.cultivar.pollen_dimension", "This pollen was collected in another dimension.");
    public static final String MSG_UNIT_OUT_OF_RANGE = k("message.cultivar.unit.out_of_range", "That plant is out of this Pollination Unit's range (%s blocks).");
    public static final String MSG_BAGGED = k("message.cultivar.bagged", "Plant bagged: it will self-pollinate.");
    public static final String MSG_ALREADY_BAGGED = k("message.cultivar.already_bagged", "This plant is already bagged.");
    public static final String MSG_UNIT_SOURCE = k("message.cultivar.unit.source", "Pollination Unit source set to %s.");
    public static final String MSG_UNIT_TARGET = k("message.cultivar.unit.target", "Pollination Unit target set to %s. Controlled cross armed.");
    public static final String MSG_UNIT_CLEARED = k("message.cultivar.unit.cleared", "Pollination Unit cross cleared.");
    public static final String MSG_UNIT_STATUS = k("message.cultivar.unit.status", "Pollination Unit: %s, fuel %s, source %s, target %s");
    public static final String MSG_ACTIVE = k("message.cultivar.active", "active");
    public static final String MSG_IDLE = k("message.cultivar.idle", "idle");
    public static final String MSG_AMENDED = k("message.cultivar.amended", "Soil amended: N %s  P %s  K %s  pH %s");
    public static final String MSG_SOIL_RESTORED = k("message.cultivar.soil_restored", "Depleted soil restored to managed soil.");
    public static final String MSG_GRAFT_ROOT = k("message.cultivar.graft.root", "Rootstock selected. Now use the knife on an adjacent mature scion.");
    public static final String MSG_GRAFT_DONE = k("message.cultivar.graft.done", "Graft union formed.");
    public static final String MSG_GRAFT_FAIL = k("message.cultivar.graft.fail", "Grafting needs two adjacent mature plants of the same species.");
    public static final String MSG_GRAFT_DISABLED = k("message.cultivar.graft.disabled", "Grafting is disabled in the config.");
    public static final String MSG_NOTEBOOK_EMPTY = k("message.cultivar.notebook.empty", "Your Field Notebook has no crosses recorded yet.");
    public static final String MSG_NOTEBOOK_HEADER = k("message.cultivar.notebook.header", "Field Notebook, page %s of %s");
    public static final String MSG_NOTEBOOK_ENTRY = k("message.cultivar.notebook.entry", "Day %s, %s (%s): %s x %s -> %s");
    public static final String MSG_PEDIGREE_HEADER = k("message.cultivar.pedigree.header", "Pedigree of line #%s");
    public static final String MSG_PEDIGREE_NODE = k("message.cultivar.pedigree.node", "%s#%s %s [%s] day %s");
    public static final String MSG_PEDIGREE_UNKNOWN = k("message.cultivar.pedigree.unknown", "%s#%s (no record)");
    public static final String MSG_PEDIGREE_NEED_PACKET = k("message.cultivar.pedigree.need_packet", "Hold a Seed Packet in your off hand.");
    public static final String MSG_TRIAL_SET = k("message.cultivar.trial.set", "Plot labelled %s #%s.");
    public static final String MSG_TRIAL_INFO = k("message.cultivar.trial.info", "Plot %s #%s");
    public static final String MSG_MACHINE_LOADED = k("message.cultivar.machine.loaded", "Loaded %s.");
    public static final String MSG_MACHINE_STATUS = k("message.cultivar.machine.status", "%s: %s queued, %s finished, charge %s");
    public static final String MSG_SPREADER_STATUS = k("message.cultivar.spreader.status", "Nutrient Spreader holds %s x%s");
    public static final String MSG_SPREADER_EMPTY = k("message.cultivar.spreader.empty", "Nutrient Spreader is empty. Load Compost, a Nutrient Blend, Lime or Sulfur.");
    public static final String MSG_HOPPER_LEVEL = k("message.cultivar.hopper.level", "Hopper charge %s / %s");
    public static final String MSG_COLLECTOR_EMPTY = k("message.cultivar.collector.empty", "The Harvest Collector is empty.");
    public static final String MSG_PORTAL_INCOMPLETE = k("message.cultivar.portal.incomplete", "The Canopy Portal Frame ring is incomplete: 8 frames around one open centre are needed.");
    public static final String MSG_PORTAL_OPENED = k("message.cultivar.portal.opened", "The portal frame aligns and opens a passage to the Canopy.");
    public static final String MSG_PORTAL_DISABLED = k("message.cultivar.portal.disabled", "The Canopy is disabled in the config.");
    public static final String MSG_BANK_FULL = k("message.cultivar.bank.full", "The Seed Bank is full (%s lines). Attach Seed Vault Drawers for more space.");
    public static final String MSG_CONTROLLER = k("message.cultivar.controller", "Climate Controller: %s");

    public static final String SCREEN_GENOME_LAB = k("container.cultivar.genome_lab", "Genome Lab");
    public static final String SCREEN_SEED_BANK = k("container.cultivar.seed_bank", "Seed Bank");
    public static final String SCREEN_MUTAGEN = k("container.cultivar.mutagen_chamber", "Mutagen Chamber");
    public static final String SCREEN_CLIMATE = k("container.cultivar.climate_controller", "Climate Controller");
    public static final String SCREEN_COMPOST = k("container.cultivar.compost_bin", "Compost Bin");
    public static final String SCREEN_GENOME_CARD = k("container.cultivar.genome_card", "Genome Card");
    public static final String LABEL_PROGRESS = k("screen.cultivar.progress", "Progress %s%%");
    public static final String LABEL_SAMPLE = k("screen.cultivar.sample", "Sample");
    public static final String LABEL_PAPER = k("screen.cultivar.paper", "Paper");
    public static final String LABEL_CARD = k("screen.cultivar.card", "Card");
    public static final String LABEL_CHARGE = k("screen.cultivar.charge", "Charge %s / 100");
    public static final String LABEL_VIAL = k("screen.cultivar.vial", "Vial uses: %s");
    public static final String LABEL_DEPOSIT = k("screen.cultivar.deposit", "Deposit");
    public static final String LABEL_WITHDRAW = k("screen.cultivar.withdraw", "Copy");
    public static final String LABEL_PREV = k("screen.cultivar.prev", "<");
    public static final String LABEL_DELETE = k("screen.cultivar.delete", "x");
    public static final String LABEL_COLDER = k("screen.cultivar.colder", "-");
    public static final String LABEL_WARMER = k("screen.cultivar.warmer", "+");
    public static final String LABEL_NEXT = k("screen.cultivar.next", ">");
    public static final String LABEL_LINES = k("screen.cultivar.lines", "Lines %s / %s");
    public static final String LABEL_COST = k("screen.cultivar.cost", "Copy cost: %s paper");
    public static final String LABEL_NO_LINES = k("screen.cultivar.no_lines", "No lines stored");
    public static final String LABEL_SEALED = k("screen.cultivar.sealed", "Sealed volume: %s blocks");
    public static final String LABEL_UNSEALED = k("screen.cultivar.unsealed", "Not sealed (leak or over %s blocks)");
    public static final String LABEL_TEMP = k("screen.cultivar.temp", "Temperature %s (setpoint %s)");
    public static final String LABEL_MOISTURE = k("screen.cultivar.moisture", "Humidity %s%%");
    public static final String LABEL_LIGHT = k("screen.cultivar.light", "Light floor %s");
    public static final String LABEL_INSULATION = k("screen.cultivar.insulation", "Insulation %s%%");
    public static final String LABEL_DEVICES = k("screen.cultivar.devices", "Grow lights %s  Humidifiers %s  Dehumidifiers %s");
    public static final String LABEL_RECOMPUTE = k("screen.cultivar.recompute", "Rescan");
    public static final String LABEL_GENE = k("screen.cultivar.gene", "Gene");
    public static final String LABEL_ALLELE_A = k("screen.cultivar.allele_a", "Allele A");
    public static final String LABEL_ALLELE_B = k("screen.cultivar.allele_b", "Allele B");
    public static final String LABEL_VALUE = k("screen.cultivar.value", "Value");
    public static final String LABEL_STRAIN_SPECIES = k("screen.cultivar.strain_species", "Infects: %s");
    public static final String LABEL_STRAIN_DEFEATED = k("screen.cultivar.strain_defeated", "Defeated by expressed resistance alleles:");
    public static final String LABEL_STRAIN_STATS = k("screen.cultivar.strain_stats", "Transmission %s%%, lethal after %s blight ticks");
    public static final String LABEL_CARD_LINE = k("screen.cultivar.card_line", "Line #%s  %s");
    public static final String LABEL_TRUE_BREEDING = k("screen.cultivar.true_breeding", "true-breeding");
    public static final String LABEL_SEGREGATING = k("screen.cultivar.segregating", "segregating");

    public static final String UI_INPUT = k("ui.cultivar.input", "INPUT");
    public static final String UI_OUTPUT = k("ui.cultivar.output", "OUTPUT");
    public static final String UI_ETA = k("ui.cultivar.eta", "ETA %ss");
    public static final String UI_PAPER_COST = k("ui.cultivar.paper_cost", "1 paper per card");
    public static final String UI_LAB_INSERT = k("ui.cultivar.lab.insert", "INSERT A SAMPLE");
    public static final String UI_LAB_PAPER = k("ui.cultivar.lab.paper", "AWAITING PAPER");
    public static final String UI_LAB_COMPLETE = k("ui.cultivar.lab.complete", "ANALYSIS COMPLETE - COLLECT CARD");
    public static final String UI_LAB_SEQUENCING = k("ui.cultivar.lab.sequencing", "SEQUENCING");
    public static final String UI_LAB_ASSAY = k("ui.cultivar.lab.assay", "STRAIN ASSAY");
    public static final String UI_OUTCOMES = k("ui.cultivar.outcomes", "OUTCOME PER SEED");
    public static final String UI_OUTCOME_NOTE = k("ui.cultivar.outcome_note", "One roll per seed: destroyed, else one new allele, else unchanged.");
    public static final String UI_MUTATE = k("ui.cultivar.mutate", "New allele %s");
    public static final String UI_UNCHANGED = k("ui.cultivar.unchanged", "Unchanged %s");
    public static final String UI_DESTROYED = k("ui.cultivar.destroyed", "Destroyed %s");
    public static final String UI_GAUGE_CHARGE = k("ui.cultivar.gauge.charge", "CHG");
    public static final String UI_GAUGE_CURE = k("ui.cultivar.gauge.cure", "CURE");
    public static final String UI_COMPOST_NEXT = k("ui.cultivar.compost.next", "Compost in %ss");
    public static final String UI_COMPOST_NEEDS = k("ui.cultivar.compost.needs", "Needs %s more charge");
    public static final String UI_COMPOST_BLOCKED = k("ui.cultivar.compost.blocked", "Output full");
    public static final String UI_HISTORY_TEMP = k("ui.cultivar.history.temp", "TEMPERATURE HISTORY");
    public static final String UI_HISTORY_HUMIDITY = k("ui.cultivar.history.humidity", "HUMIDITY HISTORY");
    public static final String UI_HISTORY_NOTE = k("ui.cultivar.history.note", "Sampled while this screen is open; the dashed line is the target.");
    public static final String UI_SETPOINT = k("ui.cultivar.setpoint", "SETPOINT");
    public static final String UI_SHIFT_COARSE = k("ui.cultivar.shift_coarse", "Shift-click: steps of 0.5");
    public static final String UI_TEMPERATURE = k("ui.cultivar.temperature", "TEMPERATURE");
    public static final String UI_HUMIDITY = k("ui.cultivar.humidity", "HUMIDITY");
    public static final String UI_HUMIDITY_TARGET = k("ui.cultivar.humidity_target", "Device target %s%%");
    public static final String UI_SEALED = k("ui.cultivar.sealed", "SEALED");
    public static final String UI_BREACH = k("ui.cultivar.breach", "BREACH");
    public static final String UI_VOLUME = k("ui.cultivar.volume", "%s / %s blocks");
    public static final String UI_VOLUME_UNSEALED = k("ui.cultivar.volume_unsealed", "Leak or over %s blocks");
    public static final String UI_LIGHT = k("ui.cultivar.light", "LIGHT");
    public static final String UI_INSULATION = k("ui.cultivar.insulation", "INSULATION");
    public static final String UI_SEARCH = k("ui.cultivar.search", "Search lines");
    public static final String UI_SEARCH_HINT = k("ui.cultivar.search_hint", "Filter lines");
    public static final String UI_LINE_META = k("ui.cultivar.line_meta", "%s  |  line #%s  |  day %s");
    public static final String UI_BADGE_WILD = k("ui.cultivar.badge.wild", "WILD");
    public static final String UI_COPY_NEEDS_PAPER = k("ui.cultivar.copy_needs_paper", "Needs %s paper in the paper slot");
    public static final String UI_DISCARD = k("ui.cultivar.discard", "Discard this line (click twice)");
    public static final String UI_DISCARD_CONFIRM = k("ui.cultivar.discard_confirm", "Click again to discard permanently");
    public static final String UI_NO_MATCH = k("ui.cultivar.no_match", "No lines match");
    public static final String UI_STORED_DAY = k("ui.cultivar.stored_day", "Stored on day %s");
    public static final String TIP_GRADE = k("tooltip.cultivar.grade", "Grade: %s");
    public static final String GRADE_C = k("grade.cultivar.c", "C");
    public static final String GRADE_B = k("grade.cultivar.b", "B");
    public static final String GRADE_A = k("grade.cultivar.a", "A");
    public static final String GRADE_PRIME = k("grade.cultivar.prime", "Prime");
    public static final String TIP_RATION_EMPTY = k("tooltip.cultivar.ration_empty", "Craft from Linen Canvas and 3 to 8 Cultivar foods");
    public static final String TIP_RATION_STRENGTH = k("tooltip.cultivar.ration_strength", "Ration strength %s / 7");
    public static final String TIP_CERTIFIED = k("tooltip.cultivar.certified", "Certified by %s: score %s (%s ribbon)");
    public static final String TIP_CERTIFIED_RECORD = k("tooltip.cultivar.certified_record", "Species record holder");
    public static final String RIBBON_NONE = k("ribbon.cultivar.none", "no");
    public static final String RIBBON_BRONZE = k("ribbon.cultivar.bronze", "bronze");
    public static final String RIBBON_SILVER = k("ribbon.cultivar.silver", "silver");
    public static final String RIBBON_GOLD = k("ribbon.cultivar.gold", "gold");
    public static final String MSG_OILED = k("message.cultivar.oiled", "Oiled: this machine runs at double speed for %s s");
    public static final String SCREEN_EXCHANGE = k("container.cultivar.produce_exchange", "Produce Exchange");
    public static final String SCREEN_CONTRACTS = k("container.cultivar.contract_board", "Contract Board");
    public static final String SCREEN_PROCESSING = k("container.cultivar.processing_station", "Processing Station");
    public static final String SCREEN_GENE_EDITOR = k("container.cultivar.gene_editor", "Gene Editor");
    public static final String SCREEN_SHOW_BENCH = k("container.cultivar.show_bench", "Show Bench");
    public static final String CONTRACT_PRODUCE = k("contract.cultivar.produce", "%s %s, Grade %s or better");
    public static final String CONTRACT_TRAIT = k("contract.cultivar.trait", "A %s line with %s %s");
    public static final String CONTRACT_TRAIT_PURE = k("contract.cultivar.trait_pure", "A true-breeding %s line with %s %s");
    public static final String CONTRACT_ALLELE = k("contract.cultivar.allele", "A %s line carrying %s");
    public static final String MSG_CONTRACT_REJECTED = k("message.cultivar.contract_rejected", "That does not meet this contract.");
    public static final String MSG_CONTRACT_DONE = k("message.cultivar.contract_done", "Contract fulfilled: %s emeralds paid.");
    public static final String MSG_EDITOR_NOT_READY = k("message.cultivar.editor_not_ready",
            "Needs a seed packet with a genome, an Edit Reagent and an empty output slot.");
    public static final String MSG_EDITOR_UNKNOWN_ALLELE = k("message.cultivar.editor_unknown",
            "You have not recorded that allele in your Allele Atlas yet. Read a Genome Card that carries it.");
    public static final String MSG_EDITOR_SAME = k("message.cultivar.editor_same", "That copy already carries this allele.");
    public static final String MSG_BENCH_NOT_READY = k("message.cultivar.bench_not_ready",
            "Needs a seed packet with a genome, paper, and an empty output slot.");
    public static final String MSG_BENCH_RIBBON_FULL = k("message.cultivar.bench_ribbon_full", "Take the ribbon out of the ribbon slot first.");
    public static final String MSG_ATLAS_TIER = k("message.cultivar.atlas_tier", "Allele Atlas: %s alleles recorded. Reward granted!");
    public static final String HINT_PRIME_TITLE = k("hint.cultivar.prime.title", "Prime produce!");
    public static final String HINT_PRIME_BODY = k("hint.cultivar.prime.body", "Top-grade crops sell for 4x and win contracts.");
    public static final String HINT_PROCESSED_TITLE = k("hint.cultivar.processed.title", "Processed goods");
    public static final String HINT_PROCESSED_BODY = k("hint.cultivar.processed.body", "Processing keeps the grade and sells for more.");

    public static final String UI_HUD_CEILING = k("ui.cultivar.hud.ceiling", "Best grade these genes allow");
    public static final String UI_EX_CRATE = k("ui.cultivar.exchange.crate", "SHIPPING CRATE");
    public static final String UI_EX_PAYOUT = k("ui.cultivar.exchange.payout", "PAYOUT");
    public static final String UI_EX_PRODUCE = k("ui.cultivar.exchange.produce", "Produce");
    public static final String UI_EX_GOODS = k("ui.cultivar.exchange.goods", "Goods");
    public static final String UI_EX_HEADER_C = k("ui.cultivar.exchange.header_c", "GRADE C");
    public static final String UI_EX_HEADER_PRIME = k("ui.cultivar.exchange.header_prime", "PRIME");
    public static final String UI_EX_DEMAND = k("ui.cultivar.exchange.demand", "Demand %s%% (falls as you sell, recovers each day)");
    public static final String UI_EX_GRADES = k("ui.cultivar.exchange.grades", "C %s · B %s · A %s · Prime %s emeralds each");
    public static final String UI_EX_ESTIMATE = k("ui.cultivar.exchange.estimate", "Crate worth now: %s emeralds");
    public static final String UI_EX_DAWN = k("ui.cultivar.exchange.dawn", "Sells at dawn in %s");
    public static final String UI_EX_LAST = k("ui.cultivar.exchange.last", "Last sale: %s items for %s emeralds");
    public static final String UI_CB_DELIVER = k("ui.cultivar.contracts.deliver", "Deliver");
    public static final String UI_CB_BUY = k("ui.cultivar.contracts.buy", "Buy");
    public static final String UI_CB_DELIVERY = k("ui.cultivar.contracts.delivery", "DELIVER");
    public static final String UI_CB_PAYMENT = k("ui.cultivar.contracts.payment", "PAY");
    public static final String UI_CB_TAB_CONTRACTS = k("ui.cultivar.contracts.tab_contracts", "Contracts");
    public static final String UI_CB_TAB_SUPPLIES = k("ui.cultivar.contracts.tab_supplies", "Supplies");
    public static final String UI_CB_REPUTATION = k("ui.cultivar.contracts.reputation", "Reputation %s · Tier %s");
    public static final String UI_CB_EMPTY = k("ui.cultivar.contracts.empty", "New order at dawn");
    public static final String UI_CB_KIND_PRODUCE = k("ui.cultivar.contracts.kind_produce", "PRODUCE");
    public static final String UI_CB_KIND_TRAIT = k("ui.cultivar.contracts.kind_trait", "TRAIT");
    public static final String UI_CB_KIND_ALLELE = k("ui.cultivar.contracts.kind_allele", "ALLELE");
    public static final String UI_CB_REWARD = k("ui.cultivar.contracts.reward", "x %s");
    public static final String UI_CB_HELP_PRODUCE = k("ui.cultivar.contracts.help_produce",
            "Put the produce in the Deliver slot and press Deliver. Partial deliveries count.");
    public static final String UI_CB_HELP_TRAIT = k("ui.cultivar.contracts.help_trait",
            "Deliver one seed packet whose expressed gene meets the target. Check it with a Genome Card first.");
    public static final String UI_CB_HELP_ALLELE = k("ui.cultivar.contracts.help_allele",
            "Deliver one seed packet carrying this allele on either copy.");
    public static final String UI_CB_LOCKED = k("ui.cultivar.contracts.locked", "Unlocks at tier %s");
    public static final String UI_CB_PRICE = k("ui.cultivar.contracts.price", "x %s");
    public static final String UI_CB_DISCARD_HELP = k("ui.cultivar.contracts.discard_help",
            "Discard this order for 1 emerald. A new one arrives at dawn.");
    public static final String UI_CB_TIER_HELP = k("ui.cultivar.contracts.tier_help",
            "Every 4 fulfilled contracts raise the board one tier: bigger orders, better rewards, more supplies.");
    public static final String UI_PS_INPUT = k("ui.cultivar.processing.input", "INPUT");
    public static final String UI_PS_OUTPUT = k("ui.cultivar.processing.output", "OUTPUT");
    public static final String UI_PS_MAKES = k("ui.cultivar.processing.makes", "%s %s → %s %s");
    public static final String UI_PS_IDLE = k("ui.cultivar.processing.idle", "Insert produce to process it");
    public static final String UI_PS_NO_RECIPE = k("ui.cultivar.processing.no_recipe", "This item cannot be processed here");
    public static final String UI_PS_GRADE_KEPT = k("ui.cultivar.processing.grade_kept", "Grade kept: %s");
    public static final String UI_PS_UNGRADED = k("ui.cultivar.processing.ungraded", "Ungraded input");
    public static final String UI_PS_OILED = k("ui.cultivar.processing.oiled", "Oiled %ss");
    public static final String UI_PS_NOT_OILED = k("ui.cultivar.processing.not_oiled", "Not oiled");
    public static final String UI_PS_HELP_1 = k("ui.cultivar.processing.help_1", "Presses, mills and cures produce into goods.");
    public static final String UI_PS_HELP_2 = k("ui.cultivar.processing.help_2", "The output keeps the input's grade, so Prime in means Prime out.");
    public static final String UI_PS_HELP_3 = k("ui.cultivar.processing.help_3", "Goods sell for more than raw produce at the Produce Exchange.");
    public static final String UI_PS_OIL_HELP = k("ui.cultivar.processing.oil_help",
            "Use Soy Oil on any Cultivar machine to run it at double speed for a while.");
    public static final String UI_GE_EDIT = k("ui.cultivar.editor.edit", "Edit");
    public static final String UI_GE_PACKET = k("ui.cultivar.editor.packet", "PACKET");
    public static final String UI_GE_REAGENT = k("ui.cultivar.editor.reagent", "REAGENT");
    public static final String UI_GE_RESULT = k("ui.cultivar.editor.result", "EDITED");
    public static final String UI_GE_GENE = k("ui.cultivar.editor.gene", "Gene %s of %s");
    public static final String UI_GE_COPY_1 = k("ui.cultivar.editor.copy_1", "COPY 1");
    public static final String UI_GE_COPY_2 = k("ui.cultivar.editor.copy_2", "COPY 2");
    public static final String UI_GE_NO_PACKET = k("ui.cultivar.editor.no_packet", "No packet");
    public static final String UI_GE_SWITCH_COPY = k("ui.cultivar.editor.switch_copy", "Click to target this copy instead");
    public static final String UI_GE_REPLACE = k("ui.cultivar.editor.replace", "Write allele:");
    public static final String UI_GE_LOCKED = k("ui.cultivar.editor.locked", "Not in atlas");
    public static final String UI_GE_VALUE = k("ui.cultivar.editor.value", "Value %s");
    public static final String UI_GE_DOMINANCE = k("ui.cultivar.editor.dominance", "Dominance rank %s");
    public static final String UI_GE_LOCKED_HELP = k("ui.cultivar.editor.locked_help",
            "Read a Genome Card of a seed carrying this allele to record it in your Allele Atlas.");
    public static final String UI_GE_ATLAS = k("ui.cultivar.editor.atlas", "Atlas %s / %s");
    public static final String UI_GE_ATLAS_HELP = k("ui.cultivar.editor.atlas_help",
            "Alleles you have recorded. Rewards at 12, 24, 36 and the full set.");
    public static final String UI_GE_HINT = k("ui.cultivar.editor.hint", "Insert a sequenced seed packet");
    public static final String UI_SB_JUDGE = k("ui.cultivar.bench.judge", "Judge");
    public static final String UI_SB_ENTRY = k("ui.cultivar.bench.entry", "ENTRY");
    public static final String UI_SB_PAPER = k("ui.cultivar.bench.paper", "PAPER x%s");
    public static final String UI_SB_CERTIFIED = k("ui.cultivar.bench.certified", "CERTIFIED");
    public static final String UI_SB_RIBBON = k("ui.cultivar.bench.ribbon", "RIBBON");
    public static final String UI_SB_EMPTY = k("ui.cultivar.bench.empty", "Awaiting entry");
    public static final String UI_SB_SCALE = k("ui.cultivar.bench.scale", "Show score out of 100");
    public static final String UI_SB_THRESHOLDS = k("ui.cultivar.bench.thresholds", "Bronze %s · Silver %s · Gold %s");
    public static final String UI_SB_BREAKDOWN = k("ui.cultivar.bench.breakdown", "JUDGING");
    public static final String UI_SB_GENETIC = k("ui.cultivar.bench.genetic", "Genetic merit");
    public static final String UI_SB_TRUE = k("ui.cultivar.bench.true", "True-breeding bonus");
    public static final String UI_SB_TOTAL = k("ui.cultivar.bench.total", "Score");
    public static final String UI_SB_NEXT = k("ui.cultivar.bench.next", "%s points to the next ribbon");
    public static final String UI_SB_TOP = k("ui.cultivar.bench.top", "Top ribbon reached");
    public static final String UI_SB_HOW_1 = k("ui.cultivar.bench.how_1", "Enter one sequenced seed packet.");
    public static final String UI_SB_HOW_2 = k("ui.cultivar.bench.how_2", "Judging certifies it and awards");
    public static final String UI_SB_HOW_3 = k("ui.cultivar.bench.how_3", "a ribbon by genetic merit.");
    public static final String UI_SB_LAST = k("ui.cultivar.bench.last", "LAST CERTIFICATE");
    public static final String UI_SB_SLIP_SCORE = k("ui.cultivar.bench.slip_score", "Score %s / 100");
    public static final String UI_SB_SLIP_BY = k("ui.cultivar.bench.slip_by", "%s, day %s");
    public static final String UI_SB_NONE_YET = k("ui.cultivar.bench.none_yet", "Nothing judged yet");
    public static final String UI_SB_RECORD_HELP = k("ui.cultivar.bench.record_help", "Beat the world record for a species to earn bonus emeralds.");

    public static final String KEY_FIELD_MANUAL = k("key.cultivar.field_manual", "Open Field Manual");
    public static final String KEY_CATEGORY = k("key.category.cultivar.cultivar", "Cultivar");
    public static final String HINT_FOOTER = k("hint.cultivar.footer", "Press %s for the Field Manual");
    public static final String HINT_FOOTER_UNBOUND = k("hint.cultivar.footer_unbound", "Use your Field Manual to read more");
    public static final String HINT_PLANTED_TITLE = k("hint.cultivar.planted.title", "Planted!");
    public static final String HINT_PLANTED_BODY = k("hint.cultivar.planted.body", "This seed has a hidden genome. Look at it with a Hand Lens.");
    public static final String HINT_FLOWERING_TITLE = k("hint.cultivar.flowering.title", "A plant is flowering");
    public static final String HINT_FLOWERING_BODY = k("hint.cultivar.flowering.body", "Cross it with a Pollination Brush, or let the wind do it.");
    public static final String HINT_HARVESTED_TITLE = k("hint.cultivar.harvested.title", "New seed packets");
    public static final String HINT_HARVESTED_BODY = k("hint.cultivar.harvested.body", "Their genomes are unread. Analyse one in a Genome Lab.");
    public static final String HINT_CARD_TITLE = k("hint.cultivar.card.title", "Reading a Genome Card");
    public static final String HINT_CARD_BODY = k("hint.cultivar.card.body", "Glowing alleles are expressed. Try the Profile and Cross tabs.");
    public static final String HINT_DEPLETED_TITLE = k("hint.cultivar.depleted.title", "Soil depleted");
    public static final String HINT_DEPLETED_BODY = k("hint.cultivar.depleted.body", "Restore it with Compost, and rotate in nitrogen fixers.");
    public static final String HINT_BLIGHT_TITLE = k("hint.cultivar.blight.title", "Blight nearby!");
    public static final String HINT_BLIGHT_BODY = k("hint.cultivar.blight.body", "Identical plants spread it fastest. Isolate and burn it.");
    public static final String HINT_WILD_TITLE = k("hint.cultivar.wild.title", "Wild plants");
    public static final String HINT_WILD_BODY = k("hint.cultivar.wild.body", "Wild alleles bring resistance. Cross them into your lines.");

    public static final String TIP_FIELD_MANUAL = k("tooltip.cultivar.field_manual", "An illustrated guide to breeding, soil, blight and the lab");
    public static final String MANUAL_TITLE = k("manual.cultivar.title", "FIELD MANUAL");
    public static final String MANUAL_SUBTITLE = k("manual.cultivar.subtitle", "Breeder's handbook");
    public static final String MANUAL_TASKS = k("manual.cultivar.tasks", "CHAPTER GOALS");
    public static final String MANUAL_PAGE = k("manual.cultivar.page", "%s / %s");
    public static final String MANUAL_HINTS_ON = k("manual.cultivar.hints_on", "Hints: on");
    public static final String MANUAL_HINTS_OFF = k("manual.cultivar.hints_off", "Hints: off");
    public static final String MANUAL_KEY = k("manual.cultivar.key", "Key: %s");
    public static final String MANUAL_RECIPE = k("manual.cultivar.recipe", "CRAFTING");
    public static final String MANUAL_RECIPE_LOADING = k("manual.cultivar.recipe_loading", "Fetching recipe...");
    public static final String MANUAL_RECIPE_NONE = k("manual.cultivar.recipe_none", "Not crafted: found in the world or made by a machine.");
    public static final String MANUAL_RECIPE_OFFLINE = k("manual.cultivar.recipe_offline", "Recipes appear when you are in a world.");
    public static final String MANUAL_SHAPELESS = k("manual.cultivar.shapeless", "any arrangement");
    public static final String MANUAL_TIP = k("manual.cultivar.tip", "TIP");

    public static final String CH_START = k("manual.cultivar.ch.start", "Getting Started");
    public static final String CH_GENES = k("manual.cultivar.ch.genes", "Genes & Alleles");
    public static final String CH_POLLINATION = k("manual.cultivar.ch.pollination", "Pollination");
    public static final String CH_SOIL = k("manual.cultivar.ch.soil", "Soil Chemistry");
    public static final String CH_BLIGHT = k("manual.cultivar.ch.blight", "Blight");
    public static final String CH_LAB = k("manual.cultivar.ch.lab", "The Laboratory");
    public static final String CH_GREENHOUSE = k("manual.cultivar.ch.greenhouse", "Greenhouses");
    public static final String CH_MUTATION = k("manual.cultivar.ch.mutation", "Mutation & Grafting");
    public static final String CH_CANOPY = k("manual.cultivar.ch.canopy", "The Canopy");

    public static final String GOAL_PLANTED = k("manual.cultivar.goal.planted", "Plant a seed packet");
    public static final String GOAL_HARVESTED = k("manual.cultivar.goal.harvested", "Harvest a mature crop");
    public static final String GOAL_READ = k("manual.cultivar.goal.read_the_code", "Read a Genome Card");
    public static final String GOAL_DOMINANT = k("manual.cultivar.goal.dominant", "Find a plant hiding a recessive allele");
    public static final String GOAL_FLOWERING = k("manual.cultivar.goal.flowering", "Grow a plant to flowering");
    public static final String GOAL_FIRST_CROSS = k("manual.cultivar.goal.first_cross", "Harvest a cross-pollinated plant");
    public static final String GOAL_TRUE_LINE = k("manual.cultivar.goal.true_line", "Breed a true-breeding line from a cross");
    public static final String GOAL_ROTATION = k("manual.cultivar.goal.rotation", "Rotate a nitrogen fixer with another crop");
    public static final String GOAL_BLIGHT = k("manual.cultivar.goal.blight", "Witness a blight outbreak");
    public static final String GOAL_RESISTANT = k("manual.cultivar.goal.resistant", "Harvest a plant with 75%+ blight resistance");
    public static final String GOAL_BANKED = k("manual.cultivar.goal.banked", "Store a line in a Seed Bank");
    public static final String GOAL_OUT_OF_SEASON = k("manual.cultivar.goal.out_of_season", "Harvest a crop only a greenhouse could grow");
    public static final String GOAL_MUTANT = k("manual.cultivar.goal.mutant", "Breed a seed carrying a mutant allele");
    public static final String GOAL_GRAFTED = k("manual.cultivar.goal.grafted", "Make a Graft Union");
    public static final String GOAL_CANOPY = k("manual.cultivar.goal.canopy", "Enter the Canopy");
    public static final String GOAL_WILD_BLOOD = k("manual.cultivar.goal.wild_blood", "Breed wild alleles into a productive line");

    public static final String FX_YIELD = k("manual.cultivar.fx.yield", "how much produce a harvest gives");
    public static final String FX_GROWTH = k("manual.cultivar.fx.growth_rate", "how fast each stage passes");
    public static final String FX_COLD = k("manual.cultivar.fx.cold_tolerance", "lowest temperature it grows in");
    public static final String FX_DROUGHT = k("manual.cultivar.fx.drought_tolerance", "lowest moisture it grows in");
    public static final String FX_LIGHT = k("manual.cultivar.fx.light_need", "lowest light it grows in");
    public static final String FX_BLIGHT = k("manual.cultivar.fx.blight_resistance", "chance to shrug off each infection");
    public static final String FX_NUTRIENT = k("manual.cultivar.fx.nutrient_draw", "how much soil each stage uses");
    public static final String FX_MUTATION = k("manual.cultivar.fx.mutation_rate", "chance a passed-on allele mutates");

    public static final String DG_EXPRESSED = k("manual.cultivar.dg.expressed", "expressed");
    public static final String DG_HIDDEN = k("manual.cultivar.dg.hidden", "hidden");
    public static final String DG_BLENDED = k("manual.cultivar.dg.blended", "blended");
    public static final String DG_DOMINANT = k("manual.cultivar.dg.dominant", "Dominant wins");
    public static final String DG_CODOMINANT = k("manual.cultivar.dg.codominant", "Equal ranks average");
    public static final String DG_RECESSIVE = k("manual.cultivar.dg.recessive", "Two recessives show");
    public static final String DG_WIND = k("manual.cultivar.dg.wind", "WIND");
    public static final String DG_SCREEN = k("manual.cultivar.dg.screen", "screen");
    public static final String DG_RATIO = k("manual.cultivar.dg.ratio", "3 : 1");
    public static final String DG_SHOWS_DOMINANT = k("manual.cultivar.dg.shows_dominant", "75% show the dominant trait");
    public static final String DG_SHOWS_RECESSIVE = k("manual.cultivar.dg.shows_recessive", "25% show the recessive trait");
    public static final String DG_CEREAL = k("manual.cultivar.dg.cereal", "wheatline draws N");
    public static final String DG_FIXER = k("manual.cultivar.dg.fixer", "clover fixes N");
    public static final String DG_CLONES = k("manual.cultivar.dg.clones", "Clones");
    public static final String DG_MIXED = k("manual.cultivar.dg.mixed", "Mixed genotypes");
    public static final String DG_SEALED = k("manual.cultivar.dg.sealed", "sealed");
    public static final String DG_LEAK = k("manual.cultivar.dg.leak", "leak!");
    public static final String DG_ROOTSTOCK = k("manual.cultivar.dg.rootstock", "ROOTSTOCK gives");
    public static final String DG_SCION = k("manual.cultivar.dg.scion", "SCION gives");
    public static final String DG_UNION = k("manual.cultivar.dg.union", "Graft Union");
    public static final String DG_PORTAL = k("manual.cultivar.dg.portal", "breach ring");
    public static final String DG_F1 = k("manual.cultivar.dg.f1", "wild x cultivated");
    public static final String DG_F2 = k("manual.cultivar.dg.f2", "F1 x F1: 1 in 16 keeps both");
    public static final String DG_AND = k("manual.cultivar.dg.and", "and");
    public static final String DG_INSERT = k("manual.cultivar.dg.insert", "insert into");
    public static final String DG_PRINTS = k("manual.cultivar.dg.prints", "prints");
    public static final String DG_COLLECT = k("manual.cultivar.dg.collect", "collect");
    public static final String DG_APPLY = k("manual.cultivar.dg.apply", "apply");
    public static final String DG_HARVEST = k("manual.cultivar.dg.harvest", "harvest");
    public static final String DG_MUTATED = k("manual.cultivar.dg.mutated", "one allele changed");

    public static final String P_START_1_T = k("manual.cultivar.start.1.title", "Welcome, breeder");
    public static final String P_START_1_A = k("manual.cultivar.start.1.a",
            "Every Cultivar seed carries a genome: eight genes, each with two alleles. Together they decide how much a plant yields, how fast it grows, what cold and drought it survives, how much light it needs, how well it fights blight, how hungry it is, and how often its seed mutates.");
    public static final String P_START_1_B = k("manual.cultivar.start.1.b",
            "Crossed plants pass their alleles on by real Mendelian rules. Your job is to breed cultivars that do exactly what you need.");
    public static final String P_START_2_T = k("manual.cultivar.start.2.title", "Getting seeds");
    public static final String P_START_2_A = k("manual.cultivar.start.2.a",
            "Craft a seed packet from a vanilla seed and paper, break grass for natural packets, or loot old trial fields and seed stores. A packet's genome is fixed the first time it is carried or planted.");
    public static final String P_START_2_B = k("manual.cultivar.start.2.b",
            "Hoe farmland and use the packet on it. The farmland becomes Managed Soil, which tracks the nutrients and pH under that plant.");
    public static final String P_START_3_T = k("manual.cultivar.start.3.title", "Tools of the trade");
    public static final String P_START_3_A = k("manual.cultivar.start.3.a",
            "The Hand Lens shows a crop's expressed traits, the Soil Probe reads soil, the Pollination Brush makes crosses and the Isolation Bag prevents them. Hold a Hand Lens or Soil Probe and look at a crop: a scanner panel reads it out beside the crosshair.");
    public static final String P_START_3_TIP = k("manual.cultivar.start.3.tip",
            "Press the Field Manual key (J by default) at any time. After a hint, the manual opens at the chapter that explains it.");

    public static final String P_GENES_1_T = k("manual.cultivar.genes.1.title", "Eight genes");
    public static final String P_GENES_1_A = k("manual.cultivar.genes.1.a",
            "Each allele has a value and a dominance rank. What the plant actually shows, its phenotype, comes from the pair.");
    public static final String P_GENES_2_T = k("manual.cultivar.genes.2.title", "Dominance");
    public static final String P_GENES_2_A = k("manual.cultivar.genes.2.a",
            "The allele with the higher dominance rank is expressed and hides the other. Equal ranks blend, and the plant shows the average. Many of the best cultivated alleles are recessive, so a plant can carry them invisibly: only a Genome Lab reveals them.");
    public static final String P_GENES_3_T = k("manual.cultivar.genes.3.title", "Rare alleles");
    public static final String P_GENES_3_A = k("manual.cultivar.genes.3.a",
            "Wild alleles exist only in Canopy plants: tough and resistant, but poor yielders. Mutation-only alleles never occur naturally; they appear through spontaneous mutation or the Mutagen Chamber, and include some of the strongest alleles in the game.");
    public static final String P_GENES_3_TIP = k("manual.cultivar.genes.3.tip",
            "On a Genome Card a green tag marks a wild allele and a violet tag a mutant one.");

    public static final String P_POLL_1_T = k("manual.cultivar.pollination.1.title", "Flowering and wind");
    public static final String P_POLL_1_A = k("manual.cultivar.pollination.1.a",
            "At stage 4 a plant flowers. Wind carries pollen between flowering plants of the same species a few blocks apart, favouring plants downwind. An Isolation Screen between two plants blocks their pollen completely.");
    public static final String P_POLL_1_B = k("manual.cultivar.pollination.1.b",
            "A plant that is never pollinated sets seed identical to itself, apart from rare mutation.");
    public static final String P_POLL_2_T = k("manual.cultivar.pollination.2.title", "Controlled crosses");
    public static final String P_POLL_2_A = k("manual.cultivar.pollination.2.a",
            "Use a Pollination Brush on one flowering plant to collect pollen, then on another of the same species to cross them. An Isolation Bag stops a plant being pollinated at all. A fuelled Pollination Unit sends drones to make crosses for you.");
    public static final String P_POLL_3_T = k("manual.cultivar.pollination.3.title", "Reading the odds");
    public static final String P_POLL_3_A = k("manual.cultivar.pollination.3.a",
            "Each parent passes one of its two alleles at random, gene by gene. Cross two plants that each carry one dominant and one recessive allele, and a quarter of the offspring show the recessive trait: the classic 3 to 1. A Genome Card's Cross tab works these odds out exactly.");

    public static final String P_SOIL_1_T = k("manual.cultivar.soil.1.title", "Nutrients and pH");
    public static final String P_SOIL_1_A = k("manual.cultivar.soil.1.a",
            "Managed Soil holds nitrogen, phosphorus and potassium from 0 to 100, plus a pH. Each growth stage uses nutrients, twice as much of the one the species prefers. Outside its pH band a crop slows down; far outside, it suffers.");
    public static final String P_SOIL_2_T = k("manual.cultivar.soil.2.title", "Keeping soil alive");
    public static final String P_SOIL_2_A = k("manual.cultivar.soil.2.a",
            "When any nutrient reaches zero the soil turns to Depleted Soil, and nothing grows until Compost restores it. Clover, soybean and tuber fix nitrogen as they grow, so rotating them with hungry crops keeps a field productive.");
    public static final String P_SOIL_3_T = k("manual.cultivar.soil.3.title", "Automating the field");
    public static final String P_SOIL_3_A = k("manual.cultivar.soil.3.a",
            "The Compost Bin turns plant waste into Compost. Lime and Sulfur Hoppers nudge nearby soil pH up or down, the Nutrient Spreader feeds the neediest soil around it, and Irrigation Lines and Sprinklers keep the ground moist.");

    public static final String P_BLIGHT_1_T = k("manual.cultivar.blight.1.title", "How blight spreads");
    public static final String P_BLIGHT_1_A = k("manual.cultivar.blight.1.a",
            "Each blight strain attacks certain species. An infected plant spreads it to nearby plants of its species: faster in wet soil and dense planting, and up to four times faster through genetically identical plants. Untreated, it kills the plant and depletes the soil.");
    public static final String P_BLIGHT_2_T = k("manual.cultivar.blight.2.title", "Defences");
    public static final String P_BLIGHT_2_A = k("manual.cultivar.blight.2.a",
            "Blight resistance gives every infection attempt a chance to fail, and some alleles make a plant immune to particular strains. Mix genotypes, rotate species, raise Isolation Screens and space plantings out. A fuelled Burn Pan removes an infected plant safely and keeps a Blight Sample.");
    public static final String P_BLIGHT_2_TIP = k("manual.cultivar.blight.2.tip",
            "Analyse a Blight Sample in the Genome Lab: its strain report lists exactly which alleles defeat it.");

    public static final String P_LAB_1_T = k("manual.cultivar.lab.1.title", "The Genome Lab");
    public static final String P_LAB_1_A = k("manual.cultivar.lab.1.a",
            "Put a seed packet or Wild Seed and a sheet of paper into a Genome Lab. After a short analysis it prints a Genome Card with the full genotype, and you keep the sample.");
    public static final String P_LAB_2_T = k("manual.cultivar.lab.2.title", "Reading a Genome Card");
    public static final String P_LAB_2_A = k("manual.cultivar.lab.2.a",
            "Use a card to open its report. Genotype shows every allele pair and which allele is expressed. Profile charts the plant's strengths. Cross shows an exact Punnett square against the card in your other hand.");
    public static final String P_LAB_2_TIP = k("manual.cultivar.lab.2.tip",
            "A line whose two alleles match at every gene is true-breeding: its seed grows into exact copies.");
    public static final String P_LAB_3_T = k("manual.cultivar.lab.3.title", "Keeping records");
    public static final String P_LAB_3_A = k("manual.cultivar.lab.3.a",
            "A Seed Bank stores named lines with their genome and pedigree; Seed Vault Drawers beside it add room, and each copy costs paper. The Field Notebook logs every cross you harvest, a Pedigree Sheet prints a line's ancestry, and Trial Markers number your test plots.");

    public static final String P_GREEN_1_T = k("manual.cultivar.greenhouse.1.title", "Sealing a greenhouse");
    public static final String P_GREEN_1_A = k("manual.cultivar.greenhouse.1.a",
            "A Climate Controller measures the air space it sits in. Enclose it with Greenhouse Panes, glass, closed doors or solid blocks. If air escapes, or the space is too large, it reports a breach. Greenhouse Panes insulate best.");
    public static final String P_GREEN_2_T = k("manual.cultivar.greenhouse.2.title", "Controlling climate");
    public static final String P_GREEN_2_A = k("manual.cultivar.greenhouse.2.a",
            "Inside a sealed greenhouse the temperature drifts toward the controller's setpoint, Humidifiers and Dehumidifiers shift the humidity, and any Grow Light keeps light at 14. Crops that cannot grow outside can thrive in there all year.");

    public static final String P_MUT_1_T = k("manual.cultivar.mutation.1.title", "Inducing mutation");
    public static final String P_MUT_1_A = k("manual.cultivar.mutation.1.a",
            "The Mutagen Chamber treats seed packets with a mutagen vial; the Thermal Shock Unit uses ice instead. Each seed is destroyed, gains one new random allele (possibly a mutation-only one), or comes out unchanged. Analyse the survivors to see what changed.");
    public static final String P_MUT_2_T = k("manual.cultivar.mutation.2.title", "Grafting");
    public static final String P_MUT_2_A = k("manual.cultivar.mutation.2.a",
            "Use a Grafting Knife on a mature rootstock plant, then on an adjacent mature plant of the same species. The Graft Union shows the rootstock's hardiness with the scion's productivity, regrows after each harvest, and never sets seed.");

    public static final String P_CANOPY_1_T = k("manual.cultivar.canopy.1.title", "Above the trees");
    public static final String P_CANOPY_1_A = k("manual.cultivar.canopy.1.a",
            "Jungles and forests hide Canopy Breaches: rings of eight portal frames. Canopy Resin turns up in Canopy Platform chests, jungle temples and woodland mansions. Craft a Canopy Key and use it on a complete ring to open the way.");
    public static final String P_CANOPY_2_T = k("manual.cultivar.canopy.2.title", "Wild blood");
    public static final String P_CANOPY_2_A = k("manual.cultivar.canopy.2.a",
            "Canopy plants carry wild alleles: superb blight resistance and tolerance, but dominant poor yield and growth. Cross wild into your line, then cross the offspring together: about 1 in 16 grandchildren keep the wild resistance with good yield. Wild resistance blends, so look for plants carrying two copies.");

    public static final String CH_MARKET = k("manual.cultivar.ch.market", "The Market");
    public static final String CH_PROCESSING = k("manual.cultivar.ch.processing", "Processing");
    public static final String CH_MASTERY = k("manual.cultivar.ch.mastery", "Mastery");
    public static final String GOAL_PRIME = k("manual.cultivar.goal.prime", "Harvest Prime produce");
    public static final String GOAL_FIRST_SALE = k("manual.cultivar.goal.first_sale", "Sell at a Produce Exchange");
    public static final String GOAL_FIRST_CONTRACT = k("manual.cultivar.goal.first_contract", "Fulfil a contract");
    public static final String GOAL_PROCESSED = k("manual.cultivar.goal.processed", "Process a crop into goods");
    public static final String GOAL_RATION = k("manual.cultivar.goal.ration", "Eat a Ration Pack");
    public static final String GOAL_ATLAS = k("manual.cultivar.goal.atlas", "Record 12 alleles in your Atlas");
    public static final String GOAL_GENE_EDIT = k("manual.cultivar.goal.gene_edit", "Edit an allele");
    public static final String GOAL_GOLD_RIBBON = k("manual.cultivar.goal.gold_ribbon", "Win a gold ribbon");
    public static final String GOAL_SHOW_RECORD = k("manual.cultivar.goal.show_record", "Set a species record");
    public static final String DG_GENES_CARE = k("manual.cultivar.dg.genes_care", "score = 55% genes + 45% care");
    public static final String DG_FLOOD = k("manual.cultivar.dg.flood", "flooded");
    public static final String DG_RECOVER = k("manual.cultivar.dg.recover", "recovers");
    public static final String DG_DAYS = k("manual.cultivar.dg.days", "days \u2192");
    public static final String DG_PRESSES = k("manual.cultivar.dg.presses", "presses");
    public static final String P_MARKET_1_T = k("manual.cultivar.market.1.title", "Grades");
    public static final String P_MARKET_1_A = k("manual.cultivar.market.1.a",
            "Every harvest is graded C, B, A or Prime. The grade blends the plant's genes with how well it was grown: soil nutrients, pH, water, light and temperature, and any stress along the way. Good genes set the ceiling; good care reaches it.");
    public static final String P_MARKET_1_TIP = k("manual.cultivar.market.1.tip",
            "The Hand Lens shows the best grade a plant's genes allow. Higher grades are more filling, and Prime food also gives Regeneration.");
    public static final String P_MARKET_2_T = k("manual.cultivar.market.2.title", "The Produce Exchange");
    public static final String P_MARKET_2_A = k("manual.cultivar.market.2.a",
            "Fill the Exchange's crate and it sells everything at dawn for emeralds. Prime pays four times Grade C. Selling a lot of one product lowers its price for a few days, so a varied farm earns more than a single crop.");
    public static final String P_MARKET_3_T = k("manual.cultivar.market.3.title", "Contracts");
    public static final String P_MARKET_3_A = k("manual.cultivar.market.3.a",
            "A Contract Board posts three orders a day: produce of a minimum grade, a line that expresses a trait, or a line carrying a named allele. Each fulfilled order pays emeralds and builds reputation. Every four contracts raise the board a tier, which unlocks bigger orders and the Supplies tab's rare items.");
    public static final String P_PROC_1_T = k("manual.cultivar.proc.1.title", "The Processing Station");
    public static final String P_PROC_1_A = k("manual.cultivar.proc.1.a",
            "The Processing Station presses, mills and cures produce into goods. The output keeps the input's grade, so Prime soybeans make Prime oil. Goods are worth more than the produce that went into them.");
    public static final String P_PROC_2_T = k("manual.cultivar.proc.2.title", "Goods");
    public static final String P_PROC_2_A = k("manual.cultivar.proc.2.a",
            "Wheatline mills into Field Meal; maize or tuber into Starch, and Starch into Bioplastic. Soybeans press into Soy Oil, flax weaves into Linen Canvas, clover packs into Biofuel Cake, brassica pickles and gourd purees. Hold Shift over any good to see its recipe.");
    public static final String P_PROC_2_TIP = k("manual.cultivar.proc.2.tip",
            "Use Soy Oil on any Cultivar machine to run it at double speed for a while.");
    public static final String P_PROC_3_T = k("manual.cultivar.proc.3.title", "Ration Packs");
    public static final String P_PROC_3_A = k("manual.cultivar.proc.3.a",
            "Wrap 3 to 8 Cultivar foods in Linen Canvas to make a Ration Pack. Its strength rises with every extra crop species in it and with its lowest grade.");
    public static final String P_PROC_3_B = k("manual.cultivar.proc.3.b",
            "A strong ration gives Regeneration, and the strongest add Resistance. A broad, well-grown farm feeds you best.");
    public static final String P_MASTERY_1_T = k("manual.cultivar.mastery.1.title", "The Allele Atlas");
    public static final String P_MASTERY_1_A = k("manual.cultivar.mastery.1.a",
            "Every allele you read on a Genome Card is recorded in your Allele Atlas, shown above. The atlas pays out at 12, 24 and 36 alleles and for the full set. Cells marked ? are mutation-only alleles: find them with the Mutagen Chamber.");
    public static final String P_MASTERY_2_T = k("manual.cultivar.mastery.2.title", "The Gene Editor");
    public static final String P_MASTERY_2_A = k("manual.cultivar.mastery.2.a",
            "The Gene Editor rewrites one copy of one gene on a seed packet with an allele from your atlas, using one Edit Reagent. Build it from a Gene Editor Blueprint, sold by a tier 4 Contract Board.");
    public static final String P_MASTERY_3_T = k("manual.cultivar.mastery.3.title", "The Show Bench");
    public static final String P_MASTERY_3_A = k("manual.cultivar.mastery.3.a",
            "The Show Bench judges a seed packet's genes out of 100, with a bonus for true-breeding lines. It certifies the packet and awards a bronze, silver or gold ribbon. Beat the world's best score for a species to set a record and earn emeralds.");

    public static final String UI_HUD_FLOWERING = k("ui.cultivar.hud.flowering", "FLOWERING");
    public static final String UI_HUD_POLLINATED = k("ui.cultivar.hud.pollinated", "POLLINATED");
    public static final String UI_HUD_BAGGED = k("ui.cultivar.hud.bagged", "BAGGED");
    public static final String UI_HUD_GRAFTED = k("ui.cultivar.hud.grafted", "GRAFT");
    public static final String UI_HUD_BLIGHTED = k("ui.cultivar.hud.blighted", "BLIGHT: %s");
    public static final String UI_HUD_UNSAMPLED = k("ui.cultivar.hud.unsampled", "Natural plant, not yet sampled");
    public static final String UI_HUD_UNSAMPLED_HINT = k("ui.cultivar.hud.unsampled_hint", "Use the Hand Lens on it to read its traits");
    public static final String UI_HUD_GENOTYPE = k("ui.cultivar.hud.genotype", "TRAITS AND ALLELES");
    public static final String UI_HUD_TRAITS = k("ui.cultivar.hud.traits", "EXPRESSED TRAITS");
    public static final String UI_HUD_SOIL = k("ui.cultivar.hud.soil", "Managed Soil");
    public static final String UI_HUD_MOISTURE = k("ui.cultivar.hud.moisture", "MOISTURE");
    public static final String UI_CROSS_PARENTS = k("ui.cultivar.cross.parents", "PARENT GENOMES");
    public static final String UI_CROSS_THIS = k("ui.cultivar.cross.this", "this");
    public static final String UI_CROSS_POLLEN = k("ui.cultivar.cross.pollen", "pollen");
    public static final String UI_HUD_EXHAUSTED = k("ui.cultivar.hud.exhausted", "A nutrient is exhausted");
    public static final String UI_TAB_GENOTYPE = k("ui.cultivar.tab.genotype", "GENOTYPE");
    public static final String UI_TAB_PROFILE = k("ui.cultivar.tab.profile", "PROFILE");
    public static final String UI_TAB_CROSS = k("ui.cultivar.tab.cross", "CROSS");
    public static final String UI_LEGEND_GENOTYPE = k("ui.cultivar.legend.genotype",
            "Glowing: expressed  |  dashed: hidden  |  blue: codominant  |  dots: dominance rank  |  green tag: wild, violet tag: mutant");
    public static final String UI_FAVOURABILITY = k("ui.cultivar.favourability", "%s%% of the favourable range");
    public static final String UI_PROFILE_SCORE = k("ui.cultivar.profile.score", "MEAN FAVOURABILITY");
    public static final String UI_STAT_HETERO = k("ui.cultivar.stat.hetero", "Heterozygous loci");
    public static final String UI_STAT_WILD = k("ui.cultivar.stat.wild", "Wild alleles carried");
    public static final String UI_STAT_MUTANT = k("ui.cultivar.stat.mutant", "Mutant alleles carried");
    public static final String UI_STAT_MUTATION = k("ui.cultivar.stat.mutation", "Mutation chance per offspring");
    public static final String UI_STAT_MUTATION_NOTE = k("ui.cultivar.stat.mutation_note",
            "At least one of the 8 alleles it passes on");
    public static final String UI_STAT_HIDDEN = k("ui.cultivar.stat.hidden", "HIDDEN ALLELES CARRIED");
    public static final String UI_SELF_CLONAL = k("ui.cultivar.self_clonal", "Self-pollinated offspring are clones; cross to segregate.");
    public static final String UI_CROSS_PARTNER = k("ui.cultivar.cross.partner", "Crossing with the card in your other hand: line #%s");
    public static final String UI_CROSS_SIBLING = k("ui.cultivar.cross.sibling", "Crossing with an identical plant (cross-pollinated sibling)");
    public static final String UI_CROSS_HINT = k("ui.cultivar.cross.hint", "Hold another Genome Card of the same species in your other hand to cross against it.");
    public static final String UI_CROSS_SEGREGATES = k("ui.cultivar.cross.segregates", "segregates");
    public static final String UI_CROSS_FIXED = k("ui.cultivar.cross.fixed", "breeds true");
    public static final String UI_CROSS_MOTHER = k("ui.cultivar.cross.mother", "THIS PLANT");
    public static final String UI_CROSS_FATHER = k("ui.cultivar.cross.father", "POLLEN PARENT");
    public static final String UI_CROSS_OFFSPRING = k("ui.cultivar.cross.offspring", "OFFSPRING PHENOTYPES");
    public static final String UI_CROSS_ODDS = k("ui.cultivar.cross.odds", "WHOLE-GENOME ODDS");
    public static final String UI_CROSS_HOMOZYGOUS_GENE = k("ui.cultivar.cross.homozygous_gene", "Homozygous here");
    public static final String UI_CROSS_PURE_ALL = k("ui.cultivar.cross.pure_all", "Pure at all 8 loci");
    public static final String UI_CROSS_MEAN_FAV = k("ui.cultivar.cross.mean_fav", "Mean favourability");
    public static final String UI_CROSS_GENE_FAV = k("ui.cultivar.cross.gene_fav", "This gene, expected");
    public static final String UI_CROSS_EXACT = k("ui.cultivar.cross.exact", "Exact odds, mutation excluded");
    public static final String UI_STRAIN_REPORT = k("ui.cultivar.strain.report", "BLIGHT STRAIN REPORT");
    public static final String UI_STRAIN_BEHAVIOUR = k("ui.cultivar.strain.behaviour", "BEHAVIOUR");
    public static final String UI_STRAIN_TRANSMISSION = k("ui.cultivar.strain.transmission", "TRANSMISSION");
    public static final String UI_STRAIN_LETHAL = k("ui.cultivar.strain.lethal", "Lethal after (blight ticks)");
    public static final String UI_STRAIN_SPONTANEOUS = k("ui.cultivar.strain.spontaneous", "Spontaneous outbreak, per check");
    public static final String UI_STRAIN_HOSTS = k("ui.cultivar.strain.hosts", "HOST SPECIES");
    public static final String UI_STRAIN_NOTE = k("ui.cultivar.strain.note",
            "Expressing any listed allele makes a plant immune. Genetically uniform plantings spread blight fastest.");

    public static final String READOUT_SEALED = k("screen.cultivar.readout_sealed", "SEALED %s");
    public static final String READOUT_UNSEALED = k("screen.cultivar.readout_unsealed", "UNSEALED");
    public static final String READOUT_TEMP = k("screen.cultivar.readout_temp", "T %s > %s");
    public static final String READOUT_HUMIDITY = k("screen.cultivar.readout_humidity", "RH %s%%  L%s");
    public static final String READOUT_LINES = k("screen.cultivar.readout_lines", "%s lines");

    public static final String CMD_NO_TARGET = k("commands.cultivar.no_target", "Look at a Cultivar crop.");
    public static final String CMD_BAD_GENOME = k("commands.cultivar.bad_genome", "A genome must be 32 hex characters.");
    public static final String CMD_PUNNETT_START = k("commands.cultivar.punnett.start", "Running %s simulated crosses...");
    public static final String CMD_SEED_GIVEN = k("commands.cultivar.seed_given", "Gave a %s seed packet with genome %s.");

    public static final String JEI_COMPOSTING = k("jei.cultivar.composting", "Composting");
    public static final String JEI_RATION = k("jei.cultivar.ration", "Ration Packing");
    public static final String JEI_GENOME_LAB = k("jei.cultivar.genome_lab", "Genome Analysis");
    public static final String JEI_MUTAGEN = k("jei.cultivar.mutagen", "Chemical Mutagenesis");
    public static final String JEI_THERMAL = k("jei.cultivar.thermal", "Thermal Shock");
    public static final String JEI_BURN_PAN = k("jei.cultivar.burn_pan", "Blight Burning");
    public static final String JEI_SUPPLIES = k("jei.cultivar.supplies", "Contract Board Supplies");
    public static final String JEI_PROCESSING_TIME = k("jei.cultivar.processing_time", "%s s per batch (half with Soy Oil)");
    public static final String JEI_GRADE_KEPT = k("jei.cultivar.grade_kept", "The output keeps the input's grade.");
    public static final String JEI_COMPOST_CHARGE = k("jei.cultivar.compost_charge", "+%s charge. %s charge makes 1 Compost.");
    public static final String JEI_RATION_NOTE_1 = k("jei.cultivar.ration_note_1", "Strength %s / 7");
    public static final String JEI_RATION_NOTE_2 = k("jei.cultivar.ration_note_2",
            "Any 3 to 8 Cultivar foods. More species and higher grades make it stronger.");
    public static final String JEI_LAB_SEED = k("jei.cultivar.lab_seed", "Prints the seed's full genome.");
    public static final String JEI_LAB_STRAIN = k("jei.cultivar.lab_strain", "Prints a strain report: which alleles resist it.");
    public static final String JEI_LAB_KEPT = k("jei.cultivar.lab_kept", "Uses one paper. The sample is given back.");
    public static final String JEI_MUTATION_ODDS = k("jei.cultivar.mutation_odds", "Per seed: new allele %s, destroyed %s, unchanged %s");
    public static final String JEI_MUTAGEN_NOTE = k("jei.cultivar.mutagen_note", "One vial treats %s seeds, %s s each.");
    public static final String JEI_THERMAL_NOTE = k("jei.cultivar.thermal_note", "Cooled with ice or snow blocks. %s s per seed.");
    public static final String JEI_BURN_NOTE_1 = k("jei.cultivar.burn_note_1", "While fuelled, burns blighted plants within %s blocks.");
    public static final String JEI_BURN_NOTE_2 = k("jei.cultivar.burn_note_2", "Soil is left intact. Each fuel burns %s plants and stores a sample.");
    public static final String JEI_EDITOR_NOTE_1 = k("jei.cultivar.editor_note_1", "Rewrites one copy of one gene.");
    public static final String JEI_EDITOR_NOTE_2 = k("jei.cultivar.editor_note_2", "Only alleles in your Allele Atlas can be written.");
    public static final String JEI_BENCH_NOTE_1 = k("jei.cultivar.bench_note_1", "Certifies the packet and awards a ribbon by score:");
    public static final String JEI_BENCH_NOTE_2 = k("jei.cultivar.bench_note_2", "+%s for true-breeding lines. Records pay emeralds.");
    public static final String JEI_MARKET_NOTE = k("jei.cultivar.market_note", "Sells at dawn. Prices dip as one product floods the market.");
    public static final String JEI_SUPPLY_TIER = k("jei.cultivar.supply_tier", "Needs Contract Board tier %s");

    public static final String JADE_CONFIG = k("config.jade.plugin_cultivar.block_info", "Cultivar block info");
    public static final String JADE_STAGE = k("jade.cultivar.stage", "Stage %s of %s: %s");
    public static final String JADE_LIMITED = k("jade.cultivar.limited", "Not growing: %s");
    public static final String JADE_LIMIT_PREFIX = "jade.cultivar.limit.";
    public static final String JADE_LIMIT_COLD = k(JADE_LIMIT_PREFIX + "cold", "too cold");
    public static final String JADE_LIMIT_HEAT = k(JADE_LIMIT_PREFIX + "heat", "too hot");
    public static final String JADE_LIMIT_DROUGHT = k(JADE_LIMIT_PREFIX + "drought", "too dry");
    public static final String JADE_LIMIT_DARK = k(JADE_LIMIT_PREFIX + "dark", "too dark");
    public static final String JADE_LIMIT_PH = k(JADE_LIMIT_PREFIX + "ph", "soil pH is wrong for this crop");
    public static final String JADE_LIMIT_NUTRIENTS = k(JADE_LIMIT_PREFIX + "nutrients", "soil nutrients too low");
    public static final String JADE_LIMIT_DEPLETED = k(JADE_LIMIT_PREFIX + "depleted", "soil depleted");
    public static final String JADE_STRESS = k("jade.cultivar.stress", "Stress %s (lowers the grade)");
    public static final String JADE_LENS_HINT = k("jade.cultivar.lens_hint", "Hold a Hand Lens to read its traits");
    public static final String JADE_MOISTURE = k("jade.cultivar.moisture", "Moisture %s / 7");
    public static final String JADE_SOIL_LOW = k("jade.cultivar.soil_low", "Nutrients running low: check with a Soil Probe");
    public static final String JADE_SOIL_EXHAUSTED = k("jade.cultivar.soil_exhausted", "Exhausted: add amendments");
    public static final String JADE_CRATE = k("jade.cultivar.crate", "Crate: %s items, worth %s emeralds now");
    public static final String JADE_CRATE_EMPTY = k("jade.cultivar.crate_empty", "Crate empty. Sells at dawn.");
    public static final String JADE_ORDERS = k("jade.cultivar.orders", "%s open orders");
    public static final String JADE_VIAL = k("jade.cultivar.vial", "Vial: %s treatments left");
    public static final String JADE_COOLANT = k("jade.cultivar.coolant", "Coolant: %s");
    public static final String JADE_CHARGE = k("jade.cultivar.charge", "Charge %s / %s");
    public static final String JADE_LINES = k("jade.cultivar.lines", "%s / %s lines stored");
    public static final String JADE_UNIT_ACTIVE = k("jade.cultivar.unit_active", "Active, %s s of fuel");
    public static final String JADE_UNIT_IDLE = k("jade.cultivar.unit_idle", "Idle: needs fuel or a redstone signal");
    public static final String JADE_BURNS = k("jade.cultivar.burns", "%s burns left");
    public static final String JADE_EMPTY = k("jade.cultivar.empty", "Empty");
    public static final String JADE_LOADED = k("jade.cultivar.loaded", "Loaded: %s %s");
    public static final String JADE_SEALED = k("jade.cultivar.sealed", "Sealed, %s blocks. Temperature %s, target %s");
    public static final String JADE_EDITING = k("jade.cultivar.editing", "Editing %s");
    public static final String JADE_LAST_CERT = k("jade.cultivar.last_cert", "Last judged: %s, score %s");

    public static final Map<String, Guide> ITEM_GUIDE = new LinkedHashMap<>();
    public static final String TIP_SHIFT = k("tooltip.cultivar.shift", "Hold Shift for how to use");

    public record Guide(String summary, java.util.List<String> details) {
        public boolean hasSummary() {
            return !summary.isEmpty();
        }
    }

    private static void guide(String item, String summary, String... details) {
        String base = "tooltip.cultivar.guide." + item;
        String summaryKey = summary != null ? k(base, summary) : "";
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (int i = 0; i < details.length; i++) {
            keys.add(k(base + "." + (i + 1), details[i]));
        }
        ITEM_GUIDE.put(item, new Guide(summaryKey, java.util.List.copyOf(keys)));
    }

    static {
        guide("genome_card", null, "Made by a Genome Lab from a seed and paper.", "Use it to read genes, alleles and plan crosses.",
                "Reading a card records its alleles in your Atlas.");
        guide("soil_probe", null, "Use on soil: shows N, P, K, pH and moisture.", "Sneak-use on the ground: a rover surveys 16 blocks.");
        guide("hand_lens", null, "Hold it and look at a crop to see its traits.", "Shows stage, vigor, blight and grade ceiling.");
        guide("pollination_brush", null, "1. Use on a flowering plant to take its pollen.",
                "2. Use on another flowering plant of the same species.", "That plant's seeds become a cross of both.");
        guide("isolation_bag", null, "Use on a plant before it flowers.", "A bagged plant can only pollinate itself.",
                "Selfing for a few generations fixes a line.");
        guide("grafting_knife", null, "Clones a plant exactly, with no gene mixing.", "Use on a mature rootstock, then a neighbouring scion.");
        guide("field_manual", null, "Press J any time to open it.", "Chapters unlock as you discover things.");
        guide("field_notebook", null, "Keep it in your inventory while harvesting.", "Every cross is logged with parents and results.");
        guide("pedigree_sheet", null, "Hold a seed packet in your off hand, then use.", "Prints four generations of its ancestry.");
        guide("trial_tag", null, "Rename it in an anvil to set the plot label.", "Use on a Trial Marker; each use numbers the next plot.");
        guide("compost", null, "Adds 15 N, 15 P and 15 K.", "Restores Depleted Soil so crops grow again.",
                "Made in a Compost Bin from plant waste.");
        guide("lime_powder", null, "Raises soil pH by 0.5 (more alkaline).", "Load a Lime Hopper to dose soil automatically.");
        guide("sulfur_powder", null, "Lowers soil pH by 0.5 (more acidic).", "Load a Sulfur Hopper to dose soil automatically.");
        guide("nutrient_blend_n", null, "Adds 35 nitrogen (N).", "Use it when a Soil Probe reads low N.");
        guide("nutrient_blend_p", null, "Adds 35 phosphorus (P).", "Use it when a Soil Probe reads low P.");
        guide("nutrient_blend_k", null, "Adds 35 potassium (K).", "Use it when a Soil Probe reads low K.");
        guide("green_manure", null, "Adds 20 nitrogen and 20 potassium.", "Craft from 3 Clover Bundles and 1 Compost.");
        guide("mutagen_vial", null, "Load into a Mutagen Chamber with seed packets.", "Each seed may gain a new allele or be destroyed.",
                "The chamber screen shows the exact odds.");
        guide("wild_seed", null, "A wild Canopy plant's seed with a wild genome.", "Analyse it in a Genome Lab or cross it with crops.",
                "Wild alleles often carry rare traits.");
        guide("canopy_resin", "Rare resin from the Canopy", "Crafts the Canopy Key with gold.",
                "Found in Canopy, jungle temple and mansion chests.", "Also sold by a tier 2 Contract Board.");
        guide("canopy_key", null, "Ring 8 Canopy Portal Frames, then use the key on one.", "Opens a portal to the Canopy.");
        guide("blight_sample", null, "Made by burning infected plants in a Burn Pan.", "Analyse it in a Genome Lab for a strain report.",
                "The report lists the alleles that resist it.");
        guide("field_meal", "Milled wheatline, a market good", "Processing Station: 2 Wheatline Sheaf make 1.", "An ingredient of Edit Reagent.");
        guide("soy_oil", "Machine oil pressed from soybeans", "Use on any Cultivar machine: double speed for a while.",
                "Processing Station: 3 Soybean Pods make 1.");
        guide("linen_canvas", "Woven flax, a market good", "Processing Station: 3 Flax Fiber make 1.", "Wraps Ration Packs.");
        guide("starch", "Refined starch, a market good", "Processing Station: 2 Maize Cobs or 2 Tubers make 1.",
                "Process 2 Starch into Bioplastic.");
        guide("bioplastic", "Plant-made plastic, high value", "Processing Station: 2 Starch make 1.", "Used for Edit Reagent and the Gene Editor.");
        guide("pickled_brassica", "Cured brassica, very filling", "Processing Station: 2 Brassica Heads make 1.",
                "Keeps the grade of the brassica.");
        guide("gourd_puree", "Smooth gourd puree", "Processing Station: 1 Gourd makes 2.", "Keeps the grade of the gourd.");
        guide("biofuel_cake", "Pressed clover fuel", "Processing Station: 4 Clover Bundles make 1.", "Burns in furnaces and Pollination Units.");
        guide("ration_pack", null, "Craft Linen Canvas with 3 to 8 Cultivar foods.", "More species and higher grades make it stronger.",
                "Gives Regeneration when eaten.");
        guide("edit_reagent", "One edit's worth of Gene Editor reagent", "Craft: Mutagen Vial, Canopy Resin, Bioplastic, Field Meal.",
                "Also sold by a tier 3 Contract Board.");
        guide("gene_editor_blueprint", "Plans for building a Gene Editor", "Sold by a tier 4 Contract Board.",
                "Craft it with a Genome Lab, a Mutagen Chamber and Bioplastic.");
        guide("ribbon_bronze", "Show Bench award: score 55 or more", "Sells at the Produce Exchange.");
        guide("ribbon_silver", "Show Bench award: score 70 or more", "Sells at the Produce Exchange.");
        guide("ribbon_gold", "Show Bench award: score 85 or more", "Sells at the Produce Exchange.", "Earns an advancement.");
        guide("breeding_plot", "Soil that gives 2 extra seed packets per harvest", "More seeds per cross means more to select from.");
        guide("managed_soil", "Farmland that tracks N, P, K, pH and moisture", "Planting a seed packet on farmland makes it.",
                "Crops drain nutrients; top up with amendments.");
        guide("depleted_soil", "Exhausted soil where nothing grows", "Use Compost on it to restore it.");
        guide("compost_bin", "Turns plant waste into Compost", "Accepts produce, plant waste, seeds and leafmass.");
        guide("lime_hopper", "Slowly raises pH of soil within 2 blocks", "Fill it with Lime Powder. Aims for pH 7.2.");
        guide("sulfur_hopper", "Slowly lowers pH of soil within 2 blocks", "Fill it with Sulfur Powder. Aims for pH 5.6.");
        guide("nutrient_spreader", "Feeds the neediest soil within 4 blocks", "Use amendments on it to load it.");
        guide("isolation_screen", "Blocks pollination that crosses it", "Place it between plots to keep lines pure.");
        guide("pollination_unit", "Sends Pollinator Drones to plants within 6 blocks", "Runs on fuel or a redstone signal.",
                "Set an exact cross with a Pollination Brush.");
        guide("burn_pan", "Safely burns blighted plants", "Leaves the soil intact and stores a Blight Sample.");
        guide("harvest_collector", "Sends a Harvester Cart over mature crops", "The cart harvests, replants and unloads here.");
        guide("trial_marker", "Labels a trial plot with a floating name", "Use a Trial Tag on it to label and number it.");
        guide("genome_lab", "Reads seeds and samples onto Genome Cards", "Needs paper. The sample is given back.");
        guide("seed_bank", "Stores seed lines as genomes, not items", "Withdraw a copy of a line any time for paper.",
                "Seed Vault Drawers next to it add capacity.");
        guide("seed_vault_drawer", "Adds 8 lines to an adjacent Seed Bank");
        guide("mutagen_chamber", "Treats seed packets with a Mutagen Vial", "Seeds may gain a new allele, including rare ones.");
        guide("thermal_shock_unit", "Shocks seeds with ice to cause mutations", "25% chance of a new allele, 8% of losing the seed.");
        guide("produce_exchange", "Sells its crate for emeralds every dawn", "Higher grades pay more: Prime pays 4x.",
                "Prices dip as you flood one product, then recover.");
        guide("contract_board", "Daily orders paid in emeralds", "Fulfil orders to raise reputation and tier.",
                "The Supplies tab sells rare items.");
        guide("processing_station", "Turns produce into higher-value goods", "The output keeps the input's grade.",
                "Oil it with Soy Oil to double its speed.");
        guide("gene_editor", "Rewrites one allele on a seed packet", "Only alleles in your Allele Atlas can be written.",
                "Each edit uses one Edit Reagent.");
        guide("show_bench", "Judges a seed packet's genetics out of 100", "Awards a ribbon and a certificate.",
                "Beat a species record for bonus emeralds.");
        guide("greenhouse_pane", "Glass pane that seals a greenhouse");
        guide("grow_light", "Sets light to 14 inside a sealed greenhouse", "Lets crops grow at night and indoors.");
        guide("humidifier", "Raises humidity inside a sealed greenhouse");
        guide("dehumidifier", "Lowers humidity inside a sealed greenhouse");
        guide("climate_controller", "Sets the temperature of a sealed room", "Build a closed room of glass or panes around it.",
                "Its screen shows whether the room is sealed.");
        guide("irrigation_line", "Keeps soil within 4 blocks moist", "It must be fed with water.");
        guide("sprinkler", "Waters the soil around it");
        guide("canopy_bark", "Bark of the giant Canopy trees");
        guide("canopy_leafmass", "Dense Canopy leaves", "Good material for a Compost Bin.");
        guide("wild_vine_block", "Hanging vine from the Canopy");
        guide("canopy_portal_frame", "Part of a Canopy Portal", "Place 8 in a ring, then use a Canopy Key on one.");
        guide("seed_packet", null, "Plant on farmland: it becomes Managed Soil.", "Each packet carries a full genome.",
                "Read it in a Genome Lab, cross it with a brush.");
        guide("produce", null, "Grade C, B, A or Prime depends on genes and care.", "Sell it at a Produce Exchange or process it.",
                "Prime food also gives Regeneration.");
    }

    public static String subtitle(String sound) {
        return "subtitles.cultivar." + sound;
    }
}
