package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.menu.MutagenChamberMenu;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModParticles;
import com.cultivar.registry.ModSounds;
import com.cultivar.util.Texts;
import com.cultivar.world.InducedMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.cultivar.menu.ComputedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MutagenChamberBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine, ProgressMachine {
    public static final int FIRST_INPUT = 0;
    public static final int SLOT_VIAL = 4;
    public static final int FIRST_OUTPUT = 5;
    public static final int SLOTS = 9;
    public static final int DATA = 2;
    public static final int USES_PER_VIAL = 8;
    public static final int TICKS_PER_SEED = 100;

    private int progress;
    private int vialUses;
    private final ContainerData data = new ComputedData(DATA, index -> index == 0 ? Math.round(progressFraction() * 1000) : vialUses);

    public MutagenChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MUTAGEN_CHAMBER.get(), pos, state, SLOTS);
    }

    public static boolean isSeed(ItemStack stack) {
        return InducedMutation.isTreatable(stack);
    }

    public int vialUses() {
        return vialUses;
    }

    @Override
    public float progressFraction() {
        return progress / (float) TICKS_PER_SEED;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        int input = firstInput();
        if (input < 0 || (vialUses <= 0 && !items.get(SLOT_VIAL).is(ModItems.MUTAGEN_VIAL.get())) || !hasOutputRoom()) {
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
        if (progress < TICKS_PER_SEED) {
            return;
        }
        progress = 0;
        if (vialUses <= 0) {
            items.get(SLOT_VIAL).shrink(1);
            vialUses = USES_PER_VIAL;
        }
        vialUses--;
        ItemStack seed = items.get(input);
        InducedMutation.Result result = InducedMutation.treat(level, seed,
                CultivarConfig.MUTAGEN_SUCCESS_CHANCE.get(), CultivarConfig.MUTAGEN_DESTROY_CHANCE.get());
        seed.shrink(1);
        if (!result.stack().isEmpty()) {
            insert(result.stack(), FIRST_OUTPUT, SLOTS);
        }
        level.playSound(null, pos, ModSounds.MUTAGEN_HISS.get(), SoundSource.BLOCKS, 0.7f,
                result.outcome() == InducedMutation.Outcome.DESTROYED ? 0.6f : 1.0f);
        level.sendParticles(ModParticles.MIST_WISP.get(), pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 6, 0.2, 0.05, 0.2, 0.01);
        if (result.outcome() == InducedMutation.Outcome.MUTATED) {
            level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, new net.minecraft.world.phys.AABB(pos).inflate(8))
                    .forEach(p -> {
                        if (com.cultivar.world.HarvestLogic.carriesFlag(result.stack().get(com.cultivar.registry.ModDataComponents.GENOME.get()),
                                com.cultivar.data.Catalog.get(level).table(), true)) {
                            CultivarTrigger.fire(p, CultivarTrigger.MUTANT);
                        }
                    });
        }
        markUpdated();
    }

    private int firstInput() {
        for (int i = FIRST_INPUT; i < SLOT_VIAL; i++) {
            if (isSeed(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasOutputRoom() {
        for (int i = FIRST_OUTPUT; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_MUTAGEN);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MutagenChamberMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
        output.putInt("vial_uses", vialUses);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("progress", 0);
        vialUses = input.getIntOr("vial_uses", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        if (slot < SLOT_VIAL) {
            return isSeed(stack);
        }
        return slot == SLOT_VIAL && stack.is(ModItems.MUTAGEN_VIAL.get());
    }
}
