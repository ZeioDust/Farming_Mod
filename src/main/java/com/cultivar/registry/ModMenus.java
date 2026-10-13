package com.cultivar.registry;

import com.cultivar.Cultivar;
import com.cultivar.menu.ClimateControllerMenu;
import com.cultivar.menu.CompostBinMenu;
import com.cultivar.menu.ContractBoardMenu;
import com.cultivar.menu.GeneEditorMenu;
import com.cultivar.menu.GenomeCardMenu;
import com.cultivar.menu.GenomeLabMenu;
import com.cultivar.menu.MutagenChamberMenu;
import com.cultivar.menu.ProcessingStationMenu;
import com.cultivar.menu.ProduceExchangeMenu;
import com.cultivar.menu.SeedBankMenu;
import com.cultivar.menu.ShowBenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cultivar.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GenomeLabMenu>> GENOME_LAB = MENUS.register("genome_lab_menu",
            () -> IMenuTypeExtension.create(GenomeLabMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SeedBankMenu>> SEED_BANK = MENUS.register("seed_bank_menu",
            () -> IMenuTypeExtension.create(SeedBankMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MutagenChamberMenu>> MUTAGEN_CHAMBER = MENUS.register("mutagen_chamber_menu",
            () -> IMenuTypeExtension.create(MutagenChamberMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ClimateControllerMenu>> CLIMATE_CONTROLLER = MENUS.register("climate_controller_menu",
            () -> IMenuTypeExtension.create(ClimateControllerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CompostBinMenu>> COMPOST_BIN = MENUS.register("compost_bin_menu",
            () -> IMenuTypeExtension.create(CompostBinMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GenomeCardMenu>> GENOME_CARD = MENUS.register("genome_card_menu",
            () -> IMenuTypeExtension.create(GenomeCardMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ProduceExchangeMenu>> PRODUCE_EXCHANGE = MENUS.register("produce_exchange_menu",
            () -> IMenuTypeExtension.create(ProduceExchangeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ContractBoardMenu>> CONTRACT_BOARD = MENUS.register("contract_board_menu",
            () -> IMenuTypeExtension.create(ContractBoardMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingStationMenu>> PROCESSING_STATION = MENUS.register(
            "processing_station_menu", () -> IMenuTypeExtension.create(ProcessingStationMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GeneEditorMenu>> GENE_EDITOR = MENUS.register("gene_editor_menu",
            () -> IMenuTypeExtension.create(GeneEditorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ShowBenchMenu>> SHOW_BENCH = MENUS.register("show_bench_menu",
            () -> IMenuTypeExtension.create(ShowBenchMenu::new));

    private ModMenus() {
    }
}
