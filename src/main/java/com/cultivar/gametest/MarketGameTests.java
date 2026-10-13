package com.cultivar.gametest;

import static com.cultivar.gametest.CultivarGameTests.HARDY;
import static com.cultivar.gametest.CultivarGameTests.check;

import com.cultivar.CultivarConfig;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.market.Contract;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.market.MarketData;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.mastery.Certificate;
import com.cultivar.processing.RationData;
import com.cultivar.processing.RationRecipe;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.world.HarvestLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;

final class MarketGameTests {
    private MarketGameTests() {
    }

    private static ItemStack produce(CropSpecies species, int count, Grade grade) {
        ItemStack stack = new ItemStack(ModItems.produce(species), count);
        return GradeLogic.apply(stack, grade);
    }

    private static int emeralds(ServerPlayer player) {
        int n = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(Items.EMERALD)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    static void exchangeSells(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.PRODUCE_EXCHANGE.get());
        ProduceExchangeBlockEntity exchange = helper.getBlockEntity(pos, ProduceExchangeBlockEntity.class);
        ServerLevel level = helper.getLevel();
        MarketData market = MarketData.get(level.getServer());
        ItemStack prime = produce(CropSpecies.GOURD, 16, Grade.PRIME);
        ItemStack plain = produce(CropSpecies.GOURD, 16, Grade.C);
        float primeUnit = market.unitPrice(prime);
        float plainUnit = market.unitPrice(plain);
        check(helper, plainUnit > 0 && Math.abs(primeUnit / plainUnit - Grade.PRIME.priceMultiplier()) < 1e-3f,
                "Prime must sell for " + Grade.PRIME.priceMultiplier() + "x Grade C, got " + primeUnit + " vs " + plainUnit);
        var gourd = BuiltInRegistries.ITEM.getKey(ModItems.produce(CropSpecies.GOURD));
        float demandBefore = market.demand(gourd);
        exchange.setItem(0, prime);
        exchange.setItem(1, plain);
        exchange.setItem(2, new ItemStack(Items.DIRT));
        exchange.sell(level, helper.absolutePos(pos));
        check(helper, exchange.lastSaleItems() == 32, "both gourd stacks must sell, got " + exchange.lastSaleItems());
        check(helper, exchange.getItem(0).isEmpty() && exchange.getItem(1).isEmpty(), "sold stacks leave the crate");
        check(helper, exchange.getItem(2).is(Items.DIRT), "unsellable items stay in the crate");
        int paid = exchange.getItem(ProduceExchangeBlockEntity.SLOT_OUTPUT).getCount();
        check(helper, paid == (int) Math.floor(exchange.lastSaleValue() + 1e-4f) || paid + exchange.credit() >= exchange.lastSaleValue() - 1e-3f,
                "payout must match the sale value, paid " + paid + " of " + exchange.lastSaleValue());
        check(helper, exchange.getItem(ProduceExchangeBlockEntity.SLOT_OUTPUT).is(Items.EMERALD) && paid > 0, "the payout is emeralds");
        check(helper, market.demand(gourd) < demandBefore, "selling must lower demand");
        helper.succeed();
    }

    static void contractDelivery(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.CONTRACT_BOARD.get());
        ContractBoardBlockEntity board = helper.getBlockEntity(pos, ContractBoardBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Contract order = Contract.of(Contract.Kind.PRODUCE, CropSpecies.MAIZE.key(), 4, Grade.B, Optional.empty(), 0f, Optional.empty(), false, 5,
                Optional.empty());
        board.post(0, order);
        board.setItem(ContractBoardBlockEntity.SLOT_DELIVERY, produce(CropSpecies.MAIZE, 4, Grade.C));
        check(helper, !board.deliver(player, 0), "Grade C must not meet a Grade B contract");
        board.setItem(ContractBoardBlockEntity.SLOT_DELIVERY, produce(CropSpecies.MAIZE, 2, Grade.A));
        check(helper, board.deliver(player, 0), "Grade A must count toward a Grade B contract");
        check(helper, board.contracts().get(0).map(Contract::progress).orElse(-1) == 2, "partial delivery is recorded");
        board.setItem(ContractBoardBlockEntity.SLOT_DELIVERY, produce(CropSpecies.MAIZE, 5, Grade.PRIME));
        check(helper, board.deliver(player, 0), "the rest must complete the contract");
        check(helper, board.contracts().get(0).isEmpty(), "a fulfilled contract leaves its slot");
        check(helper, board.getItem(ContractBoardBlockEntity.SLOT_DELIVERY).getCount() == 3, "only the remaining 2 are taken");
        check(helper, emeralds(player) == 5, "the reward is paid, got " + emeralds(player));
        check(helper, board.reputation() == 1, "reputation rises");
        board.setItem(ContractBoardBlockEntity.SLOT_PAYMENT, new ItemStack(Items.EMERALD, 3));
        check(helper, board.buy(player, 1), "a tier 0 offer can be bought");
        check(helper, board.getItem(ContractBoardBlockEntity.SLOT_PAYMENT).getCount() == 3 - ContractBoardBlockEntity.OFFERS.get(1).price(),
                "the price is paid");
        check(helper, !board.buy(player, ContractBoardBlockEntity.OFFERS.size() - 1), "a tier 3 offer stays locked at tier 0");
        helper.succeed();
    }

