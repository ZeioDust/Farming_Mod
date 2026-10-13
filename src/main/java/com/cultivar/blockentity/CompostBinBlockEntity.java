package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.menu.CompostBinMenu;
import com.cultivar.recipe.CompostingRecipe;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModItems;
import com.cultivar.registry.ModRecipes;
import com.cultivar.util.Texts;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.cultivar.menu.ComputedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CompostBinBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine, ProgressMachine {
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOTS = 4;
    public static final int DATA = 2;
    public static final int FULL_CHARGE = 100;

    private int charge;
    private int progress;
    private final ContainerData data = new ComputedData(DATA, index -> index == 0 ? charge : Math.round(progressFraction() * 1000));

    public CompostBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPOST_BIN.get(), pos, state, SLOTS);
    }

    public int charge() {
        return charge;
    }

    @Override
    public float progressFraction() {
        return Math.min(1f, progress / (float) Math.max(1, CultivarConfig.COMPOST_TICKS.get()));
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (charge < FULL_CHARGE && level.getGameTime() % 10 == 0) {
            for (int i = 0; i < SLOT_OUTPUT; i++) {
                ItemStack stack = items.get(i);
                if (stack.isEmpty()) {
                    continue;
                }
                Optional<RecipeHolder<CompostingRecipe>> recipe = level.recipeAccess().getRecipeFor(ModRecipes.COMPOSTING.get(),
                        new SingleRecipeInput(stack), level);
                if (recipe.isPresent()) {
                    charge = Math.min(FULL_CHARGE, charge + recipe.get().value().charge());
                    stack.shrink(1);
                    level.playSound(null, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 0.6f, 1.0f);
                    markUpdated();
                    break;
                }
            }
        }
        if (charge < FULL_CHARGE) {
            return;
        }
        ItemStack output = items.get(SLOT_OUTPUT);
        if (!output.isEmpty() && (!output.is(ModItems.COMPOST.get()) || output.getCount() >= output.getMaxStackSize())) {
            return;
        }
        progress++;
        if (progress % 40 == 0) {
            markUpdated();
        }
        if (progress >= CultivarConfig.COMPOST_TICKS.get()) {
            progress = 0;
            charge -= FULL_CHARGE;
            if (output.isEmpty()) {
                items.set(SLOT_OUTPUT, new ItemStack(ModItems.COMPOST.get()));
            } else {
                output.grow(1);
            }
            level.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 0.8f, 1.0f);
            markUpdated();
        }
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_COMPOST);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CompostBinMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("charge", charge);
        output.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charge = input.getIntOr("charge", 0);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return serverLevel.recipeAccess().getRecipeFor(ModRecipes.COMPOSTING.get(), new SingleRecipeInput(stack), serverLevel).isPresent();
    }
}
