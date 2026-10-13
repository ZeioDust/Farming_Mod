package com.cultivar.datagen;

import com.cultivar.Cultivar;
import com.cultivar.data.CropSpecies;
import com.cultivar.processing.ProcessingRecipe;
import com.cultivar.processing.RationRecipe;
import com.cultivar.recipe.CompostingRecipe;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class CultivarRecipeProvider extends RecipeProvider {
    protected CultivarRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    private String name(String path) {
        return Cultivar.MODID + ":" + path;
    }

    @Override
    protected void buildRecipes() {
        seedPackets();
        produce();
        tools();
        amendments();
        blocks();
        composting();
        processing();
        market();
    }

    private void seedPackets() {
        ItemLike[] sources = {Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.COCOA_BEANS, Items.SHORT_GRASS, Items.POTATO, Items.BEETROOT_SEEDS,
                Items.PUMPKIN_SEEDS, Items.STRING};
        for (CropSpecies species : CropSpecies.VALUES) {
            ItemLike source = sources[species.ordinal()];
            shapeless(RecipeCategory.MISC, ModItems.seedPacket(species))
                    .requires(source).requires(Items.PAPER)
                    .unlockedBy(getHasName(source), has(source))
                    .save(output, name(species.seedPacketName() + "_from_" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(source.asItem()).getPath()));
        }
    }

    private void produce() {
        shaped(RecipeCategory.FOOD, Items.BREAD).define('W', ModItems.produce(CropSpecies.WHEATLINE)).pattern("WWW")
                .unlockedBy(getHasName(ModItems.produce(CropSpecies.WHEATLINE)), has(ModItems.produce(CropSpecies.WHEATLINE)))
                .save(output, name("bread_from_wheatline"));
        shaped(RecipeCategory.MISC, Items.STRING).define('F', ModItems.produce(CropSpecies.FIBER_FLAX)).pattern("FFF")
                .unlockedBy(getHasName(ModItems.produce(CropSpecies.FIBER_FLAX)), has(ModItems.produce(CropSpecies.FIBER_FLAX)))
                .save(output, name("string_from_flax_fiber"));
        shaped(RecipeCategory.MISC, Items.PAPER, 3).define('F', ModItems.produce(CropSpecies.FIBER_FLAX)).pattern("FFF").pattern("FFF")
                .unlockedBy(getHasName(ModItems.produce(CropSpecies.FIBER_FLAX)), has(ModItems.produce(CropSpecies.FIBER_FLAX)))
                .save(output, name("paper_from_flax_fiber"));
        shapeless(RecipeCategory.FOOD, Items.PUMPKIN_PIE).requires(ModItems.produce(CropSpecies.GOURD)).requires(Items.SUGAR)
                .unlockedBy(getHasName(ModItems.produce(CropSpecies.GOURD)), has(ModItems.produce(CropSpecies.GOURD)))
                .save(output, name("pumpkin_pie_from_gourd"));
    }

    private void tools() {
        shaped(RecipeCategory.TOOLS, ModItems.SOIL_PROBE.get()).define('C', Items.COPPER_INGOT).define('I', Items.IRON_INGOT).define('R', Items.REDSTONE)
                .pattern(" R ").pattern(" I ").pattern(" C ").unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT)).save(output);
        shaped(RecipeCategory.TOOLS, ModItems.HAND_LENS.get()).define('G', Items.GLASS_PANE).define('N', Items.GOLD_NUGGET).define('S', Items.STICK)
                .pattern(" N ").pattern("NGN").pattern(" S ").unlockedBy(getHasName(Items.GLASS_PANE), has(Items.GLASS_PANE)).save(output);
        shaped(RecipeCategory.TOOLS, ModItems.POLLINATION_BRUSH.get()).define('W', ItemTags.WOOL).define('S', Items.STICK)
                .pattern("W").pattern("S").pattern("S").unlockedBy(getHasName(Items.STICK), has(Items.STICK)).save(output);
        shaped(RecipeCategory.TOOLS, ModItems.ISOLATION_BAG.get(), 4).define('P', Items.PAPER).define('S', Items.STRING)
                .pattern("PSP").pattern("P P").pattern("PPP").unlockedBy(getHasName(Items.PAPER), has(Items.PAPER)).save(output);
        shaped(RecipeCategory.TOOLS, ModItems.GRAFTING_KNIFE.get()).define('I', Items.IRON_INGOT).define('S', Items.STICK)
                .pattern(" I").pattern("S ").unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.FIELD_MANUAL.get()).requires(Items.BOOK).requires(tag(ModTags.Items.SEED_PACKETS))
                .unlockedBy("has_book", has(Items.BOOK)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.FIELD_NOTEBOOK.get()).requires(Items.BOOK).requires(Items.GREEN_DYE)
                .unlockedBy(getHasName(Items.BOOK), has(Items.BOOK)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.PEDIGREE_SHEET.get(), 4).requires(Items.PAPER, 4).requires(Items.BLACK_DYE)
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.TRIAL_TAG.get()).requires(Items.PAPER).requires(Items.STRING).requires(Items.STICK)
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER)).save(output);
        shaped(RecipeCategory.MISC, ModItems.MUTAGEN_VIAL.get(), 2).define('B', Items.GLASS_BOTTLE).define('S', ModItems.SULFUR_POWDER.get())
                .define('R', Items.REDSTONE).pattern(" R ").pattern("SBS").pattern(" B ")
                .unlockedBy(getHasName(ModItems.SULFUR_POWDER.get()), has(ModItems.SULFUR_POWDER.get())).save(output);
        shaped(RecipeCategory.MISC, ModItems.CANOPY_KEY.get()).define('R', ModItems.CANOPY_RESIN.get()).define('G', Items.GOLD_INGOT)
                .pattern("RRR").pattern("RGR").pattern("RRR")
                .unlockedBy(getHasName(ModItems.CANOPY_RESIN.get()), has(ModItems.CANOPY_RESIN.get())).save(output);
    }

    private void amendments() {
        shapeless(RecipeCategory.MISC, ModItems.LIME_POWDER.get(), 4).requires(Items.CALCITE)
                .unlockedBy(getHasName(Items.CALCITE), has(Items.CALCITE)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.LIME_POWDER.get(), 2).requires(Items.BONE_MEAL).requires(Items.DIORITE)
                .unlockedBy(getHasName(Items.DIORITE), has(Items.DIORITE)).save(output, name("lime_powder_from_diorite"));
        shapeless(RecipeCategory.MISC, ModItems.SULFUR_POWDER.get(), 2).requires(Items.GUNPOWDER).requires(Items.COAL)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER)).save(output);
        Item compost = ModItems.COMPOST.get();
        shapeless(RecipeCategory.MISC, ModItems.NUTRIENT_BLEND_N.get(), 2).requires(compost).requires(ModItems.produce(CropSpecies.CLOVER))
                .requires(ModItems.produce(CropSpecies.SOYBEAN)).unlockedBy(getHasName(compost), has(compost)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.NUTRIENT_BLEND_P.get(), 2).requires(compost).requires(Items.BONE_MEAL, 2)
                .unlockedBy(getHasName(compost), has(compost)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.NUTRIENT_BLEND_K.get(), 2).requires(compost).requires(Items.CHARCOAL).requires(Items.KELP)
                .unlockedBy(getHasName(compost), has(compost)).save(output);
    }

    private void blocks() {
        Item compost = ModItems.COMPOST.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.MANAGED_SOIL.get(), 4).define('D', Items.DIRT).define('C', compost)
                .pattern("DD").pattern("DC").pattern("DD").unlockedBy(getHasName(compost), has(compost)).save(output);
        shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.BREEDING_PLOT.get(), 4).define('S', ModItems.MANAGED_SOIL.get()).define('C', compost)
                .define('B', Items.BONE_MEAL).pattern("SCS").pattern("SBS")
                .unlockedBy(getHasName(ModItems.MANAGED_SOIL.get()), has(ModItems.MANAGED_SOIL.get())).save(output);
        shaped(RecipeCategory.DECORATIONS, ModItems.COMPOST_BIN.get()).define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS)
                .pattern("S S").pattern("S S").pattern("PPP").unlockedBy("has_planks", has(ItemTags.PLANKS)).save(output);
        shaped(RecipeCategory.MISC, ModItems.LIME_HOPPER.get()).define('H', Items.HOPPER).define('L', ModItems.LIME_POWDER.get()).define('I', Items.IRON_INGOT)
                .pattern("ILI").pattern(" H ").unlockedBy(getHasName(ModItems.LIME_POWDER.get()), has(ModItems.LIME_POWDER.get())).save(output);
        shaped(RecipeCategory.MISC, ModItems.SULFUR_HOPPER.get()).define('H', Items.HOPPER).define('L', ModItems.SULFUR_POWDER.get()).define('I', Items.IRON_INGOT)
                .pattern("ILI").pattern(" H ").unlockedBy(getHasName(ModItems.SULFUR_POWDER.get()), has(ModItems.SULFUR_POWDER.get())).save(output);
        shaped(RecipeCategory.MISC, ModItems.NUTRIENT_SPREADER.get()).define('D', Items.DISPENSER).define('H', Items.HOPPER).define('I', Items.IRON_INGOT)
                .pattern("IHI").pattern("IDI").unlockedBy(getHasName(Items.DISPENSER), has(Items.DISPENSER)).save(output);
        shaped(RecipeCategory.DECORATIONS, ModItems.ISOLATION_SCREEN.get(), 4).define('S', Items.STRING).define('W', ItemTags.WOOL)
                .pattern("SSS").pattern("SWS").pattern("SSS").unlockedBy(getHasName(Items.STRING), has(Items.STRING)).save(output);
        shaped(RecipeCategory.MISC, ModItems.POLLINATION_UNIT.get()).define('I', Items.IRON_INGOT).define('R', Items.REDSTONE).define('G', Items.GLASS)
                .define('B', ModItems.POLLINATION_BRUSH.get()).pattern("IGI").pattern("RBR").pattern("III")
                .unlockedBy(getHasName(ModItems.POLLINATION_BRUSH.get()), has(ModItems.POLLINATION_BRUSH.get())).save(output);
        shaped(RecipeCategory.MISC, ModItems.BURN_PAN.get()).define('I', Items.IRON_INGOT).define('C', Items.CAMPFIRE)
                .pattern("ICI").pattern("III").unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT)).save(output);
        shaped(RecipeCategory.MISC, ModItems.HARVEST_COLLECTOR.get()).define('C', Items.CHEST).define('H', Items.HOPPER).define('I', Items.IRON_INGOT)
                .pattern("IHI").pattern("ICI").pattern("III").unlockedBy(getHasName(Items.CHEST), has(Items.CHEST)).save(output);
        shaped(RecipeCategory.DECORATIONS, ModItems.TRIAL_MARKER.get(), 2).define('P', ItemTags.PLANKS).define('S', Items.STICK)
                .pattern("PPP").pattern(" S ").pattern(" S ").unlockedBy(getHasName(Items.STICK), has(Items.STICK)).save(output);
        shaped(RecipeCategory.MISC, ModItems.GENOME_LAB.get()).define('G', Items.GLASS_PANE).define('I', Items.IRON_INGOT).define('R', Items.REDSTONE)
                .define('Q', Items.QUARTZ).pattern("GQG").pattern("IRI").pattern("III").unlockedBy(getHasName(Items.QUARTZ), has(Items.QUARTZ)).save(output);
        shaped(RecipeCategory.MISC, ModItems.SEED_BANK.get()).define('C', Items.CHEST).define('I', Items.IRON_INGOT).define('P', Items.PAPER)
                .define('Q', Items.QUARTZ).pattern("IQI").pattern("PCP").pattern("III").unlockedBy(getHasName(Items.CHEST), has(Items.CHEST)).save(output);
        shaped(RecipeCategory.MISC, ModItems.SEED_VAULT_DRAWER.get(), 2).define('C', Items.CHEST).define('I', Items.IRON_INGOT).define('P', ItemTags.PLANKS)
                .pattern("PIP").pattern("PCP").pattern("PIP").unlockedBy(getHasName(Items.CHEST), has(Items.CHEST)).save(output);
        shaped(RecipeCategory.MISC, ModItems.MUTAGEN_CHAMBER.get()).define('G', Items.GLASS).define('I', Items.IRON_INGOT).define('R', Items.REDSTONE)
                .define('V', ModItems.MUTAGEN_VIAL.get()).pattern("IGI").pattern("GVG").pattern("IRI")
                .unlockedBy(getHasName(ModItems.MUTAGEN_VIAL.get()), has(ModItems.MUTAGEN_VIAL.get())).save(output);
        shaped(RecipeCategory.MISC, ModItems.THERMAL_SHOCK_UNIT.get()).define('F', Items.FURNACE).define('P', Items.PACKED_ICE).define('I', Items.IRON_INGOT)
                .pattern("III").pattern("FIP").pattern("III").unlockedBy(getHasName(Items.PACKED_ICE), has(Items.PACKED_ICE)).save(output);
        shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.GREENHOUSE_PANE.get(), 8).define('G', Items.GLASS).define('I', Items.IRON_INGOT)
                .pattern("GGG").pattern("GIG").pattern("GGG").unlockedBy(getHasName(Items.GLASS), has(Items.GLASS)).save(output);
        shaped(RecipeCategory.MISC, ModItems.GROW_LIGHT.get()).define('G', Items.GLOWSTONE).define('I', Items.IRON_INGOT).define('P', Items.GLASS_PANE)
                .define('R', Items.REDSTONE).pattern("IRI").pattern("IGI").pattern("PPP").unlockedBy(getHasName(Items.GLOWSTONE), has(Items.GLOWSTONE)).save(output);
        shaped(RecipeCategory.MISC, ModItems.HUMIDIFIER.get()).define('B', Items.BUCKET).define('I', Items.IRON_INGOT).define('G', Items.GLASS)
                .define('R', Items.REDSTONE).pattern("IGI").pattern("IBI").pattern("IRI").unlockedBy(getHasName(Items.BUCKET), has(Items.BUCKET)).save(output);
        shaped(RecipeCategory.MISC, ModItems.DEHUMIDIFIER.get()).define('C', Items.CHARCOAL).define('I', Items.IRON_INGOT).define('G', Items.GLASS)
                .define('R', Items.REDSTONE).pattern("IGI").pattern("ICI").pattern("IRI").unlockedBy(getHasName(Items.CHARCOAL), has(Items.CHARCOAL)).save(output);
        shaped(RecipeCategory.MISC, ModItems.CLIMATE_CONTROLLER.get()).define('C', Items.COMPARATOR).define('I', Items.IRON_INGOT).define('G', Items.GLASS_PANE)
                .define('Q', Items.QUARTZ).define('R', Items.REDSTONE).pattern("IGI").pattern("QCQ").pattern("IRI")
                .unlockedBy(getHasName(Items.COMPARATOR), has(Items.COMPARATOR)).save(output);
        shaped(RecipeCategory.MISC, ModItems.IRRIGATION_LINE.get(), 8).define('C', Items.COPPER_INGOT)
                .pattern("CCC").unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT)).save(output);
        shaped(RecipeCategory.MISC, ModItems.SPRINKLER.get()).define('C', Items.COPPER_INGOT).define('L', ModItems.IRRIGATION_LINE.get()).define('I', Items.IRON_INGOT)
                .pattern(" C ").pattern("CLC").pattern(" I ")
                .unlockedBy(getHasName(ModItems.IRRIGATION_LINE.get()), has(ModItems.IRRIGATION_LINE.get())).save(output);
    }

    private void composting() {
        compost("produce", tag(ModTags.Items.PRODUCE), 25);
        compost("plant_waste", tag(ModTags.Items.PLANT_WASTE), 15);
        compost("seed_packets", tag(ModTags.Items.SEED_PACKETS), 10);
        compost("blight_sample", Ingredient.of(ModItems.BLIGHT_SAMPLE.get()), 40);
        compost("canopy_leafmass", Ingredient.of(ModItems.CANOPY_LEAFMASS.get()), 20);
    }

    private void processing() {
        process("field_meal", ModItems.produce(CropSpecies.WHEATLINE), 2, ModItems.FIELD_MEAL.get(), 1, 160);
        process("starch_from_maize", ModItems.produce(CropSpecies.MAIZE), 2, ModItems.STARCH.get(), 1, 160);
        process("starch_from_tuber", ModItems.produce(CropSpecies.TUBER), 2, ModItems.STARCH.get(), 1, 160);
        process("soy_oil", ModItems.produce(CropSpecies.SOYBEAN), 3, ModItems.SOY_OIL.get(), 1, 200);
        process("linen_canvas", ModItems.produce(CropSpecies.FIBER_FLAX), 3, ModItems.LINEN_CANVAS.get(), 1, 240);
        process("pickled_brassica", ModItems.produce(CropSpecies.BRASSICA), 2, ModItems.PICKLED_BRASSICA.get(), 1, 200);
        process("gourd_puree", ModItems.produce(CropSpecies.GOURD), 1, ModItems.GOURD_PUREE.get(), 2, 140);
        process("biofuel_cake", ModItems.produce(CropSpecies.CLOVER), 4, ModItems.BIOFUEL_CAKE.get(), 1, 160);
        process("bioplastic", ModItems.STARCH.get(), 2, ModItems.BIOPLASTIC.get(), 1, 300);
    }

    private void process(String path, ItemLike input, int count, Item result, int resultCount, int ticks) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Cultivar.MODID, "processing/" + path));
        output.accept(key, new ProcessingRecipe(Ingredient.of(input), count, new ItemStackTemplate(result, resultCount), ticks), null);
    }

    private void market() {
        Item paper = Items.PAPER;
        shaped(RecipeCategory.MISC, ModItems.PRODUCE_EXCHANGE.get()).define('P', paper).define('I', Items.IRON_INGOT).define('C', Items.CHEST)
                .define('L', ItemTags.LOGS).pattern("PPP").pattern("ICI").pattern("LLL").unlockedBy(getHasName(Items.CHEST), has(Items.CHEST)).save(output);
        shaped(RecipeCategory.MISC, ModItems.CONTRACT_BOARD.get()).define('P', paper).define('F', Items.ITEM_FRAME).define('L', ItemTags.LOGS)
                .pattern("PPP").pattern("PFP").pattern("LLL").unlockedBy(getHasName(paper), has(paper)).save(output);
        shaped(RecipeCategory.MISC, ModItems.PROCESSING_STATION.get()).define('I', Items.IRON_INGOT).define('G', Items.GRINDSTONE)
                .define('P', Items.PISTON).define('S', Items.SMOOTH_STONE).pattern("IGI").pattern("PSP").pattern("SSS")
                .unlockedBy(getHasName(Items.GRINDSTONE), has(Items.GRINDSTONE)).save(output);
        shaped(RecipeCategory.MISC, ModItems.SHOW_BENCH.get()).define('G', Items.GOLD_INGOT).define('P', paper).define('W', ItemTags.PLANKS)
                .pattern("GPG").pattern("WWW").pattern("W W").unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT)).save(output);
        Item blueprint = ModItems.GENE_EDITOR_BLUEPRINT.get();
        shaped(RecipeCategory.MISC, ModItems.GENE_EDITOR.get()).define('B', blueprint).define('L', ModItems.GENOME_LAB.get())
                .define('M', ModItems.MUTAGEN_CHAMBER.get()).define('P', ModItems.BIOPLASTIC.get()).define('D', Items.DIAMOND)
                .pattern("PDP").pattern("LBM").pattern("PDP").unlockedBy(getHasName(blueprint), has(blueprint)).save(output);
        shapeless(RecipeCategory.MISC, ModItems.EDIT_REAGENT.get()).requires(ModItems.MUTAGEN_VIAL.get()).requires(ModItems.CANOPY_RESIN.get())
                .requires(ModItems.BIOPLASTIC.get()).requires(ModItems.FIELD_MEAL.get())
                .unlockedBy(getHasName(ModItems.BIOPLASTIC.get()), has(ModItems.BIOPLASTIC.get())).save(output);
        shapeless(RecipeCategory.MISC, ModItems.GREEN_MANURE.get(), 2).requires(ModItems.produce(CropSpecies.CLOVER), 3).requires(ModItems.COMPOST.get())
                .unlockedBy(getHasName(ModItems.produce(CropSpecies.CLOVER)), has(ModItems.produce(CropSpecies.CLOVER))).save(output);
        SpecialRecipeBuilder.special(() -> RationRecipe.INSTANCE)
                .unlockedBy(getHasName(ModItems.LINEN_CANVAS.get()), has(ModItems.LINEN_CANVAS.get())).save(output, name("ration_pack"));
    }

    private void compost(String path, Ingredient ingredient, int charge) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Cultivar.MODID, "composting/" + path));
        output.accept(key, new CompostingRecipe(ingredient, charge), null);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new CultivarRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Cultivar Recipes";
        }
    }
}
