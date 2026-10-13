package com.cultivar.compat.jade;

import com.cultivar.CultivarConfig;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.BurnPanBlockEntity;
import com.cultivar.blockentity.ClimateControllerBlockEntity;
import com.cultivar.blockentity.CompostBinBlockEntity;
import com.cultivar.blockentity.ContractBoardBlockEntity;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.GeneEditorBlockEntity;
import com.cultivar.blockentity.InventoryBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.blockentity.MutagenChamberBlockEntity;
import com.cultivar.blockentity.NutrientSpreaderBlockEntity;
import com.cultivar.blockentity.PollinationUnitBlockEntity;
import com.cultivar.blockentity.ProcessingStationBlockEntity;
import com.cultivar.blockentity.ProduceExchangeBlockEntity;
import com.cultivar.blockentity.ProgressMachine;
import com.cultivar.blockentity.SeedBankBlockEntity;
import com.cultivar.blockentity.ShowBenchBlockEntity;
import com.cultivar.blockentity.ThermalShockUnitBlockEntity;
import com.cultivar.genetics.Gene;
import com.cultivar.market.GradeLogic;
import com.cultivar.market.MarketData;
import com.cultivar.market.MarketValues;
import com.cultivar.registry.ModItems;
import com.cultivar.util.GeneText;
import com.cultivar.util.Texts;
import com.cultivar.world.GrowthRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record BlockInfo(List<Component> lines, float progress) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInfo> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(16)), BlockInfo::lines,
            ByteBufCodecs.FLOAT, BlockInfo::progress,
            BlockInfo::new);

    public static @Nullable BlockInfo of(BlockEntity entity, BlockState state, Player player) {
        if (!(entity.getLevel() instanceof ServerLevel level)) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        float progress = entity instanceof ProgressMachine machine ? machine.progressFraction() : -1f;
        switch (entity) {
            case CropBlockEntity crop -> {
                crop(lines, crop, state, player);
                int stage = state.getBlock() instanceof CultivarCropBlock ? CultivarCropBlock.stage(state) : 0;
                int ticksPerStage = Math.max(20, com.cultivar.data.Catalog.get(level).species(crop.speciesKey()).ticksPerStage());
                float withinStage = stage < CultivarCropBlock.MAX_STAGE ? Math.min(1f, crop.progress() / ticksPerStage) : 0f;
                progress = Math.min(1f, (stage + withinStage) / CultivarCropBlock.MAX_STAGE);
            }
            case ManagedSoilBlockEntity soil -> soil(lines, soil);
            case ProduceExchangeBlockEntity exchange -> exchange(lines, exchange, level);
            case ContractBoardBlockEntity board -> {
                lines.add(Texts.tr(Texts.UI_CB_REPUTATION, board.reputation(), board.tier() + 1));
                long open = board.contracts().stream().filter(java.util.Optional::isPresent).count();
                lines.add(Texts.tr(Texts.JADE_ORDERS, open));
            }
            case ProcessingStationBlockEntity station -> {
                ItemStack preview = station.preview();
                if (!preview.isEmpty()) {
                    lines.add(Texts.tr(Texts.UI_PS_MAKES, station.batch(), station.getItem(ProcessingStationBlockEntity.SLOT_INPUT).getHoverName(),
                            preview.getCount(), preview.getHoverName()));
                    lines.add(Texts.tr(Texts.UI_PS_GRADE_KEPT, Texts.tr(GradeLogic.of(preview).translationKey())));
                }
            }
            case MutagenChamberBlockEntity chamber -> lines.add(Texts.tr(Texts.JADE_VIAL, chamber.vialUses()));
            case ThermalShockUnitBlockEntity unit -> lines.add(Texts.tr(Texts.JADE_COOLANT, unit.coolant()));
            case CompostBinBlockEntity bin -> lines.add(Texts.tr(Texts.JADE_CHARGE, bin.charge(), CompostBinBlockEntity.FULL_CHARGE));
            case SeedBankBlockEntity bank -> lines.add(Texts.tr(Texts.JADE_LINES, bank.lines().size(), bank.capacity()));
            case PollinationUnitBlockEntity unit -> lines.add(unit.isActive() ? Texts.tr(Texts.JADE_UNIT_ACTIVE, unit.fuel() / 20)
                    : Texts.tr(Texts.JADE_UNIT_IDLE));
            case BurnPanBlockEntity pan -> lines.add(Texts.tr(Texts.JADE_BURNS, pan.burnsLeft()));
            case NutrientSpreaderBlockEntity spreader -> {
                ItemStack loaded = spreader.loaded();
                lines.add(loaded.isEmpty() ? Texts.tr(Texts.JADE_EMPTY) : Texts.tr(Texts.JADE_LOADED, loaded.getCount(), loaded.getHoverName()));
            }
            case ClimateControllerBlockEntity climate -> lines.add(climate.isSealed()
                    ? Texts.tr(Texts.JADE_SEALED, climate.volume(), degrees(climate.temperature()), degrees(climate.setpoint()))
                    : Texts.tr(Texts.READOUT_UNSEALED));
            case GeneEditorBlockEntity editor -> {
                if (editor.progressFraction() > 0) {
                    lines.add(Texts.tr(Texts.JADE_EDITING, GeneText.gene(Gene.VALUES[Math.floorMod(editor.gene(), Gene.COUNT)])));
                }
            }
            case ShowBenchBlockEntity bench -> bench.last().ifPresent(cert -> lines.add(Texts.tr(Texts.JADE_LAST_CERT,
                    bench.lastSpecies().map(GeneText::species).orElse(Component.literal("?")), cert.score())));
            default -> {
            }
        }
        if (entity instanceof InventoryBlockEntity inventory && inventory.boostTicks() > 0) {
            lines.add(Texts.tr(Texts.UI_PS_OILED, inventory.boostTicks() / 20));
        }
        if (lines.isEmpty() && progress < 0) {
            return null;
        }
        return new BlockInfo(List.copyOf(lines), progress);
    }

    private static void crop(List<Component> lines, CropBlockEntity crop, BlockState state, Player player) {
        int stage = state.getBlock() instanceof CultivarCropBlock ? CultivarCropBlock.stage(state) : 0;
        lines.add(Texts.tr(Texts.JADE_STAGE, stage + 1, CultivarCropBlock.MAX_STAGE + 1, Texts.stage(stage)));
        List<Component> status = new ArrayList<>();
        if (crop.isPollinated()) {
            status.add(Texts.tr(Texts.UI_HUD_POLLINATED));
        }
        if (crop.isBagged()) {
            status.add(Texts.tr(Texts.UI_HUD_BAGGED));
        }
        if (crop.isGrafted()) {
            status.add(Texts.tr(Texts.UI_HUD_GRAFTED));
        }
        if (crop.isWild()) {
            status.add(Texts.tr(Texts.UI_BADGE_WILD));
        }
        if (!status.isEmpty()) {
            lines.add(Component.empty().append(join(status)));
        }
        if (crop.isBlighted()) {
            Component strain = crop.strain() != null ? GeneText.strain(crop.strain()) : Texts.tr(Texts.UNKNOWN);
            lines.add(Texts.tr(Texts.UI_HUD_BLIGHTED, strain).withStyle(net.minecraft.ChatFormatting.RED));
        }
        GrowthRules.Limit limit = crop.lastLimit();
        if (limit != GrowthRules.Limit.NONE && stage < CultivarCropBlock.MAX_STAGE) {
            lines.add(Texts.tr(Texts.JADE_LIMITED, Texts.tr(Texts.JADE_LIMIT_PREFIX + limit.name().toLowerCase(Locale.ROOT)))
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        if (crop.stress() > 0) {
            lines.add(Texts.tr(Texts.JADE_STRESS, crop.stress()));
        }
        if (crop.genome() != null && !player.getMainHandItem().is(ModItems.HAND_LENS.get()) && !player.getOffhandItem().is(ModItems.HAND_LENS.get())) {
            lines.add(Texts.tr(Texts.JADE_LENS_HINT).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
    }

    private static Component join(List<Component> parts) {
        var out = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                out.append(Component.literal(" · "));
            }
            out.append(parts.get(i));
        }
        return out;
    }

    private static void soil(List<Component> lines, ManagedSoilBlockEntity soil) {
        lines.add(Texts.tr(Texts.JADE_MOISTURE, soil.moisture()));
        if (soil.isExhausted()) {
            lines.add(Texts.tr(Texts.JADE_SOIL_EXHAUSTED).withStyle(net.minecraft.ChatFormatting.RED));
        } else if (soil.lowestNutrient() < (CultivarConfig.isLoaded() ? CultivarConfig.NUTRIENT_FLOOR.get() : 10) * 2) {
            lines.add(Texts.tr(Texts.JADE_SOIL_LOW).withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }

    private static void exchange(List<Component> lines, ProduceExchangeBlockEntity exchange, ServerLevel level) {
        MarketData market = MarketData.get(level.getServer());
        int items = 0;
        float worth = 0;
        for (int i = 0; i < ProduceExchangeBlockEntity.CRATE_SLOTS; i++) {
            ItemStack stack = exchange.getItem(i);
            if (!stack.isEmpty() && MarketValues.base(stack) > 0) {
                items += stack.getCount();
                worth += market.unitPrice(stack) * stack.getCount();
            }
        }
        lines.add(items > 0 ? Texts.tr(Texts.JADE_CRATE, items, String.format(Locale.ROOT, "%.2f", worth)) : Texts.tr(Texts.JADE_CRATE_EMPTY));
    }

    private static String degrees(float temperature) {
        return String.format(Locale.ROOT, "%.2f", temperature);
    }
}