    static void processingKeepsGrade(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.PROCESSING_STATION.get());
        ProcessingStationBlockEntity station = helper.getBlockEntity(pos, ProcessingStationBlockEntity.class);
        station.setItem(ProcessingStationBlockEntity.SLOT_INPUT, produce(CropSpecies.SOYBEAN, 6, Grade.PRIME));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        int ticks = (int) Math.ceil(200 / CultivarConfig.PROCESSING_SPEED_MULTIPLIER.get()) + 2;
        for (int i = 0; i < ticks; i++) {
            station.serverTick(level, abs, level.getBlockState(abs));
        }
        ItemStack out = station.getItem(ProcessingStationBlockEntity.SLOT_OUTPUT);
        check(helper, out.is(ModItems.SOY_OIL.get()) && out.getCount() == 1, "3 soybean pods press into 1 soy oil, got " + out);
        check(helper, GradeLogic.of(out) == Grade.PRIME, "the oil keeps the Prime grade");
        check(helper, station.getItem(ProcessingStationBlockEntity.SLOT_INPUT).getCount() == 3, "one batch of 3 pods consumed");
        check(helper, station.preview().is(ModItems.SOY_OIL.get()) && station.batch() == 3, "the preview shows the next batch");
        helper.succeed();
    }

    static void rationRecipe(GameTestHelper helper) {
        List<ItemStack> grid = new ArrayList<>();
        grid.add(new ItemStack(ModItems.LINEN_CANVAS.get()));
        grid.add(produce(CropSpecies.MAIZE, 1, Grade.A));
        grid.add(produce(CropSpecies.TUBER, 1, Grade.PRIME));
        grid.add(produce(CropSpecies.GOURD, 1, Grade.A));
        grid.add(produce(CropSpecies.BRASSICA, 1, Grade.A));
        while (grid.size() < 9) {
            grid.add(ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        check(helper, RationRecipe.INSTANCE.matches(input, helper.getLevel()), "canvas plus four foods makes a ration");
        ItemStack pack = RationRecipe.INSTANCE.assemble(input);
        RationData data = pack.get(ModDataComponents.RATION.get());
        check(helper, pack.is(ModItems.RATION_PACK.get()) && data != null, "the result is a filled Ration Pack");
        check(helper, data.species().size() == 4 && data.grade() == Grade.A, "four species, lowest grade A, got " + data);
        check(helper, data.strength() == 3 + Grade.A.ordinal() + 1, "strength counts species and grade, got " + data.strength());
        List<ItemStack> tooFew = new ArrayList<>(List.of(new ItemStack(ModItems.LINEN_CANVAS.get()), produce(CropSpecies.MAIZE, 1, Grade.C),
                produce(CropSpecies.TUBER, 1, Grade.C)));
        while (tooFew.size() < 9) {
            tooFew.add(ItemStack.EMPTY);
        }
        check(helper, !RationRecipe.INSTANCE.matches(CraftingInput.of(3, 3, tooFew), helper.getLevel()), "two foods are not enough");
        helper.succeed();
    }

    static void showBenchJudges(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.SHOW_BENCH.get());
        ShowBenchBlockEntity bench = helper.getBlockEntity(pos, ShowBenchBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        int cost = CultivarConfig.SHOW_BENCH_PAPER_COST.get();
        bench.setItem(ShowBenchBlockEntity.SLOT_PACKET, HarvestLogic.seedStack(CropSpecies.FIBER_FLAX, HARDY, 0, false));
        bench.setItem(ShowBenchBlockEntity.SLOT_PAPER, new ItemStack(Items.PAPER, cost + 1));
        check(helper, bench.judge(player), "a sequenced packet with paper can be judged");
        ItemStack out = bench.getItem(ShowBenchBlockEntity.SLOT_OUTPUT);
        Certificate certificate = out.get(ModDataComponents.CERTIFICATE.get());
        int expected = ShowBenchBlockEntity.score(HARDY, Catalog.get(helper.getLevel()));
        check(helper, certificate != null && certificate.score() == expected, "the packet is certified with its score " + expected);
        check(helper, HARDY.equals(out.get(ModDataComponents.GENOME.get())), "judging does not change the genome");
        check(helper, bench.getItem(ShowBenchBlockEntity.SLOT_PACKET).isEmpty(), "the entry moves to the output");
        check(helper, bench.getItem(ShowBenchBlockEntity.SLOT_PAPER).getCount() == 1, "paper is spent");
        String ribbon = ShowBenchBlockEntity.ribbon(expected);
        var ribbonItem = ShowBenchBlockEntity.ribbonItem(ribbon);
        check(helper, ribbonItem == null ? bench.getItem(ShowBenchBlockEntity.SLOT_RIBBON).isEmpty()
                : bench.getItem(ShowBenchBlockEntity.SLOT_RIBBON).is(ribbonItem), "the ribbon matches the score");
        check(helper, !bench.judge(player), "a full output slot blocks judging");
        helper.succeed();
    }

    static void geneEditorNeedsAtlas(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.GENE_EDITOR.get());
        GeneEditorBlockEntity editor = helper.getBlockEntity(pos, GeneEditorBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ServerLevel level = helper.getLevel();
        AlleleTable table = Catalog.get(level).table();
        int current = HARDY.allele(Gene.YIELD, 0);
        Allele target = null;
        for (Allele allele : table.alleles(Gene.YIELD)) {
            if (allele.code() != current && !allele.mutationOnly()) {
                target = allele;
                break;
            }
        }
        check(helper, target != null, "yield has a second allele");
        editor.setItem(GeneEditorBlockEntity.SLOT_PACKET, HarvestLogic.seedStack(CropSpecies.WHEATLINE, HARDY, 0, false));
        editor.setItem(GeneEditorBlockEntity.SLOT_REAGENT, new ItemStack(ModItems.EDIT_REAGENT.get()));
        editor.button(player, 1);
        editor.button(player, 0);
        for (int i = 0; i < table.alleles(Gene.YIELD).size() && editor.alleleCode() != target.code(); i++) {
            editor.button(player, 3);
        }
        check(helper, editor.gene() == Gene.YIELD.ordinal() && editor.alleleCode() == target.code(), "the target allele is selected");
        check(helper, !editor.button(player, 5), "an allele missing from the atlas cannot be written");
        AlleleAtlas.record(player, Genome.homozygous(target.code(), 1, 4, 4, 4, 0, 2, 0), table);
        check(helper, AlleleAtlas.knows(player, target.id()), "reading a genome records its alleles");
        check(helper, editor.button(player, 5), "a recorded allele can be written");
        BlockPos abs = helper.absolutePos(pos);
        for (int i = 0; i <= CultivarConfig.GENE_EDIT_TICKS.get(); i++) {
            editor.serverTick(level, abs, level.getBlockState(abs));
        }
        ItemStack out = editor.getItem(GeneEditorBlockEntity.SLOT_OUTPUT);
        Genome edited = out.get(ModDataComponents.GENOME.get());
        check(helper, edited != null && edited.carries(Gene.YIELD, target.code()), "the edited packet carries the new allele");
        check(helper, edited.allele(Gene.GROWTH_RATE, 0) == HARDY.allele(Gene.GROWTH_RATE, 0), "other genes are untouched");
        check(helper, editor.getItem(GeneEditorBlockEntity.SLOT_REAGENT).isEmpty(), "one reagent is used");
        check(helper, out.getOrDefault(ModDataComponents.LINEAGE.get(), 0L) > 0, "the edit is recorded in the pedigree");
        helper.succeed();
    }
}
