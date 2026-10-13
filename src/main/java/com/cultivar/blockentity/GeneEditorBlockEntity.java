package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.data.Catalog;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.mastery.AlleleAtlas;
import com.cultivar.menu.ComputedData;
import com.cultivar.menu.GeneEditorMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.util.Texts;
import com.cultivar.world.PedigreeData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class GeneEditorBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine, ProgressMachine {
    public static final int SLOT_PACKET = 0;
    public static final int SLOT_REAGENT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOTS = 3;
    public static final int DATA = 5;

    private int gene;
    private int copy;
    private int alleleCode;
    private int progress;
    private boolean running;
    private @Nullable UUID operator;
    private final ContainerData data = new ComputedData(DATA, i -> switch (i) {
        case 0 -> gene;
        case 1 -> copy;
        case 2 -> alleleCode;
        case 3 -> Math.round(progressFraction() * 1000);
        default -> running ? 1 : 0;
    });

    public GeneEditorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENE_EDITOR.get(), pos, state, SLOTS);
    }

    @Override
    public float progressFraction() {
        return running ? Math.min(1f, progress / (float) Math.max(1, CultivarConfig.GENE_EDIT_TICKS.get())) : 0f;
    }

    public int gene() {
        return gene;
    }

    public int copy() {
        return copy;
    }

    public int alleleCode() {
        return alleleCode;
    }

    public boolean button(ServerPlayer player, int id) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        AlleleTable table = Catalog.get(serverLevel).table();
        if (running && id != 5) {
            return false;
        }
        switch (id) {
            case 0 -> {
                gene = (gene + Gene.COUNT - 1) % Gene.COUNT;
                alleleCode = firstCode(table);
            }
            case 1 -> {
                gene = (gene + 1) % Gene.COUNT;
                alleleCode = firstCode(table);
            }
            case 2, 3 -> alleleCode = stepCode(table, id == 3 ? 1 : -1);
            case 4 -> copy = 1 - copy;
            case 5 -> {
                return start(player, table);
            }
            default -> {
                return false;
            }
        }
        markUpdated();
        return true;
    }

    private int firstCode(AlleleTable table) {
        List<Allele> alleles = table.alleles(Gene.VALUES[gene]);
        return alleles.isEmpty() ? 0 : alleles.getFirst().code();
    }

    private int stepCode(AlleleTable table, int dir) {
        List<Allele> alleles = table.alleles(Gene.VALUES[gene]);
        if (alleles.isEmpty()) {
            return 0;
        }
        int index = 0;
        for (int i = 0; i < alleles.size(); i++) {
            if (alleles.get(i).code() == alleleCode) {
                index = i;
            }
        }
        return alleles.get((index + dir + alleles.size()) % alleles.size()).code();
    }

    private boolean start(ServerPlayer player, AlleleTable table) {
        ItemStack packet = items.get(SLOT_PACKET);
        Gene target = Gene.VALUES[gene];
        Genome genome = packet.get(ModDataComponents.GENOME.get());
        if (!(packet.getItem() instanceof SeedPacketItem) || genome == null || !items.get(SLOT_REAGENT).is(ModItems.EDIT_REAGENT.get())
                || !items.get(SLOT_OUTPUT).isEmpty() || !table.has(target, alleleCode)) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_EDITOR_NOT_READY));
            return false;
        }
        Allele allele = table.get(target, alleleCode);
        if (!AlleleAtlas.knows(player, allele.id())) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_EDITOR_UNKNOWN_ALLELE));
            return false;
        }
        if (genome.allele(target, copy) == alleleCode) {
            player.sendOverlayMessage(Texts.tr(Texts.MSG_EDITOR_SAME));
            return false;
        }
        running = true;
        progress = 0;
        operator = player.getUUID();
        markUpdated();
        return true;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!running) {
            return;
        }
        ItemStack packet = items.get(SLOT_PACKET);
        Genome genome = packet.get(ModDataComponents.GENOME.get());
        if (!(packet.getItem() instanceof SeedPacketItem species) || genome == null || !items.get(SLOT_REAGENT).is(ModItems.EDIT_REAGENT.get())
                || !items.get(SLOT_OUTPUT).isEmpty()) {
            running = false;
            progress = 0;
            markUpdated();
            return;
        }
        progress++;
        if (progress % 20 == 0) {
            markUpdated();
        }
        if (progress < CultivarConfig.GENE_EDIT_TICKS.get()) {
            return;
        }
        Gene target = Gene.VALUES[gene];
        int other = genome.allele(target, 1 - copy);
        Genome edited = genome.withGene(target, alleleCode, other);
        long parent = packet.getOrDefault(ModDataComponents.LINEAGE.get(), 0L);
        long lineage = PedigreeData.get(level.getServer()).create(species.species().key(), parent, parent, PedigreeData.Kind.EDITED,
                level.getGameTime(), edited.toHex());
        ItemStack result = new ItemStack(species);
        result.set(ModDataComponents.GENOME.get(), edited);
        result.set(ModDataComponents.LINEAGE.get(), lineage);
        packet.shrink(1);
        items.get(SLOT_REAGENT).shrink(1);
        items.set(SLOT_OUTPUT, result);
        running = false;
        progress = 0;
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8f, 1.4f);
        if (operator != null && level.getServer().getPlayerList().getPlayer(operator) instanceof ServerPlayer player) {
            CultivarTrigger.fire(player, CultivarTrigger.GENE_EDIT);
            AlleleAtlas.record(player, edited, Catalog.get(level).table());
        }
        markUpdated();
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_GENE_EDITOR);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneEditorMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("gene", gene);
        output.putInt("copy", copy);
        output.putInt("allele", alleleCode);
        output.putInt("progress", progress);
        output.putBoolean("running", running);
        if (operator != null) {
            output.store("operator", net.minecraft.core.UUIDUtil.CODEC, operator);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        gene = Math.max(0, Math.min(Gene.COUNT - 1, input.getIntOr("gene", 0)));
        copy = input.getIntOr("copy", 0) == 1 ? 1 : 0;
        alleleCode = input.getIntOr("allele", 0);
        progress = input.getIntOr("progress", 0);
        running = input.getBooleanOr("running", false);
        operator = input.read("operator", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_PACKET -> stack.getItem() instanceof SeedPacketItem;
            case SLOT_REAGENT -> stack.is(ModItems.EDIT_REAGENT.get());
            default -> false;
        };
    }
}
