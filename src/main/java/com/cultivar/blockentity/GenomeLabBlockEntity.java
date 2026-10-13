package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.component.CardData;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.item.Planting;
import com.cultivar.item.SeedPacketItem;
import com.cultivar.item.WildSeedItem;
import com.cultivar.menu.GenomeLabMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.cultivar.menu.ComputedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class GenomeLabBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine, ProgressMachine {
    public static final int SLOT_SAMPLE = 0;
    public static final int SLOT_PAPER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOTS = 3;
    public static final int DATA = 1;

    private int progress;
    private final ContainerData data = new ComputedData(DATA, index -> Math.round(progressFraction() * 1000));

    public GenomeLabBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENOME_LAB.get(), pos, state, SLOTS);
    }

    public static boolean isSample(ItemStack stack) {
        return stack.getItem() instanceof SeedPacketItem || stack.is(ModItems.WILD_SEED.get())
                || (stack.is(ModItems.BLIGHT_SAMPLE.get()) && stack.has(ModDataComponents.STRAIN.get()));
    }

    @Override
    public float progressFraction() {
        return Math.min(1f, progress / (float) Math.max(1, CultivarConfig.LAB_ANALYZE_TICKS.get()));
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack sample = items.get(SLOT_SAMPLE);
        boolean ready = isSample(sample) && items.get(SLOT_PAPER).is(Items.PAPER) && items.get(SLOT_OUTPUT).isEmpty();
        if (!ready) {
            if (progress != 0) {
                progress = 0;
                markUpdated();
            }
            return;
        }
        progress++;
        if (progress % 20 == 0) {
            markUpdated();
        }
        if (progress < CultivarConfig.LAB_ANALYZE_TICKS.get()) {
            return;
        }
        progress = 0;
        CardData card = analyse(level, sample);
        if (card != null) {
            ItemStack output = new ItemStack(ModItems.GENOME_CARD.get());
            output.set(ModDataComponents.CARD.get(), card);
            if (sample.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                output.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, sample.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
            }
            items.set(SLOT_OUTPUT, output);
            items.get(SLOT_PAPER).shrink(1);
            level.playSound(null, pos, ModSounds.LAB_ANALYZE.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        markUpdated();
    }

    private static @Nullable CardData analyse(ServerLevel level, ItemStack sample) {
        if (sample.is(ModItems.BLIGHT_SAMPLE.get())) {
            Identifier strain = sample.get(ModDataComponents.STRAIN.get());
            return strain != null ? CardData.ofStrain(strain) : null;
        }
        CropSpecies species = sample.getItem() instanceof SeedPacketItem packet ? packet.species() : WildSeedItem.speciesOf(sample);
        Planting.assignNaturalGenome(sample, species, level);
        Genome genome = sample.get(ModDataComponents.GENOME.get());
        if (genome == null) {
            return null;
        }
        return CardData.ofGenome(species.key(), genome, sample.getOrDefault(ModDataComponents.LINEAGE.get(), 0L));
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_GENOME_LAB);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GenomeLabMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot == SLOT_SAMPLE ? isSample(stack) : slot == SLOT_PAPER && stack.is(Items.PAPER);
    }
}
