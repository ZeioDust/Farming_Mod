package com.cultivar.command;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.BlightStrain;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.data.Nutrient;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Inheritance;
import com.cultivar.genetics.Phenotype;
import com.cultivar.genetics.PunnettSimulator;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.util.Texts;
import com.cultivar.world.AsyncWork;
import com.cultivar.world.BlightSystem;
import com.cultivar.world.ClimateIndex;
import com.cultivar.world.HarvestLogic;
import com.cultivar.world.LevelState;
import com.cultivar.world.TickStats;
import com.cultivar.world.WindSystem;
import com.cultivar.world.WindVector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SplittableRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class CultivarCommands {
    public static final int DEFAULT_SAMPLES = 10_000;

    private CultivarCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("cultivar")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("debug").executes(CultivarCommands::debug))
                .then(Commands.literal("stats").executes(ctx -> {
                    printTickCosts(ctx.getSource(), ctx.getSource().getLevel());
                    return 1;
                }))
                .then(Commands.literal("punnett")
                        .then(Commands.literal("held").executes(ctx -> punnettHeld(ctx, DEFAULT_SAMPLES)))
                        .then(Commands.argument("mother", StringArgumentType.word())
                                .then(Commands.argument("father", StringArgumentType.word())
                                        .executes(ctx -> punnett(ctx, DEFAULT_SAMPLES, false))
                                        .then(Commands.argument("samples", IntegerArgumentType.integer(100, 1_000_000))
                                                .executes(ctx -> punnett(ctx, IntegerArgumentType.getInteger(ctx, "samples"), false))
                                                .then(Commands.argument("mutation", BoolArgumentType.bool())
                                                        .executes(ctx -> punnett(ctx, IntegerArgumentType.getInteger(ctx, "samples"),
                                                                BoolArgumentType.getBool(ctx, "mutation"))))))))
                .then(Commands.literal("seed")
                        .then(Commands.argument("species", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (CropSpecies s : CropSpecies.VALUES) {
                                        builder.suggest(s.id());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("genome", StringArgumentType.word()).executes(CultivarCommands::seed)))));
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        HitResult hit = player.pick(8.0, 0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || !(level.getBlockEntity(blockHit.getBlockPos()) instanceof CropBlockEntity crop)) {
            source.sendFailure(Texts.tr(Texts.CMD_NO_TARGET));
            printTickCosts(source, level);
            return 0;
        }
        crop.ensureGenome(level);
        BlockPos pos = crop.getBlockPos();
        Catalog catalog = Catalog.get(level);
        AlleleTable table = catalog.table();
        Genome genome = crop.genome();
        Phenotype phenotype = crop.phenotype();
        line(source, ChatFormatting.GOLD, "== %s at %s ==", crop.speciesKey(), pos.toShortString());
        line(source, ChatFormatting.GRAY, "genome %s  line #%d  wild=%s grafted=%s true-breeding=%s",
                genome != null ? genome.toHex() : "-", crop.lineage(), crop.isWild(), crop.isGrafted(), genome != null && genome.isTrueBreeding());
        if (genome != null) {
            for (Gene gene : Gene.VALUES) {
                Allele a = table.get(gene, genome.first(gene));
                Allele b = table.get(gene, genome.second(gene));
                String expr = switch (phenotype.expression(gene)) {
                    case Phenotype.EXPRESSED_FIRST -> "A";
                    case Phenotype.EXPRESSED_SECOND -> "B";
                    default -> "A+B";
                };
                line(source, ChatFormatting.WHITE, "  %-18s A=%s(%.3f,d%d) B=%s(%.3f,d%d) expr=%s -> %s", gene.id(), a.id(), table.effectiveValue(a),
                        a.dominance(), b.id(), table.effectiveValue(b), b.dominance(), expr, gene.format(phenotype.get(gene)));
            }
        }
        line(source, ChatFormatting.GRAY, "growth progress %.0f, stress %d, limit %s, pollinated=%s (%s), donor %s",
                crop.progress(), crop.stress(), crop.lastLimit(), crop.isPollinated(), crop.pollinationMethod().getSerializedName(),
                crop.donor() != null ? crop.donor().toHex() : "-");
        if (level.getBlockEntity(pos.below()) instanceof ManagedSoilBlockEntity soil) {
            line(source, ChatFormatting.YELLOW, "soil N %.1f P %.1f K %.1f pH %.2f moisture %d/7 last=%s fixer=%s",
                    soil.get(Nutrient.NITROGEN), soil.get(Nutrient.PHOSPHORUS), soil.get(Nutrient.POTASSIUM), soil.ph(), soil.moisture(),
                    soil.lastSpecies(), soil.lastWasFixer());
        } else {
            line(source, ChatFormatting.YELLOW, "soil: unmanaged (%s)", level.getBlockState(pos.below()).getBlock().getName().getString());
        }
        int radius = CultivarConfig.BLIGHT_SPREAD_RADIUS.get();
        LevelState state = LevelState.get(level);
        List<CropBlockEntity> neighbours = BlightSystem.neighbours(level, state, crop, radius);
        long infected = neighbours.stream().filter(CropBlockEntity::isBlighted).count();
        float similarity = crop.neighbourhoodSimilarity();
        BlightSystem.refreshSimilarityIfStale(level, crop, radius);
        float density = BlightSystem.localDensity(level, crop, radius);
        float moisture = BlightSystem.soilMoisture(level, pos);
        line(source, ChatFormatting.RED, "blight: strain=%s ticks=%d | neighbours %d (infected %d) similarity %s mono x%.2f density %.2f moisture %.2f",
                crop.strain(), crop.blightTicks(), neighbours.size(), infected, similarity < 0 ? "pending" : String.format(Locale.ROOT, "%.3f", similarity),
                BlightSystem.monocultureMultiplier(Math.max(0, similarity)), density, moisture);
        for (Identifier strainId : catalog.strainsFor(crop.speciesKey())) {
            BlightStrain strain = catalog.strain(strainId).orElseThrow();
            boolean immune = BlightSystem.isImmune(crop, strain, table);
            line(source, ChatFormatting.RED, "  vs %s: immune=%s resistance %.2f transmission-if-exposed %.3f", strainId, immune,
                    phenotype.get(Gene.BLIGHT_RESISTANCE), BlightSystem.transmission(level, crop, crop, strain));
        }
        WindVector wind = WindSystem.get(level, pos);
        line(source, ChatFormatting.AQUA, "wind toward %s strength %.2f | greenhouse %s", wind.compass(), wind.strength(),
                ClimateIndex.controllerFor(level, pos) != null ? "sealed" : "none");
        printTickCosts(source, level);
        return 1;
    }

    private static void printTickCosts(CommandSourceStack source, ServerLevel level) {
        double total = 0;
        line(source, ChatFormatting.DARK_AQUA, "Cultivar server cost per tick, last %s s (average / peak / last, work items):", TickStats.WINDOW / 20);
        for (Map.Entry<String, TickStats.Figures> entry : TickStats.figures().entrySet()) {
            TickStats.Figures f = entry.getValue();
            total += f.average();
            line(source, ChatFormatting.DARK_AQUA, "  %s: %.3f / %.3f / %.3f ms (%d)", entry.getKey(), f.average() / 1_000_000.0, f.peak() / 1_000_000.0,
                    f.last() / 1_000_000.0, f.items());
        }
        line(source, ChatFormatting.AQUA, "  total average %.3f ms of the 50 ms tick budget", total / 1_000_000.0);
        LevelState state = LevelState.get(level);
        line(source, ChatFormatting.DARK_AQUA, "crops indexed %d, infected %d, blight queue %d, async pending %d", state.cropCount(),
                state.infectedCount(), state.blight().queued(), AsyncWork.pending());
    }

    private static void line(CommandSourceStack source, ChatFormatting color, String format, Object... args) {
        String text = String.format(Locale.ROOT, format, args);
        source.sendSuccess(() -> Component.literal(text).withStyle(color), false);
    }

    private static int punnett(CommandContext<CommandSourceStack> ctx, int samples, boolean mutation) {
        Genome mother;
        Genome father;
        try {
            mother = Genome.fromHex(StringArgumentType.getString(ctx, "mother"));
            father = Genome.fromHex(StringArgumentType.getString(ctx, "father"));
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Texts.tr(Texts.CMD_BAD_GENOME));
            return 0;
        }
        return runPunnett(ctx.getSource(), mother, father, samples, mutation);
    }

    private static int punnettHeld(CommandContext<CommandSourceStack> ctx, int samples) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Genome mother = player.getMainHandItem().get(ModDataComponents.GENOME.get());
        Genome father = player.getOffhandItem().get(ModDataComponents.GENOME.get());
        if (mother == null || father == null) {
            ctx.getSource().sendFailure(Texts.tr(Texts.CMD_BAD_GENOME));
            return 0;
        }
        return runPunnett(ctx.getSource(), mother, father, samples, false);
    }

    public static int runPunnett(CommandSourceStack source, Genome mother, Genome father, int samples, boolean mutation) {
        AlleleTable table = Catalog.get(source.getLevel()).table();
        Inheritance.MutationSettings settings = mutation
                ? new Inheritance.MutationSettings(true, CultivarConfig.MUTATION_RATE_MULTIPLIER.get().floatValue(),
                CultivarConfig.MUTATION_ONLY_ALLELES_ENABLED.get())
                : Inheritance.MutationSettings.NONE;
        long seed = source.getLevel().getRandom().nextLong();
        source.sendSuccess(() -> Texts.tr(Texts.CMD_PUNNETT_START, samples), false);
        line(source, ChatFormatting.GRAY, "mother %s", mother.toHex());
        line(source, ChatFormatting.GRAY, "father %s", father.toHex());
        AsyncWork.submit("punnett", () -> PunnettSimulator.simulate(mother, father, table, samples, new SplittableRandom(seed), settings),
                result -> result.describe().forEach(text -> line(source, ChatFormatting.WHITE, "%s", text)));
        return 1;
    }

    private static int seed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String speciesId = StringArgumentType.getString(ctx, "species");
        CropSpecies species = null;
        for (CropSpecies s : CropSpecies.VALUES) {
            if (s.id().equals(speciesId)) {
                species = s;
            }
        }
        Genome genome;
        try {
            genome = Genome.fromHex(StringArgumentType.getString(ctx, "genome"));
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Texts.tr(Texts.CMD_BAD_GENOME));
            return 0;
        }
        if (species == null) {
            ctx.getSource().sendFailure(Texts.tr(Texts.CMD_BAD_GENOME));
            return 0;
        }
        ItemStack stack = HarvestLogic.seedStack(species, genome, 0, false);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        CropSpecies given = species;
        ctx.getSource().sendSuccess(() -> Texts.tr(Texts.CMD_SEED_GIVEN, given.displayName(), genome.toHex()), false);
        return 1;
    }
}
