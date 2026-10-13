package com.cultivar.blockentity;

import com.cultivar.CultivarConfig;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.market.Grade;
import com.cultivar.market.GradeLogic;
import com.cultivar.menu.ComputedData;
import com.cultivar.menu.ProcessingStationMenu;
import com.cultivar.processing.ProcessingRecipe;
import com.cultivar.registry.ModBlockEntities;
import com.cultivar.registry.ModDataComponents;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ProcessingStationBlockEntity extends InventoryBlockEntity implements MenuProvider, TickingMachine, ProgressMachine {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOTS = 2;
    public static final int DATA = 2;

    private float progress;
    private int recipeTicks = 1;
    private ItemStack preview = ItemStack.EMPTY;
    private int batch;
    private final ContainerData data = new ComputedData(DATA, i -> i == 0 ? Math.round(progressFraction() * 1000) : boostTicks() / 20);

    public ProcessingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROCESSING_STATION.get(), pos, state, SLOTS);
    }

    @Override
    public float progressFraction() {
        return Math.min(1f, progress / Math.max(1, recipeTicks));
    }

    private Optional<RecipeHolder<ProcessingRecipe>> recipe(ServerLevel level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return level.recipeAccess().getRecipeFor(ModRecipes.PROCESSING.get(), new SingleRecipeInput(input), level);
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack input = items.get(SLOT_INPUT);
        Optional<RecipeHolder<ProcessingRecipe>> found = recipe(level, input);
        if (found.isEmpty()) {
            if (progress != 0 || !preview.isEmpty()) {
                progress = 0;
                preview = ItemStack.EMPTY;
                batch = 0;
                markUpdated();
            }
            return;
        }
        ProcessingRecipe recipe = found.get().value();
        ItemStack result = recipe.assemble(new SingleRecipeInput(input));
        Grade grade = input.get(ModDataComponents.GRADE.get());
        if (grade != null) {
            GradeLogic.apply(result, grade);
        }
        if (!ItemStack.matches(preview, result) || batch != recipe.count()) {
            preview = result.copy();
            batch = recipe.count();
            markUpdated();
        }
        ItemStack out = items.get(SLOT_OUTPUT);
        boolean fits = out.isEmpty() || (ItemStack.isSameItemSameComponents(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize());
        if (!fits) {
            return;
        }
        recipeTicks = recipe.ticks();
        float before = progress;
        progress += CultivarConfig.PROCESSING_SPEED_MULTIPLIER.get().floatValue();
        if ((int) (progress / 20f) != (int) (before / 20f)) {
            markUpdated();
        }
        if (progress < recipeTicks) {
            return;
        }
        progress = 0;
        input.shrink(recipe.count());
        if (out.isEmpty()) {
            items.set(SLOT_OUTPUT, result);
        } else {
            out.grow(result.getCount());
        }
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.4f, 1.2f);
        Guide.hintNear(level, pos, 8, Hint.PROCESSED);
        markUpdated();
    }

    public ItemStack preview() {
        return preview;
    }

    public int batch() {
        return batch;
    }

    @Override
    public Component getDisplayName() {
        return Texts.tr(Texts.SCREEN_PROCESSING);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ProcessingStationMenu(id, inventory, this, data, getBlockPos());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("progress", progress);
        output.putInt("recipe_ticks", recipeTicks);
        if (!preview.isEmpty()) {
            output.store("preview", ItemStack.CODEC, preview);
        }
        output.putInt("batch", batch);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getFloatOr("progress", 0f);
        recipeTicks = Math.max(1, input.getIntOr("recipe_ticks", 1));
        preview = input.read("preview", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        batch = input.getIntOr("batch", 0);
    }

    @Override
    protected boolean acceptsInSlot(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && level instanceof ServerLevel serverLevel && recipe(serverLevel, stack.copyWithCount(64)).isPresent();
    }
}
