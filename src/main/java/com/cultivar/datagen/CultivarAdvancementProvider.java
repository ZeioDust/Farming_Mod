package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModTags;
import com.cultivar.world.dimension.CultivarDimensions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public class CultivarAdvancementProvider extends AdvancementProvider {
    public static final Map<String, String[]> TEXT = new LinkedHashMap<>();

    static {
        TEXT.put("root", new String[]{"Cultivar", "Collect a packet of seed. Every seed carries a genome."});
        TEXT.put("first_cross", new String[]{"First Cross", "Harvest seed from a plant you cross-pollinated"});
        TEXT.put("read_the_code", new String[]{"Read the Code", "Read a Genome Card printed by a Genome Lab"});
        TEXT.put("dominant", new String[]{"Dominant", "Read a genome in which a dominant allele hides a recessive one"});
        TEXT.put("true_line", new String[]{"True Line", "Breed a cross whose offspring is homozygous at every gene"});
        TEXT.put("rotation", new String[]{"Rotation", "Follow a nitrogen-fixing crop with a different kind, or the reverse, on the same soil"});
        TEXT.put("resistant", new String[]{"Resistant", "Harvest a plant with at least 75% blight resistance"});
        TEXT.put("mutant", new String[]{"Mutant", "Obtain a seed carrying a mutation-only allele"});
        TEXT.put("out_of_season", new String[]{"Out of Season", "Harvest a crop that could only grow inside your greenhouse"});
        TEXT.put("wild_blood", new String[]{"Wild Blood", "Breed a line that carries a wild allele yet yields at least as well as a standard crop"});
        TEXT.put("into_the_canopy", new String[]{"Into the Canopy", "Travel through a Canopy Portal"});
        TEXT.put("banked", new String[]{"Banked", "Store a line in a Seed Bank"});
        TEXT.put("prime_harvest", new String[]{"Prime Cut", "Harvest Prime-grade produce"});
        TEXT.put("first_sale", new String[]{"Open for Business", "Sell goods at a Produce Exchange"});
        TEXT.put("first_contract", new String[]{"Signed and Delivered", "Fulfil a contract from a Contract Board"});
        TEXT.put("ration", new String[]{"Field Rations", "Eat a Ration Pack packed from your own crops"});
        TEXT.put("gene_edit", new String[]{"Rewritten", "Edit an allele with a Gene Editor"});
        TEXT.put("show_record", new String[]{"Best in Show", "Set a species record at a Show Bench"});
        TEXT.put("gold_ribbon", new String[]{"Gold Standard", "Win a gold ribbon at a Show Bench"});
        TEXT.put("atlas_complete", new String[]{"Allele Atlas", "Record every allele in your Allele Atlas"});
    }

    public CultivarAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new Tree()));
    }

    static String titleKey(String id) {
        return "advancements." + Cultivar.MODID + "." + id + ".title";
    }

    static String descriptionKey(String id) {
        return "advancements." + Cultivar.MODID + "." + id + ".description";
    }

    static class Tree implements AdvancementSubProvider {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
            var items = registries.lookupOrThrow(Registries.ITEM);
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(ModItems.seedPacket(CropSpecies.WHEATLINE), Component.translatable(titleKey("root")),
                            Component.translatable(descriptionKey("root")),
                            Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry"), AdvancementType.TASK, false, false, false)
                    .addCriterion("has_seed", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, ModTags.Items.SEED_PACKETS)))
                    .save(output, Cultivar.MODID + ":root");
            AdvancementHolder firstCross = action(output, root, "first_cross", ModItems.POLLINATION_BRUSH::get, AdvancementType.TASK, CultivarTrigger.FIRST_CROSS);
            AdvancementHolder read = action(output, root, "read_the_code", ModItems.GENOME_CARD::get, AdvancementType.TASK, CultivarTrigger.READ_THE_CODE);
            action(output, read, "dominant", ModItems.GENOME_CARD::get, AdvancementType.TASK, CultivarTrigger.DOMINANT);
            AdvancementHolder trueLine = action(output, firstCross, "true_line", () -> ModItems.seedPacket(CropSpecies.MAIZE), AdvancementType.GOAL,
                    CultivarTrigger.TRUE_LINE);
            action(output, root, "rotation", () -> ModItems.produce(CropSpecies.CLOVER), AdvancementType.TASK, CultivarTrigger.ROTATION);
            action(output, firstCross, "resistant", ModItems.ISOLATION_SCREEN::get, AdvancementType.GOAL, CultivarTrigger.RESISTANT);
            action(output, read, "mutant", ModItems.MUTAGEN_VIAL::get, AdvancementType.GOAL, CultivarTrigger.MUTANT);
            action(output, root, "out_of_season", ModItems.CLIMATE_CONTROLLER::get, AdvancementType.GOAL, CultivarTrigger.OUT_OF_SEASON);
            AdvancementHolder canopy = Advancement.Builder.advancement().parent(root)
                    .display(ModItems.CANOPY_KEY.get(), Component.translatable(titleKey("into_the_canopy")),
                            Component.translatable(descriptionKey("into_the_canopy")), null, AdvancementType.GOAL, true, true, false)
                    .addCriterion("entered", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(CultivarDimensions.CANOPY_LEVEL))
                    .save(output, Cultivar.MODID + ":into_the_canopy");
            action(output, canopy, "wild_blood", ModItems.WILD_SEED::get, AdvancementType.CHALLENGE, CultivarTrigger.WILD_BLOOD);
            action(output, trueLine, "banked", ModItems.SEED_BANK::get, AdvancementType.TASK, CultivarTrigger.BANKED);
            AdvancementHolder prime = action(output, root, "prime_harvest", () -> ModItems.produce(CropSpecies.GOURD), AdvancementType.TASK,
                    CultivarTrigger.PRIME_HARVEST);
            AdvancementHolder sale = action(output, prime, "first_sale", ModItems.PRODUCE_EXCHANGE::get, AdvancementType.TASK, CultivarTrigger.FIRST_SALE);
            action(output, sale, "first_contract", ModItems.CONTRACT_BOARD::get, AdvancementType.TASK, CultivarTrigger.FIRST_CONTRACT);
            action(output, prime, "ration", ModItems.RATION_PACK::get, AdvancementType.GOAL, CultivarTrigger.RATION);
            AdvancementHolder edit = action(output, read, "gene_edit", ModItems.GENE_EDITOR::get, AdvancementType.GOAL, CultivarTrigger.GENE_EDIT);
            action(output, edit, "atlas_complete", ModItems.GENOME_CARD::get, AdvancementType.CHALLENGE, CultivarTrigger.ATLAS_COMPLETE);
            AdvancementHolder gold = action(output, trueLine, "gold_ribbon", ModItems.RIBBON_GOLD::get, AdvancementType.GOAL, CultivarTrigger.GOLD_RIBBON);
            action(output, gold, "show_record", ModItems.SHOW_BENCH::get, AdvancementType.CHALLENGE, CultivarTrigger.SHOW_RECORD);
        }

        private static AdvancementHolder action(Consumer<AdvancementHolder> output, AdvancementHolder parent, String id, Supplier<? extends ItemLike> icon,
                                                AdvancementType type, String action) {
            return Advancement.Builder.advancement().parent(parent)
                    .display(icon.get(), Component.translatable(titleKey(id)), Component.translatable(descriptionKey(id)), null, type, true,
                            type != AdvancementType.TASK, false)
                    .addCriterion(action, CultivarTrigger.criterion(action))
                    .save(output, Cultivar.MODID + ":" + id);
        }
    }
}
