package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.DefaultData;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.DefaultGenetics;
import com.cultivar.genetics.Gene;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import com.cultivar.world.dimension.CultivarDimensions;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class CultivarLanguageProvider extends LanguageProvider {
    public CultivarLanguageProvider(PackOutput output) {
        super(output, Cultivar.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        Texts.ALL.forEach(this::add);

        for (CropSpecies species : CropSpecies.VALUES) {
            addBlock(ModBlocks.CROPS.get(species), species.displayName());
            addItem(ModItems.SEED_PACKETS.get(species), species.displayName() + " Seed Packet");
            addItem(ModItems.PRODUCE.get(species), species.produceDisplayName());
            add("species." + Cultivar.MODID + "." + species.id(), species.displayName());
        }
        addBlock(ModBlocks.GRAFT_UNION, "Graft Union");

        addBlock(ModBlocks.BREEDING_PLOT, "Breeding Plot");
        addBlock(ModBlocks.MANAGED_SOIL, "Managed Soil");
        addBlock(ModBlocks.DEPLETED_SOIL, "Depleted Soil");
        addBlock(ModBlocks.COMPOST_BIN, "Compost Bin");
        addBlock(ModBlocks.LIME_HOPPER, "Lime Hopper");
        addBlock(ModBlocks.SULFUR_HOPPER, "Sulfur Hopper");
        addBlock(ModBlocks.NUTRIENT_SPREADER, "Nutrient Spreader");
        addBlock(ModBlocks.ISOLATION_SCREEN, "Isolation Screen");
        addBlock(ModBlocks.POLLINATION_UNIT, "Pollination Unit");
        addBlock(ModBlocks.BURN_PAN, "Burn Pan");
        addBlock(ModBlocks.HARVEST_COLLECTOR, "Harvest Collector");
        addBlock(ModBlocks.TRIAL_MARKER, "Trial Marker");
        addBlock(ModBlocks.GENOME_LAB, "Genome Lab");
        addBlock(ModBlocks.SEED_BANK, "Seed Bank");
        addBlock(ModBlocks.SEED_VAULT_DRAWER, "Seed Vault Drawer");
        addBlock(ModBlocks.MUTAGEN_CHAMBER, "Mutagen Chamber");
        addBlock(ModBlocks.THERMAL_SHOCK_UNIT, "Thermal Shock Unit");
        addBlock(ModBlocks.GREENHOUSE_PANE, "Greenhouse Pane");
        addBlock(ModBlocks.GROW_LIGHT, "Grow Light");
        addBlock(ModBlocks.HUMIDIFIER, "Humidifier");
        addBlock(ModBlocks.DEHUMIDIFIER, "Dehumidifier");
        addBlock(ModBlocks.CLIMATE_CONTROLLER, "Climate Controller");
        addBlock(ModBlocks.IRRIGATION_LINE, "Irrigation Line");
        addBlock(ModBlocks.SPRINKLER, "Sprinkler");
        addBlock(ModBlocks.CANOPY_BARK, "Canopy Bark");
        addBlock(ModBlocks.CANOPY_LEAFMASS, "Canopy Leafmass");
        addBlock(ModBlocks.WILD_VINE_BLOCK, "Wild Vine");
        addBlock(ModBlocks.CANOPY_PORTAL_FRAME, "Canopy Portal Frame");
        addBlock(ModBlocks.CANOPY_PORTAL, "Canopy Portal");
        addBlock(ModBlocks.PRODUCE_EXCHANGE, "Produce Exchange");
        addBlock(ModBlocks.CONTRACT_BOARD, "Contract Board");
        addBlock(ModBlocks.PROCESSING_STATION, "Processing Station");
        addBlock(ModBlocks.GENE_EDITOR, "Gene Editor");
        addBlock(ModBlocks.SHOW_BENCH, "Show Bench");

        addItem(ModItems.GENOME_CARD, "Genome Card");
        addItem(ModItems.SOIL_PROBE, "Soil Probe");
        addItem(ModItems.HAND_LENS, "Hand Lens");
        addItem(ModItems.POLLINATION_BRUSH, "Pollination Brush");
        addItem(ModItems.ISOLATION_BAG, "Isolation Bag");
        addItem(ModItems.COMPOST, "Compost");
        addItem(ModItems.LIME_POWDER, "Lime Powder");
        addItem(ModItems.SULFUR_POWDER, "Sulfur Powder");
        addItem(ModItems.NUTRIENT_BLEND_N, "Nitrogen Nutrient Blend");
        addItem(ModItems.NUTRIENT_BLEND_P, "Phosphorus Nutrient Blend");
        addItem(ModItems.NUTRIENT_BLEND_K, "Potassium Nutrient Blend");
        addItem(ModItems.MUTAGEN_VIAL, "Mutagen Vial");
        addItem(ModItems.GRAFTING_KNIFE, "Grafting Knife");
        addItem(ModItems.FIELD_MANUAL, "Breeder's Field Manual");
        addItem(ModItems.FIELD_NOTEBOOK, "Field Notebook");
        addItem(ModItems.PEDIGREE_SHEET, "Pedigree Sheet");
        addItem(ModItems.TRIAL_TAG, "Trial Tag");
        addItem(ModItems.WILD_SEED, "Wild Seed");
        addItem(ModItems.CANOPY_RESIN, "Canopy Resin");
        addItem(ModItems.CANOPY_KEY, "Canopy Key");
        addItem(ModItems.BLIGHT_SAMPLE, "Blight Sample");
        addItem(ModItems.GREEN_MANURE, "Green Manure");
        addItem(ModItems.FIELD_MEAL, "Field Meal");
        addItem(ModItems.SOY_OIL, "Soy Oil");
        addItem(ModItems.LINEN_CANVAS, "Linen Canvas");
        addItem(ModItems.STARCH, "Starch");
        addItem(ModItems.BIOPLASTIC, "Bioplastic");
        addItem(ModItems.PICKLED_BRASSICA, "Pickled Brassica");
        addItem(ModItems.GOURD_PUREE, "Gourd Puree");
        addItem(ModItems.BIOFUEL_CAKE, "Biofuel Cake");
        addItem(ModItems.RATION_PACK, "Ration Pack");
        addItem(ModItems.EDIT_REAGENT, "Edit Reagent");
        addItem(ModItems.GENE_EDITOR_BLUEPRINT, "Gene Editor Blueprint");
        addItem(ModItems.RIBBON_BRONZE, "Bronze Show Ribbon");
        addItem(ModItems.RIBBON_SILVER, "Silver Show Ribbon");
        addItem(ModItems.RIBBON_GOLD, "Gold Show Ribbon");

        addEntityType(ModEntities.POLLINATOR_DRONE, "Pollinator Drone");
        addEntityType(ModEntities.HARVESTER_CART, "Harvester Cart");
        addEntityType(ModEntities.SPORE_DRIFT, "Spore Drift");
        addEntityType(ModEntities.WATER_JET, "Water Jet");
        addEntityType(ModEntities.SOIL_PROBE_ROVER, "Soil Probe Rover");
        addEntityType(ModEntities.FALLING_PRODUCE, "Falling Produce");
        addEntityType(ModEntities.MIST_CLOUD, "Mist Cloud");

        for (Gene gene : Gene.VALUES) {
            add(gene.translationKey(), title(gene.id()));
        }
        for (Allele allele : DefaultGenetics.alleles()) {
            add(allele.translationKey(), alleleName(allele));
        }
        for (Identifier strain : DefaultData.blightStrains().keySet()) {
            add("strain." + strain.getNamespace() + "." + strain.getPath(), title(strain.getPath()));
        }

        CultivarAdvancementProvider.TEXT.forEach((id, text) -> {
            add(CultivarAdvancementProvider.titleKey(id), text[0]);
            add(CultivarAdvancementProvider.descriptionKey(id), text[1]);
        });

        ModSounds.SUBTITLES.forEach((name, text) -> add(Texts.subtitle(name), text));

        addBiome(CultivarDimensions.CANOPY_GROVE, "Canopy Grove");
        add("dimension." + Cultivar.MODID + ".canopy", "The Canopy");

        String prefix = Cultivar.MODID + ".configuration.";
        add(prefix + "title", "Cultivar Configuration");
        add(prefix + "section.cultivar.server.toml", "Cultivar Settings");
        add(prefix + "section.cultivar.server.toml.title", "Cultivar Settings");
        for (String section : CultivarConfig.SECTIONS) {
            add(prefix + section, title(section));
            add(prefix + section + ".tooltip", CultivarConfig.COMMENTS.get(section));
            add(prefix + section + ".button", title(section) + "...");
        }
        for (String key : CultivarConfig.KEYS) {
            add(prefix + key, spaced(key));
            add(prefix + key + ".tooltip", CultivarConfig.COMMENTS.get(key));
        }
    }

    private static String alleleName(Allele allele) {
        String id = allele.id();
        int underscore = id.indexOf('_');
        return title(underscore >= 0 ? id.substring(underscore + 1) : id);
    }

    private static String title(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static String spaced(String camel) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (i == 0) {
                out.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                out.append(' ').append(c);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
