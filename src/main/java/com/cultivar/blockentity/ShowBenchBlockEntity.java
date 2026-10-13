package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.market.GradeLogic;
import com.cultivar.mastery.Certificate;
import com.cultivar.mastery.ShowRecords;
import com.cultivar.menu.ShowBenchMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ShowBenchBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine {
    public static final int SLOT_PACKET = 0;
    public static final int SLOT_PAPER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_RIBBON = 3;
    public static final int SLOTS = 4;
    public static final int BRONZE = 55;
    public static final int SILVER = 70;
    public static final int GOLD = 85;
    public static final int TRUE_BREEDING_BONUS = 8;

    private @Nullable Certificate last;
    private @Nullable Identifier lastSpecies;

    public ShowBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOW_BENCH.get(), pos, state, SLOTS);
    }

    public Optional<Certificate> last() {
        return Optional.ofNullable(last);
    }

    public Optional<Identifier> lastSpecies() {
        return Optional.ofNullable(lastSpecies);
    }

    public static int score(Genome genome, Catalog catalog) {
        float genetic = GradeLogic.geneticScore(Phenotype.resolve(genome, catalog.table()), catalog);
        return Math.min(100, Math.round(genetic * 100) + (genome.isTrueBreeding() ? TRUE_BREEDING_BONUS : 0));
    }

    public static String ribbon(int score) {
        return score >= GOLD ? "gold" : score >= SILVER ? "silver" : score >= BRONZE ? "bronze" : "none";
    }

    public static @Nullable Item ribbonItem(String ribbon) {
        return switch (ribbon) {
            case "gold" -> ModItems.RIBBON_GOLD.get();
            case "silver" -> ModItems.RIBBON_SILVER.get();
            case "bronze" -> ModItems.RIBBON_BRONZE.get();
            default -> null;
        };
    }

    public boolean judge(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        ItemStack packet = items.get(SLOT_PACKET);
        Genome genome = packet.get(ModDataComponents.GENOME.get());
        int paperCost = CultivarConfig.SHOW_BENCH_PAPER_COST.get();
        ItemStack paper = items.get(SLOT_PAPER);
        if (!(packet.getItem() instanceof SeedPacketItem species) || genome == null || !items.get(SLOT_OUTPUT).isEmpty()
                || (paperCost > 0 && (!paper.is(Items.PAPER) || paper.getCount() < paperCost))) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_BENCH_NOT_READY));
            return false;
        }
        Catalog catalog = Catalog.get(serverLevel);
        int score = score(genome, catalog);
        String ribbon = ribbon(score);
        long day = serverLevel.getOverworldClockTime() / 24000L;
        Identifier speciesKey = species.species().key();
        boolean record = ShowRecords.get(serverLevel.getServer()).submit(speciesKey, score, player.getGameProfile().name(), day);
        Item ribbonItem = ribbonItem(ribbon);
        ItemStack ribbonSlot = items.get(SLOT_RIBBON);
        if (ribbonItem != null && !ribbonSlot.isEmpty() && (!ribbonSlot.is(ribbonItem) || ribbonSlot.getCount() >= ribbonSlot.getMaxStackSize())) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_BENCH_RIBBON_FULL));
            return false;
        }
        Certificate certificate = new Certificate(player.getGameProfile().name(), score, day, ribbon, record);
        ItemStack certified = packet.copyWithCount(1);
        certified.set(ModDataComponents.CERTIFICATE.get(), certificate);
        packet.shrink(1);
        if (paperCost > 0) {
            paper.shrink(paperCost);
        }
        items.set(SLOT_OUTPUT, certified);
        if (ribbonItem != null) {
            if (ribbonSlot.isEmpty()) {
                items.set(SLOT_RIBBON, new ItemStack(ribbonItem));
            } else {
                ribbonSlot.grow(1);
            }
        }
        if (record) {
            ItemStack bonus = new ItemStack(Items.EMERALD, Math.max(1, score / 10));
            if (!player.getInventory().add(bonus)) {
                player.drop(bonus, false);
            }
            CultivarTrigger.fire(player, CultivarTrigger.SHOW_RECORD);
        }
        if ("gold".equals(ribbon)) {
            CultivarTrigger.fire(player, CultivarTrigger.GOLD_RIBBON);
        }
        last = certificate;
        lastSpecies = speciesKey;
        serverLevel.playSound(null, getBlockPos(), record ? SoundEvents.PLAYER_LEVELUP : SoundEvents.BOOK_PAGE_TURN,
                SoundSource.BLOCKS, 0.7f, 1.2f);
        markUpdated();
        return true;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_SHOW_BENCH);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ShowBenchMenu(id, inventory, this, getBlockPos());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (last != null) {
            output.store("last", Certificate.CODEC, last);
        }
        if (lastSpecies != null) {
            output.store("last_species", Identifier.CODEC, lastSpecies);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        last = input.read("last", Certificate.CODEC).orElse(null);
        lastSpecies = input.read("last_species", Identifier.CODEC).orElse(null);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_PACKET -> stack.getItem() instanceof SeedPacketItem;
            case SLOT_PAPER -> stack.is(Items.PAPER);
            default -> false;
        };
    }
}
