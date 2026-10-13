package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.data.CropSpecies;
import com.cultivar.item.AmendmentItem;
import com.cultivar.item.BlightSampleItem;
import com.cultivar.item.CanopyKeyItem;
import com.cultivar.item.DescribedItem;
import com.cultivar.item.FieldManualItem;
import com.cultivar.item.FieldNotebookItem;
import com.cultivar.item.GenomeCardItem;
import com.cultivar.item.GraftingKnifeItem;
import com.cultivar.item.HandLensItem;
import com.cultivar.item.IsolationBagItem;
import com.cultivar.item.PedigreeSheetItem;
import com.cultivar.item.PollinationBrushItem;
import com.cultivar.item.ProduceItem;
import com.cultivar.item.RationPackItem;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.item.SoilProbeItem;
import com.cultivar.item.SoyOilItem;
import com.cultivar.item.TrialTagItem;
import com.cultivar.item.WildSeedItem;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cultivar.MODID);

    public static final Map<CropSpecies, DeferredItem<SeedPacketItem>> SEED_PACKETS = new EnumMap<>(CropSpecies.class);
    public static final Map<CropSpecies, DeferredItem<ProduceItem>> PRODUCE = new EnumMap<>(CropSpecies.class);

    static {
        for (CropSpecies species : CropSpecies.VALUES) {
            SEED_PACKETS.put(species, ITEMS.registerItem(species.seedPacketName(),
                    p -> new SeedPacketItem(species, ModBlocks.crop(species), p.useItemDescriptionPrefix())));
            PRODUCE.put(species, ITEMS.registerItem(species.produceName(), p -> {
                if (species.foodNutrition() > 0) {
                    p.food(new FoodProperties.Builder().nutrition(species.foodNutrition()).saturationModifier(0.6f).build());
                }
                return new ProduceItem(species, p);
            }));
        }
    }

    public static final DeferredItem<GenomeCardItem> GENOME_CARD = ITEMS.registerItem("genome_card", GenomeCardItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<SoilProbeItem> SOIL_PROBE = ITEMS.registerItem("soil_probe", SoilProbeItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<HandLensItem> HAND_LENS = ITEMS.registerItem("hand_lens", HandLensItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<PollinationBrushItem> POLLINATION_BRUSH = ITEMS.registerItem("pollination_brush", PollinationBrushItem::new,
            p -> p.stacksTo(1));
    public static final DeferredItem<IsolationBagItem> ISOLATION_BAG = ITEMS.registerItem("isolation_bag", IsolationBagItem::new);
    public static final DeferredItem<GraftingKnifeItem> GRAFTING_KNIFE = ITEMS.registerItem("grafting_knife", GraftingKnifeItem::new,
            p -> p.stacksTo(1).durability(128));
    public static final DeferredItem<FieldManualItem> FIELD_MANUAL = ITEMS.registerItem("field_manual", FieldManualItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<FieldNotebookItem> FIELD_NOTEBOOK = ITEMS.registerItem("field_notebook", FieldNotebookItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<PedigreeSheetItem> PEDIGREE_SHEET = ITEMS.registerItem("pedigree_sheet", PedigreeSheetItem::new, p -> p.stacksTo(16));
    public static final DeferredItem<TrialTagItem> TRIAL_TAG = ITEMS.registerItem("trial_tag", TrialTagItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<AmendmentItem> COMPOST = ITEMS.registerItem("compost", AmendmentItem::new);
    public static final DeferredItem<AmendmentItem> LIME_POWDER = ITEMS.registerItem("lime_powder", AmendmentItem::new);
    public static final DeferredItem<AmendmentItem> SULFUR_POWDER = ITEMS.registerItem("sulfur_powder", AmendmentItem::new);
    public static final DeferredItem<AmendmentItem> NUTRIENT_BLEND_N = ITEMS.registerItem("nutrient_blend_n", AmendmentItem::new);
    public static final DeferredItem<AmendmentItem> NUTRIENT_BLEND_P = ITEMS.registerItem("nutrient_blend_p", AmendmentItem::new);
    public static final DeferredItem<AmendmentItem> NUTRIENT_BLEND_K = ITEMS.registerItem("nutrient_blend_k", AmendmentItem::new);

    public static final DeferredItem<DescribedItem> MUTAGEN_VIAL = ITEMS.registerItem("mutagen_vial",
            p -> new DescribedItem(Texts.TIP_MUTAGEN, p), p -> p.stacksTo(16));
    public static final DeferredItem<WildSeedItem> WILD_SEED = ITEMS.registerItem("wild_seed", WildSeedItem::new);
    public static final DeferredItem<Item> CANOPY_RESIN = ITEMS.registerSimpleItem("canopy_resin", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CanopyKeyItem> CANOPY_KEY = ITEMS.registerItem("canopy_key", CanopyKeyItem::new,
            p -> p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<BlightSampleItem> BLIGHT_SAMPLE = ITEMS.registerItem("blight_sample", BlightSampleItem::new, p -> p.stacksTo(16));

    public static final DeferredItem<AmendmentItem> GREEN_MANURE = ITEMS.registerItem("green_manure", AmendmentItem::new);
    public static final DeferredItem<Item> FIELD_MEAL = ITEMS.registerSimpleItem("field_meal");
    public static final DeferredItem<SoyOilItem> SOY_OIL = ITEMS.registerItem("soy_oil", SoyOilItem::new, p -> p.stacksTo(16));
    public static final DeferredItem<Item> LINEN_CANVAS = ITEMS.registerSimpleItem("linen_canvas");
    public static final DeferredItem<Item> STARCH = ITEMS.registerSimpleItem("starch");
    public static final DeferredItem<Item> BIOPLASTIC = ITEMS.registerSimpleItem("bioplastic");
    public static final DeferredItem<Item> PICKLED_BRASSICA = ITEMS.registerSimpleItem("pickled_brassica",
            p -> p.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.9f).build()));
    public static final DeferredItem<Item> GOURD_PUREE = ITEMS.registerSimpleItem("gourd_puree",
            p -> p.food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.7f).build()));
    public static final DeferredItem<Item> BIOFUEL_CAKE = ITEMS.registerSimpleItem("biofuel_cake");
    public static final DeferredItem<RationPackItem> RATION_PACK = ITEMS.registerItem("ration_pack", RationPackItem::new,
            p -> p.stacksTo(16).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).build()));
    public static final DeferredItem<Item> EDIT_REAGENT = ITEMS.registerSimpleItem("edit_reagent", p -> p.rarity(Rarity.RARE).stacksTo(16));
    public static final DeferredItem<Item> GENE_EDITOR_BLUEPRINT = ITEMS.registerSimpleItem("gene_editor_blueprint",
            p -> p.rarity(Rarity.EPIC).stacksTo(1));
    public static final DeferredItem<Item> RIBBON_BRONZE = ITEMS.registerSimpleItem("ribbon_bronze", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RIBBON_SILVER = ITEMS.registerSimpleItem("ribbon_silver", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> RIBBON_GOLD = ITEMS.registerSimpleItem("ribbon_gold", p -> p.rarity(Rarity.EPIC));

    public static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = new ArrayList<>();
    public static final DeferredItem<BlockItem> BREEDING_PLOT = block(ModBlocks.BREEDING_PLOT);
    public static final DeferredItem<BlockItem> MANAGED_SOIL = block(ModBlocks.MANAGED_SOIL);
    public static final DeferredItem<BlockItem> DEPLETED_SOIL = block(ModBlocks.DEPLETED_SOIL);
    public static final DeferredItem<BlockItem> COMPOST_BIN = block(ModBlocks.COMPOST_BIN);
    public static final DeferredItem<BlockItem> LIME_HOPPER = block(ModBlocks.LIME_HOPPER);
    public static final DeferredItem<BlockItem> SULFUR_HOPPER = block(ModBlocks.SULFUR_HOPPER);
    public static final DeferredItem<BlockItem> NUTRIENT_SPREADER = block(ModBlocks.NUTRIENT_SPREADER);
    public static final DeferredItem<BlockItem> ISOLATION_SCREEN = block(ModBlocks.ISOLATION_SCREEN);
    public static final DeferredItem<BlockItem> POLLINATION_UNIT = block(ModBlocks.POLLINATION_UNIT);
    public static final DeferredItem<BlockItem> BURN_PAN = block(ModBlocks.BURN_PAN);
    public static final DeferredItem<BlockItem> HARVEST_COLLECTOR = block(ModBlocks.HARVEST_COLLECTOR);
    public static final DeferredItem<BlockItem> TRIAL_MARKER = block(ModBlocks.TRIAL_MARKER);
    public static final DeferredItem<BlockItem> GENOME_LAB = block(ModBlocks.GENOME_LAB);
    public static final DeferredItem<BlockItem> SEED_BANK = block(ModBlocks.SEED_BANK);
    public static final DeferredItem<BlockItem> SEED_VAULT_DRAWER = block(ModBlocks.SEED_VAULT_DRAWER);
    public static final DeferredItem<BlockItem> MUTAGEN_CHAMBER = block(ModBlocks.MUTAGEN_CHAMBER);
    public static final DeferredItem<BlockItem> THERMAL_SHOCK_UNIT = block(ModBlocks.THERMAL_SHOCK_UNIT);
    public static final DeferredItem<BlockItem> GREENHOUSE_PANE = block(ModBlocks.GREENHOUSE_PANE);
    public static final DeferredItem<BlockItem> GROW_LIGHT = block(ModBlocks.GROW_LIGHT);
    public static final DeferredItem<BlockItem> HUMIDIFIER = block(ModBlocks.HUMIDIFIER);
    public static final DeferredItem<BlockItem> DEHUMIDIFIER = block(ModBlocks.DEHUMIDIFIER);
    public static final DeferredItem<BlockItem> CLIMATE_CONTROLLER = block(ModBlocks.CLIMATE_CONTROLLER);
    public static final DeferredItem<BlockItem> IRRIGATION_LINE = block(ModBlocks.IRRIGATION_LINE);
    public static final DeferredItem<BlockItem> SPRINKLER = block(ModBlocks.SPRINKLER);
    public static final DeferredItem<BlockItem> CANOPY_BARK = block(ModBlocks.CANOPY_BARK);
    public static final DeferredItem<BlockItem> CANOPY_LEAFMASS = block(ModBlocks.CANOPY_LEAFMASS);
    public static final DeferredItem<BlockItem> WILD_VINE_BLOCK = block(ModBlocks.WILD_VINE_BLOCK);
    public static final DeferredItem<BlockItem> CANOPY_PORTAL_FRAME = block(ModBlocks.CANOPY_PORTAL_FRAME);
    public static final DeferredItem<BlockItem> PRODUCE_EXCHANGE = block(ModBlocks.PRODUCE_EXCHANGE);
    public static final DeferredItem<BlockItem> CONTRACT_BOARD = block(ModBlocks.CONTRACT_BOARD);
    public static final DeferredItem<BlockItem> PROCESSING_STATION = block(ModBlocks.PROCESSING_STATION);
    public static final DeferredItem<BlockItem> GENE_EDITOR = block(ModBlocks.GENE_EDITOR);
    public static final DeferredItem<BlockItem> SHOW_BENCH = block(ModBlocks.SHOW_BENCH);

    private ModItems() {
    }

    private static DeferredItem<BlockItem> block(DeferredBlock<? extends Block> block) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block);
        BLOCK_ITEMS.add(item);
        return item;
    }

    public static SeedPacketItem seedPacket(CropSpecies species) {
        return SEED_PACKETS.get(species).get();
    }

    public static ProduceItem produce(CropSpecies species) {
        return PRODUCE.get(species).get();
    }
}
