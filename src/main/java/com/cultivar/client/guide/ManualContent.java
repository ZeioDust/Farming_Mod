package com.cultivar.client.guide;

import com.cultivar.client.ui.Icons.Icon;
import com.cultivar.client.ui.Theme;
import com.cultivar.data.CropSpecies;
import com.cultivar.guide.Hint;
import com.cultivar.guide.Milestones;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class ManualContent {
    public sealed interface Element permits Para, Diagram, Recipe, Showcase, Tip, Flow {
    }

    public record Para(String key) implements Element {
    }

    public record Diagram(Diagrams.Kind kind, int height) implements Element {
    }

    public record Recipe(Supplier<? extends ItemLike> item) implements Element {
    }

    public record Showcase(List<Supplier<? extends ItemLike>> items) implements Element {
    }

    public record Tip(String key) implements Element {
    }

    public record Flow(List<Supplier<ItemStack>> stacks, List<String> labels) implements Element {
    }

    public record Page(String titleKey, List<Element> elements) {
    }

    public record Goal(String milestone, String labelKey) {
    }

    public record Chapter(Hint.Chapter id, String titleKey, Icon icon, int accent, List<Goal> goals, List<Page> pages) {
    }

    private static List<Chapter> chapters;

    private ManualContent() {
    }

    private static Supplier<ItemStack> stack(Supplier<? extends ItemLike> item) {
        return () -> new ItemStack(item.get());
    }

    public static List<Chapter> chapters() {
        if (chapters == null) {
            chapters = build();
        }
        return chapters;
    }

    public static Chapter chapter(Hint.Chapter id) {
        for (Chapter chapter : chapters()) {
            if (chapter.id() == id) {
                return chapter;
            }
        }
        return chapters().getFirst();
    }

    private static List<Chapter> build() {
        Supplier<Item> wheatPacket = () -> ModItems.seedPacket(CropSpecies.WHEATLINE);
        Supplier<Item> maizePacket = () -> ModItems.seedPacket(CropSpecies.MAIZE);
        return List.of(
                new Chapter(Hint.Chapter.START, Texts.CH_START, Icon.SEED, Theme.ACCENT_LAB,
                        List.of(new Goal(Milestones.PLANTED, Texts.GOAL_PLANTED), new Goal(Milestones.HARVESTED, Texts.GOAL_HARVESTED)),
                        List.of(
                                new Page(Texts.P_START_1_T, List.of(new Para(Texts.P_START_1_A), new Diagram(Diagrams.Kind.HELIX, 50),
                                        new Para(Texts.P_START_1_B))),
                                new Page(Texts.P_START_2_T, List.of(new Para(Texts.P_START_2_A), new Recipe(wheatPacket),
                                        new Para(Texts.P_START_2_B))),
                                new Page(Texts.P_START_3_T, List.of(
                                        new Showcase(List.of(ModItems.HAND_LENS, ModItems.SOIL_PROBE, ModItems.POLLINATION_BRUSH,
                                                ModItems.ISOLATION_BAG, ModItems.GENOME_CARD, ModItems.FIELD_MANUAL)),
                                        new Para(Texts.P_START_3_A), new Tip(Texts.P_START_3_TIP))))),
                new Chapter(Hint.Chapter.GENES, Texts.CH_GENES, Icon.HELIX, Theme.ACCENT_CARD,
                        List.of(new Goal(Milestones.READ_THE_CODE, Texts.GOAL_READ), new Goal(Milestones.DOMINANT, Texts.GOAL_DOMINANT)),
                        List.of(
                                new Page(Texts.P_GENES_1_T, List.of(new Diagram(Diagrams.Kind.GENES, 92), new Para(Texts.P_GENES_1_A))),
                                new Page(Texts.P_GENES_2_T, List.of(new Diagram(Diagrams.Kind.DOMINANCE, 62), new Para(Texts.P_GENES_2_A))),
                                new Page(Texts.P_GENES_3_T, List.of(new Para(Texts.P_GENES_3_A),
                                        new Showcase(List.of(ModItems.WILD_SEED, ModItems.MUTAGEN_VIAL, ModItems.CANOPY_KEY)),
                                        new Tip(Texts.P_GENES_3_TIP))))),
                new Chapter(Hint.Chapter.POLLINATION, Texts.CH_POLLINATION, Icon.WIND, Theme.GOOD,
                        List.of(new Goal(Milestones.FLOWERING, Texts.GOAL_FLOWERING), new Goal(Milestones.FIRST_CROSS, Texts.GOAL_FIRST_CROSS),
                                new Goal(Milestones.TRUE_LINE, Texts.GOAL_TRUE_LINE)),
                        List.of(
                                new Page(Texts.P_POLL_1_T, List.of(new Diagram(Diagrams.Kind.WIND, 58), new Para(Texts.P_POLL_1_A),
                                        new Para(Texts.P_POLL_1_B))),
                                new Page(Texts.P_POLL_2_T, List.of(
                                        new Flow(List.of(stack(ModItems.POLLINATION_BRUSH), stack(maizePacket), stack(maizePacket),
                                                stack(maizePacket)), List.of(Texts.DG_COLLECT, Texts.DG_APPLY, Texts.DG_HARVEST)),
                                        new Para(Texts.P_POLL_2_A), new Recipe(ModItems.POLLINATION_BRUSH))),
                                new Page(Texts.P_POLL_3_T, List.of(new Diagram(Diagrams.Kind.PUNNETT, 82), new Para(Texts.P_POLL_3_A))))),
                new Chapter(Hint.Chapter.SOIL, Texts.CH_SOIL, Icon.GRID, Theme.ACCENT_COMPOST,
                        List.of(new Goal(Milestones.ROTATION, Texts.GOAL_ROTATION)),
                        List.of(
                                new Page(Texts.P_SOIL_1_T, List.of(new Diagram(Diagrams.Kind.PH, 70), new Para(Texts.P_SOIL_1_A))),
                                new Page(Texts.P_SOIL_2_T, List.of(new Diagram(Diagrams.Kind.NPK, 48), new Para(Texts.P_SOIL_2_A),
                                        new Showcase(List.of(ModItems.COMPOST, ModItems.NUTRIENT_BLEND_N, ModItems.NUTRIENT_BLEND_P,
                                                ModItems.NUTRIENT_BLEND_K, ModItems.LIME_POWDER, ModItems.SULFUR_POWDER)))),
                                new Page(Texts.P_SOIL_3_T, List.of(new Para(Texts.P_SOIL_3_A), new Recipe(ModItems.COMPOST_BIN),
                                        new Showcase(List.of(ModItems.LIME_HOPPER, ModItems.SULFUR_HOPPER, ModItems.NUTRIENT_SPREADER,
                                                ModItems.IRRIGATION_LINE, ModItems.SPRINKLER)))))),
                new Chapter(Hint.Chapter.BLIGHT, Texts.CH_BLIGHT, Icon.SPORE, Theme.ACCENT_BLIGHT,
                        List.of(new Goal(Milestones.BLIGHT, Texts.GOAL_BLIGHT), new Goal(Milestones.RESISTANT, Texts.GOAL_RESISTANT)),
                        List.of(
                                new Page(Texts.P_BLIGHT_1_T, List.of(new Diagram(Diagrams.Kind.BLIGHT, 72), new Para(Texts.P_BLIGHT_1_A))),
                                new Page(Texts.P_BLIGHT_2_T, List.of(new Para(Texts.P_BLIGHT_2_A),
                                        new Showcase(List.of(ModItems.ISOLATION_SCREEN, ModItems.BURN_PAN, ModItems.BLIGHT_SAMPLE)),
                                        new Tip(Texts.P_BLIGHT_2_TIP))))),
                new Chapter(Hint.Chapter.LAB, Texts.CH_LAB, Icon.FLASK, Theme.ACCENT_LAB,
                        List.of(new Goal(Milestones.READ_THE_CODE, Texts.GOAL_READ), new Goal(Milestones.BANKED, Texts.GOAL_BANKED)),
                        List.of(
                                new Page(Texts.P_LAB_1_T, List.of(
                                        new Flow(List.of(stack(maizePacket), () -> new ItemStack(Items.PAPER), stack(ModItems.GENOME_LAB),
                                                stack(ModItems.GENOME_CARD)), List.of(Texts.DG_AND, Texts.DG_INSERT, Texts.DG_PRINTS)),
                                        new Para(Texts.P_LAB_1_A), new Recipe(ModItems.GENOME_LAB))),
                                new Page(Texts.P_LAB_2_T, List.of(new Diagram(Diagrams.Kind.CARD, 40), new Para(Texts.P_LAB_2_A),
                                        new Tip(Texts.P_LAB_2_TIP))),
                                new Page(Texts.P_LAB_3_T, List.of(new Para(Texts.P_LAB_3_A),
                                        new Showcase(List.of(ModItems.SEED_BANK, ModItems.SEED_VAULT_DRAWER, ModItems.FIELD_NOTEBOOK,
                                                ModItems.PEDIGREE_SHEET, ModItems.TRIAL_TAG, ModItems.TRIAL_MARKER)),
                                        new Recipe(ModItems.SEED_BANK))))),
                new Chapter(Hint.Chapter.GREENHOUSE, Texts.CH_GREENHOUSE, Icon.THERMO, Theme.ACCENT_CLIMATE,
                        List.of(new Goal(Milestones.OUT_OF_SEASON, Texts.GOAL_OUT_OF_SEASON)),
                        List.of(
                                new Page(Texts.P_GREEN_1_T, List.of(new Diagram(Diagrams.Kind.GREENHOUSE, 66), new Para(Texts.P_GREEN_1_A))),
                                new Page(Texts.P_GREEN_2_T, List.of(new Para(Texts.P_GREEN_2_A),
                                        new Showcase(List.of(ModItems.CLIMATE_CONTROLLER, ModItems.GREENHOUSE_PANE, ModItems.GROW_LIGHT,
                                                ModItems.HUMIDIFIER, ModItems.DEHUMIDIFIER)),
                                        new Recipe(ModItems.CLIMATE_CONTROLLER))))),
                new Chapter(Hint.Chapter.MUTATION, Texts.CH_MUTATION, Icon.SPLIT, Theme.ACCENT_MUTAGEN,
                        List.of(new Goal(Milestones.MUTANT, Texts.GOAL_MUTANT), new Goal(Milestones.GRAFTED, Texts.GOAL_GRAFTED)),
                        List.of(
                                new Page(Texts.P_MUT_1_T, List.of(new Diagram(Diagrams.Kind.MUTATION, 44), new Para(Texts.P_MUT_1_A),
                                        new Recipe(ModItems.MUTAGEN_CHAMBER))),
                                new Page(Texts.P_MUT_2_T, List.of(new Diagram(Diagrams.Kind.GRAFT, 62), new Para(Texts.P_MUT_2_A))))),
                new Chapter(Hint.Chapter.CANOPY, Texts.CH_CANOPY, Icon.PORTAL, Theme.WILD,
                        List.of(new Goal(Milestones.CANOPY, Texts.GOAL_CANOPY), new Goal(Milestones.WILD_BLOOD, Texts.GOAL_WILD_BLOOD)),
                        List.of(
                                new Page(Texts.P_CANOPY_1_T, List.of(new Diagram(Diagrams.Kind.CANOPY, 62), new Para(Texts.P_CANOPY_1_A),
                                        new Recipe(ModItems.CANOPY_KEY))),
                                new Page(Texts.P_CANOPY_2_T, List.of(new Diagram(Diagrams.Kind.BACKCROSS, 64), new Para(Texts.P_CANOPY_2_A))))),
                new Chapter(Hint.Chapter.MARKET, Texts.CH_MARKET, Icon.GRID, Theme.ACCENT_BANK,
                        List.of(new Goal(Milestones.PRIME, Texts.GOAL_PRIME), new Goal(Milestones.FIRST_SALE, Texts.GOAL_FIRST_SALE),
                                new Goal(Milestones.FIRST_CONTRACT, Texts.GOAL_FIRST_CONTRACT)),
                        List.of(
                                new Page(Texts.P_MARKET_1_T, List.of(new Diagram(Diagrams.Kind.GRADES, 52), new Para(Texts.P_MARKET_1_A),
                                        new Tip(Texts.P_MARKET_1_TIP))),
                                new Page(Texts.P_MARKET_2_T, List.of(new Para(Texts.P_MARKET_2_A), new Diagram(Diagrams.Kind.MARKET, 52),
                                        new Recipe(ModItems.PRODUCE_EXCHANGE))),
                                new Page(Texts.P_MARKET_3_T, List.of(new Para(Texts.P_MARKET_3_A),
                                        new Showcase(List.of(ModItems.CONTRACT_BOARD, ModItems.CANOPY_RESIN, ModItems.EDIT_REAGENT,
                                                ModItems.GENE_EDITOR_BLUEPRINT)),
                                        new Recipe(ModItems.CONTRACT_BOARD))))),
                new Chapter(Hint.Chapter.PROCESSING, Texts.CH_PROCESSING, Icon.SPLIT, Theme.ACCENT_COMPOST,
                        List.of(new Goal(Milestones.PROCESSED, Texts.GOAL_PROCESSED), new Goal(Milestones.RATION, Texts.GOAL_RATION)),
                        List.of(
                                new Page(Texts.P_PROC_1_T, List.of(
                                        new Flow(List.of(stack(() -> ModItems.produce(CropSpecies.SOYBEAN)), stack(ModItems.PROCESSING_STATION),
                                                stack(ModItems.SOY_OIL)), List.of(Texts.DG_INSERT, Texts.DG_PRESSES)),
                                        new Para(Texts.P_PROC_1_A), new Recipe(ModItems.PROCESSING_STATION))),
                                new Page(Texts.P_PROC_2_T, List.of(new Para(Texts.P_PROC_2_A),
                                        new Showcase(List.of(ModItems.FIELD_MEAL, ModItems.STARCH, ModItems.BIOPLASTIC, ModItems.SOY_OIL,
                                                ModItems.LINEN_CANVAS, ModItems.PICKLED_BRASSICA, ModItems.GOURD_PUREE, ModItems.BIOFUEL_CAKE)),
                                        new Tip(Texts.P_PROC_2_TIP))),
                                new Page(Texts.P_PROC_3_T, List.of(new Para(Texts.P_PROC_3_A),
                                        new Showcase(List.of(ModItems.RATION_PACK, ModItems.LINEN_CANVAS)), new Para(Texts.P_PROC_3_B))))),
                new Chapter(Hint.Chapter.MASTERY, Texts.CH_MASTERY, Icon.SHIELD, Theme.ACCENT_CARD,
                        List.of(new Goal(Milestones.ATLAS_TIER_1, Texts.GOAL_ATLAS), new Goal(Milestones.GENE_EDIT, Texts.GOAL_GENE_EDIT),
                                new Goal(Milestones.GOLD_RIBBON, Texts.GOAL_GOLD_RIBBON), new Goal(Milestones.SHOW_RECORD, Texts.GOAL_SHOW_RECORD)),
                        List.of(
                                new Page(Texts.P_MASTERY_1_T, List.of(new Diagram(Diagrams.Kind.ATLAS, 80), new Para(Texts.P_MASTERY_1_A))),
                                new Page(Texts.P_MASTERY_2_T, List.of(new Para(Texts.P_MASTERY_2_A),
                                        new Showcase(List.of(ModItems.GENE_EDITOR, ModItems.EDIT_REAGENT, ModItems.GENE_EDITOR_BLUEPRINT)),
                                        new Recipe(ModItems.GENE_EDITOR))),
                                new Page(Texts.P_MASTERY_3_T, List.of(new Para(Texts.P_MASTERY_3_A),
                                        new Showcase(List.of(ModItems.SHOW_BENCH, ModItems.RIBBON_BRONZE, ModItems.RIBBON_SILVER, ModItems.RIBBON_GOLD)),
                                        new Recipe(ModItems.SHOW_BENCH))))));
    }
}
