package com.cultivar.compat.jei;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.BurnPanBlockEntity;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.blockentity.ThermalShockUnitBlockEntity;
import com.cultivar.client.ClientSession;
import com.cultivar.client.screen.GeneEditorScreen;
import com.cultivar.client.screen.MutagenChamberScreen;
import com.cultivar.client.screen.ProcessingStationScreen;
import com.cultivar.client.screen.ShowBenchScreen;
import com.cultivar.data.CropSpecies;
import com.cultivar.item.ProduceItem;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.market.MarketValues;
import com.cultivar.processing.ProcessingRecipe;
import com.cultivar.processing.RationRecipe;
import com.cultivar.recipe.CompostingRecipe;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModRecipes;
import com.cultivar.registry.ModTags;
import com.cultivar.util.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class CultivarJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(Cultivar.MODID, "jei");

    public static final IRecipeType<MachineRecipe> PROCESSING = type("processing");
    public static final IRecipeType<MachineRecipe> COMPOSTING = type("composting");
    public static final IRecipeType<MachineRecipe> RATION = type("ration");
    public static final IRecipeType<MachineRecipe> GENOME_LAB = type("genome_lab");
    public static final IRecipeType<MachineRecipe> MUTAGEN = type("mutagen_chamber");
    public static final IRecipeType<MachineRecipe> THERMAL_SHOCK = type("thermal_shock_unit");
    public static final IRecipeType<MachineRecipe> BURN_PAN = type("burn_pan");
    public static final IRecipeType<MachineRecipe> GENE_EDITOR = type("gene_editor");
    public static final IRecipeType<MachineRecipe> SHOW_BENCH = type("show_bench");
    public static final IRecipeType<MachineRecipe> MARKET = type("produce_exchange");
    public static final IRecipeType<MachineRecipe> SUPPLIES = type("contract_supplies");

    private static IRecipeType<MachineRecipe> type(String path) {
        return IRecipeType.create(Cultivar.MODID, path, MachineRecipe.class);
    }

    private static @org.jspecify.annotations.Nullable IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static long count(String path) {
        return runtime != null ? runtime.getRecipeManager().createRecipeLookup(type(path)).get().count() : -1;
    }

    public static boolean show(String path) {
        if (runtime == null) {
            return false;
        }
        runtime.getRecipesGui().showTypes(List.of(type(path)));
        return true;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MachineCategory(gui, PROCESSING, Texts.tr(Texts.SCREEN_PROCESSING), ModItems.PROCESSING_STATION.get(), 1, 1, 1, 4),
                new MachineCategory(gui, COMPOSTING, Texts.tr(Texts.JEI_COMPOSTING), ModItems.COMPOST_BIN.get(), 1, 1, 1, 2),
                new MachineCategory(gui, RATION, Texts.tr(Texts.JEI_RATION), ModItems.RATION_PACK.get(), 3, 3, 1, 4),
                new MachineCategory(gui, GENOME_LAB, Texts.tr(Texts.JEI_GENOME_LAB), ModItems.GENOME_LAB.get(), 2, 1, 1, 4),
                new MachineCategory(gui, MUTAGEN, Texts.tr(Texts.JEI_MUTAGEN), ModItems.MUTAGEN_CHAMBER.get(), 2, 1, 1, 4),
                new MachineCategory(gui, THERMAL_SHOCK, Texts.tr(Texts.JEI_THERMAL), ModItems.THERMAL_SHOCK_UNIT.get(), 2, 1, 1, 4),
                new MachineCategory(gui, BURN_PAN, Texts.tr(Texts.JEI_BURN_PAN), ModItems.BURN_PAN.get(), 1, 1, 1, 5),
                new MachineCategory(gui, GENE_EDITOR, Texts.tr(Texts.SCREEN_GENE_EDITOR), ModItems.GENE_EDITOR.get(), 2, 1, 1, 4),
                new MachineCategory(gui, SHOW_BENCH, Texts.tr(Texts.SCREEN_SHOW_BENCH), ModItems.SHOW_BENCH.get(), 2, 1, 2, 6),
                new MachineCategory(gui, MARKET, Texts.tr(Texts.SCREEN_EXCHANGE), ModItems.PRODUCE_EXCHANGE.get(), 1, 1, 1, 4),
                new MachineCategory(gui, SUPPLIES, Texts.tr(Texts.JEI_SUPPLIES), ModItems.CONTRACT_BOARD.get(), 1, 1, 1, 2));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PROCESSING, processing());
        registration.addRecipes(COMPOSTING, composting());
        registration.addRecipes(RATION, rations());
        registration.addRecipes(GENOME_LAB, genomeLab());
        registration.addRecipes(MUTAGEN, List.of(mutation(List.of(List.of(new ItemStack(ModItems.MUTAGEN_VIAL.get()))),
                config(CultivarConfig.MUTAGEN_SUCCESS_CHANCE, 0.6), config(CultivarConfig.MUTAGEN_DESTROY_CHANCE, 0.2),
                Texts.tr(Texts.JEI_MUTAGEN_NOTE, MutagenChamberBlockEntity.USES_PER_VIAL, seconds(MutagenChamberBlockEntity.TICKS_PER_SEED)))));
        registration.addRecipes(THERMAL_SHOCK, List.of(mutation(List.of(tag(ModTags.Items.COOLANTS)),
                config(CultivarConfig.THERMAL_SHOCK_SUCCESS_CHANCE, 0.25), config(CultivarConfig.THERMAL_SHOCK_DESTROY_CHANCE, 0.08),
                Texts.tr(Texts.JEI_THERMAL_NOTE, seconds(ThermalShockUnitBlockEntity.TICKS_PER_SEED)))));
        registration.addRecipes(BURN_PAN, List.of(MachineRecipe.of(List.of(tag(ModTags.Items.BURN_FUEL)), List.of(new ItemStack(ModItems.BLIGHT_SAMPLE.get())),
                Texts.tr(Texts.JEI_BURN_NOTE_1, BurnPanBlockEntity.RADIUS), Texts.tr(Texts.JEI_BURN_NOTE_2, BurnPanBlockEntity.BURN_PER_FUEL))));
        registration.addRecipes(GENE_EDITOR, List.of(MachineRecipe.of(List.of(packets(), List.of(new ItemStack(ModItems.EDIT_REAGENT.get()))), packets(),
                Texts.tr(Texts.JEI_EDITOR_NOTE_1), Texts.tr(Texts.JEI_EDITOR_NOTE_2))));
        registration.addRecipes(SHOW_BENCH, List.of(showBench()));
        registration.addRecipes(MARKET, market());
        registration.addRecipes(SUPPLIES, supplies());
        info(registration);
    }

    private static List<MachineRecipe> processing() {
        List<MachineRecipe> out = new ArrayList<>();
        for (RecipeHolder<ProcessingRecipe> holder : ClientSession.recipes().byType(ModRecipes.PROCESSING.get())) {
            ProcessingRecipe recipe = holder.value();
            out.add(MachineRecipe.of(List.of(MachineCategory.counted(items(recipe.input()), recipe.count())), List.of(recipe.result().create()),
                    Texts.tr(Texts.JEI_PROCESSING_TIME, seconds((int) Math.round(recipe.ticks() / speed()))), Texts.tr(Texts.JEI_GRADE_KEPT)));
        }
        return out;
    }

    private static List<MachineRecipe> composting() {
        List<MachineRecipe> out = new ArrayList<>();
        for (RecipeHolder<CompostingRecipe> holder : ClientSession.recipes().byType(ModRecipes.COMPOSTING.get())) {
            CompostingRecipe recipe = holder.value();
            out.add(MachineRecipe.of(List.of(items(recipe.input())), List.of(new ItemStack(ModItems.COMPOST.get())),
                    Texts.tr(Texts.JEI_COMPOST_CHARGE, recipe.charge(), CompostBinBlockEntity.FULL_CHARGE)));
        }
        return out;
    }

    private static List<MachineRecipe> rations() {
        List<MachineRecipe> out = new ArrayList<>();
        ration(out, List.of(produce(CropSpecies.MAIZE, Grade.C), produce(CropSpecies.TUBER, Grade.C), produce(CropSpecies.GOURD, Grade.C)));
        ration(out, List.of(produce(CropSpecies.MAIZE, Grade.A), produce(CropSpecies.TUBER, Grade.A), produce(CropSpecies.GOURD, Grade.A),
                produce(CropSpecies.BRASSICA, Grade.A), produce(CropSpecies.SOYBEAN, Grade.A)));
        List<ItemStack> feast = new ArrayList<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            feast.add(produce(species, Grade.PRIME));
        }
        ration(out, feast);
        return out;
    }

    private static void ration(List<MachineRecipe> out, List<ItemStack> foods) {
        List<ItemStack> grid = new ArrayList<>();
        grid.add(new ItemStack(ModItems.LINEN_CANVAS.get()));
        grid.addAll(foods);
        List<ItemStack> padded = new ArrayList<>(grid);
        while (padded.size() < 9) {
            padded.add(ItemStack.EMPTY);
        }
        ItemStack pack = RationRecipe.INSTANCE.assemble(CraftingInput.of(3, 3, padded));
        var data = pack.get(ModDataComponents.RATION.get());
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (ItemStack stack : grid) {
            inputs.add(List.of(stack));
        }
        out.add(MachineRecipe.of(inputs, List.of(pack), Texts.tr(Texts.JEI_RATION_NOTE_1, data != null ? data.strength() : 1),
                Texts.tr(Texts.JEI_RATION_NOTE_2)));
    }

    private static List<MachineRecipe> genomeLab() {
        List<ItemStack> samples = new ArrayList<>(packets());
        samples.add(new ItemStack(ModItems.WILD_SEED.get()));
        List<ItemStack> card = List.of(new ItemStack(ModItems.GENOME_CARD.get()));
        List<ItemStack> paper = List.of(new ItemStack(Items.PAPER));
        return List.of(
                MachineRecipe.of(List.of(samples, paper), card, Texts.tr(Texts.JEI_LAB_SEED), Texts.tr(Texts.JEI_LAB_KEPT)),
                MachineRecipe.of(List.of(List.of(new ItemStack(ModItems.BLIGHT_SAMPLE.get())), paper), card, Texts.tr(Texts.JEI_LAB_STRAIN),
                        Texts.tr(Texts.JEI_LAB_KEPT)));
    }

    private static MachineRecipe mutation(List<List<ItemStack>> agent, double success, double destroy, Component extra) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(packets());
        inputs.addAll(agent);
        double destroyed = Math.min(1.0, destroy);
        double mutated = Math.min(1.0 - destroyed, success);
        return MachineRecipe.of(inputs, packets(), Texts.tr(Texts.JEI_MUTATION_ODDS, percent(mutated), percent(destroyed),
                percent(Math.max(0.0, 1.0 - destroyed - mutated))), extra);
    }

    private static MachineRecipe showBench() {
        int cost = CultivarConfig.isLoaded() ? CultivarConfig.SHOW_BENCH_PAPER_COST.get() : 1;
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(packets());
        if (cost > 0) {
            inputs.add(List.of(new ItemStack(Items.PAPER, cost)));
        }
        List<List<ItemStack>> outputs = List.of(packets(), List.of(new ItemStack(ModItems.RIBBON_BRONZE.get()),
                new ItemStack(ModItems.RIBBON_SILVER.get()), new ItemStack(ModItems.RIBBON_GOLD.get())));
        return new MachineRecipe(inputs, outputs, List.of(Texts.tr(Texts.JEI_BENCH_NOTE_1),
                Texts.tr(Texts.UI_SB_THRESHOLDS, ShowBenchBlockEntity.BRONZE, ShowBenchBlockEntity.SILVER, ShowBenchBlockEntity.GOLD),
                Texts.tr(Texts.JEI_BENCH_NOTE_2, ShowBenchBlockEntity.TRUE_BREEDING_BONUS)));
    }

    private static List<MachineRecipe> market() {
        List<MachineRecipe> out = new ArrayList<>();
        double multiplier = CultivarConfig.isLoaded() ? CultivarConfig.EXCHANGE_PRICE_MULTIPLIER.get() : 1.0;
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            float base = MarketValues.base(stack);
            if (base <= 0) {
                continue;
            }
            float c = (float) (base * multiplier);
            out.add(MachineRecipe.of(List.of(List.of(stack)), List.of(new ItemStack(Items.EMERALD)),
                    Texts.tr(Texts.UI_EX_GRADES, price(c * Grade.C.priceMultiplier()), price(c * Grade.B.priceMultiplier()),
                            price(c * Grade.A.priceMultiplier()), price(c * Grade.PRIME.priceMultiplier())),
                    Texts.tr(Texts.JEI_MARKET_NOTE)));
        }
        return out;
    }

    private static List<MachineRecipe> supplies() {
        List<MachineRecipe> out = new ArrayList<>();
        for (ContractBoardBlockEntity.Offer offer : ContractBoardBlockEntity.OFFERS) {
            out.add(MachineRecipe.of(List.of(List.of(new ItemStack(Items.EMERALD, offer.price()))), List.of(offer.stack()),
                    Texts.tr(Texts.JEI_SUPPLY_TIER, offer.tier() + 1)));
        }
        return out;
    }

    private static void info(IRecipeRegistration registration) {
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!Cultivar.MODID.equals(id.getNamespace())) {
                continue;
            }
            Texts.Guide guide = Texts.ITEM_GUIDE.get(id.getPath());
            if (guide == null && item instanceof SeedPacketItem) {
                guide = Texts.ITEM_GUIDE.get("seed_packet");
            }
            if (guide == null && item instanceof ProduceItem) {
                guide = Texts.ITEM_GUIDE.get("produce");
            }
            if (guide == null) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            if (guide.hasSummary()) {
                lines.add(Texts.tr(guide.summary()));
            }
            for (String key : guide.details()) {
                lines.add(Texts.tr(key));
            }
            if (!lines.isEmpty()) {
                registration.addIngredientInfo(item, lines.toArray(Component[]::new));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PROCESSING, ModItems.PROCESSING_STATION.get());
        registration.addCraftingStation(COMPOSTING, ModItems.COMPOST_BIN.get());
        registration.addCraftingStation(RATION, Blocks.CRAFTING_TABLE);
        registration.addCraftingStation(GENOME_LAB, ModItems.GENOME_LAB.get());
        registration.addCraftingStation(MUTAGEN, ModItems.MUTAGEN_CHAMBER.get());
        registration.addCraftingStation(THERMAL_SHOCK, ModItems.THERMAL_SHOCK_UNIT.get());
        registration.addCraftingStation(BURN_PAN, ModItems.BURN_PAN.get());
        registration.addCraftingStation(GENE_EDITOR, ModItems.GENE_EDITOR.get());
        registration.addCraftingStation(SHOW_BENCH, ModItems.SHOW_BENCH.get());
        registration.addCraftingStation(MARKET, ModItems.PRODUCE_EXCHANGE.get());
        registration.addCraftingStation(SUPPLIES, ModItems.CONTRACT_BOARD.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ProcessingStationScreen.class, 85, 34, 48, 36, PROCESSING);
        registration.addRecipeClickArea(MutagenChamberScreen.class, 96, 22, 42, 74, MUTAGEN);
        registration.addRecipeClickArea(GeneEditorScreen.class, 44, 92, 164, 12, GENE_EDITOR);
        registration.addRecipeClickArea(ShowBenchScreen.class, 48, 32, 52, 52, SHOW_BENCH);
    }

    private static List<ItemStack> items(Ingredient ingredient) {
        return ingredient.items().map(Holder::value).map(ItemStack::new).toList();
    }

    private static List<ItemStack> tag(TagKey<Item> tag) {
        List<ItemStack> out = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> out.add(new ItemStack(holder.value())));
        return out;
    }

    private static List<ItemStack> packets() {
        List<ItemStack> out = new ArrayList<>();
        for (CropSpecies species : CropSpecies.VALUES) {
            out.add(new ItemStack(ModItems.seedPacket(species)));
        }
        return out;
    }

    private static ItemStack produce(CropSpecies species, Grade grade) {
        return GradeLogic.apply(new ItemStack(ModItems.produce(species)), grade);
    }

    private static double config(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
        return CultivarConfig.isLoaded() ? value.get() : fallback;
    }

    private static double speed() {
        return CultivarConfig.isLoaded() ? CultivarConfig.PROCESSING_SPEED_MULTIPLIER.get() : 1.0;
    }

    private static String seconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20f);
    }

    private static String percent(double chance) {
        return Math.round(chance * 100) + "%";
    }

    private static String price(float emeralds) {
        return String.format(Locale.ROOT, emeralds >= 10 ? "%.0f" : "%.2f", emeralds);
    }
}
