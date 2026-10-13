package com.cultivar.entity;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.block.GraftUnionBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.HarvestCollectorBlockEntity;
import com.cultivar.genetics.Genome;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModSounds;
import com.cultivar.world.HarvestLogic;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class HarvesterCartEntity extends MachineEntity {
    public static final double SPEED = 0.16;
    public static final int MAX_AGE = 2400;

    private BlockPos home = BlockPos.ZERO;
    private final List<BlockPos> route = new ArrayList<>();
    private int index;
    private final List<ItemStack> cargo = new ArrayList<>();
    private int capacity = HarvestCollectorBlockEntity.SIZE;
    public static final int STACKS_PER_HARVEST = 6;

    public HarvesterCartEntity(EntityType<? extends HarvesterCartEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void assign(BlockPos home, List<BlockPos> route, int capacity) {
        this.capacity = capacity;
        this.home = home.immutable();
        this.route.clear();
        route.forEach(p -> this.route.add(p.immutable()));
        this.index = 0;
        setPos(home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount > MAX_AGE) {
            unload(level);
            return;
        }
        if (index < route.size() && cargo.size() + STACKS_PER_HARVEST > capacity) {
            index = route.size();
        }
        if (index < route.size()) {
            BlockPos next = route.get(index);
            if (moveToward(new Vec3(next.getX() + 0.5, next.getY() + 0.05, next.getZ() + 0.5), SPEED)) {
                harvestAt(level, next);
                index++;
            }
        } else if (moveToward(new Vec3(home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5), SPEED)) {
            unload(level);
        }
    }

    private void harvestAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CultivarCropBlock crop) || CultivarCropBlock.stage(state) != CultivarCropBlock.MAX_STAGE
                || !(level.getBlockEntity(pos) instanceof CropBlockEntity be)) {
            return;
        }
        HarvestLogic.Result result = HarvestLogic.harvest(level, be, state, null);
        result.produce().forEach(this::load);
        List<ItemStack> seeds = new ArrayList<>(result.seeds());
        level.playSound(null, pos, ModSounds.HARVEST_CLATTER.get(), SoundSource.BLOCKS, 0.6f, 1.0f);
        if (crop instanceof GraftUnionBlock) {
            be.regrowAfterHarvest();
            seeds.forEach(this::load);
            return;
        }
        ItemStack replant = seeds.isEmpty() ? ItemStack.EMPTY : seeds.get(0);
        Genome genome = replant.isEmpty() ? null : replant.get(ModDataComponents.GENOME.get());
        boolean bagged = state.getValue(CultivarCropBlock.BAGGED);
        boolean wild = be.isWild() && be.donor() == null;
        level.removeBlock(pos, false);
        if (genome != null && crop.species() != null) {
            long lineage = replant.getOrDefault(ModDataComponents.LINEAGE.get(), 0L);
            level.setBlock(pos, crop.defaultBlockState().setValue(CultivarCropBlock.BAGGED, bagged), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof CropBlockEntity fresh) {
                fresh.plant(genome, lineage, wild);
            }
            replant.shrink(1);
        } else if (bagged) {
            load(new ItemStack(com.cultivar.registry.ModItems.ISOLATION_BAG.get()));
        }
        seeds.forEach(this::load);
        setWorking(!cargo.isEmpty());
    }

    private void load(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (ItemStack held : cargo) {
            if (ItemStack.isSameItemSameComponents(held, stack) && held.getCount() < held.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
                held.grow(moved);
                stack.shrink(moved);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        cargo.add(stack.copy());
    }

    private void unload(ServerLevel level) {
        if (level.getBlockEntity(home) instanceof HarvestCollectorBlockEntity collector) {
            for (ItemStack stack : cargo) {
                ItemStack rest = collector.insert(stack, 0, collector.getContainerSize());
                if (!rest.isEmpty()) {
                    Containers.dropItemStack(level, home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5, rest);
                }
            }
            collector.cartReturned();
        } else {
            cargo.forEach(stack -> Containers.dropItemStack(level, getX(), getY(), getZ(), stack));
        }
        cargo.clear();
        discard();
    }

    @Override
    protected void onSwitchedOff(ServerLevel level) {
        cargo.forEach(stack -> Containers.dropItemStack(level, getX(), getY(), getZ(), stack));
        cargo.clear();
        if (level.getBlockEntity(home) instanceof HarvestCollectorBlockEntity collector) {
            collector.cartReturned();
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        home = input.read("home", BlockPos.CODEC).orElse(BlockPos.ZERO);
        route.clear();
        input.read("route", BlockPos.CODEC.listOf()).ifPresent(route::addAll);
        index = input.getIntOr("index", 0);
        capacity = input.getIntOr("capacity", HarvestCollectorBlockEntity.SIZE);
        cargo.clear();
        input.read("cargo", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(cargo::addAll);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("home", BlockPos.CODEC, home);
        output.store("route", BlockPos.CODEC.listOf(), List.copyOf(route));
        output.putInt("index", index);
        output.putInt("capacity", capacity);
        output.store("cargo", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(cargo));
    }
}
