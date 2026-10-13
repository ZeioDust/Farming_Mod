package com.cultivar.item;

import com.cultivar.advancement.CultivarTrigger;
import com.cultivar.block.CultivarCropBlock;
import com.cultivar.blockentity.CropBlockEntity;
import com.cultivar.blockentity.ManagedSoilBlockEntity;
import com.cultivar.data.Catalog;
import com.cultivar.data.CropSpecies;
import com.cultivar.genetics.Genome;
import com.cultivar.guide.Guide;
import com.cultivar.guide.Hint;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModDataComponents;
import com.cultivar.registry.ModTags;
import com.cultivar.util.Rng;
import com.cultivar.world.PedigreeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class Planting {
    private Planting() {
    }

    public static InteractionResult plant(UseOnContext context, CropSpecies species, boolean wild) {
        Level level = context.getLevel();
        BlockPos soilPos = context.getClickedPos();
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockState soilState = level.getBlockState(soilPos);
        BlockPos cropPos = soilPos.above();
        if (!soilState.is(ModTags.Blocks.PLANTABLE_SOIL) || !level.getBlockState(cropPos).isAir()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        if (soilState.getBlock() instanceof FarmlandBlock) {
            int moisture = soilState.getValue(FarmlandBlock.MOISTURE);
            serverLevel.setBlock(soilPos, ModBlocks.MANAGED_SOIL.get().defaultBlockState().setValue(FarmlandBlock.MOISTURE, moisture), Block.UPDATE_ALL);
        }
        Genome genome = stack.get(ModDataComponents.GENOME.get());
        long lineage = stack.getOrDefault(ModDataComponents.LINEAGE.get(), 0L);
        if (genome == null) {
            genome = Catalog.get(serverLevel).naturalGenome(species.key(), Rng.of(serverLevel.getRandom()));
            lineage = PedigreeData.get(serverLevel.getServer()).create(species.key(), 0, 0, PedigreeData.Kind.NATURAL,
                    serverLevel.getGameTime(), genome.toHex());
        }
        serverLevel.setBlock(cropPos, ModBlocks.crop(species).defaultBlockState(), Block.UPDATE_ALL);
        if (serverLevel.getBlockEntity(cropPos) instanceof CropBlockEntity crop) {
            crop.plant(genome, lineage, wild);
        }
        if (context.getPlayer() instanceof ServerPlayer planter) {
            Guide.hint(planter, Hint.PLANTED);
        }
        if (context.getPlayer() instanceof ServerPlayer player && serverLevel.getBlockEntity(soilPos) instanceof ManagedSoilBlockEntity soil
                && soil.lastSpecies() != null && !soil.lastSpecies().equals(species.key())) {
            boolean fixer = Catalog.get(serverLevel).nitrogenFixer(species.key()).isPresent();
            if (fixer != soil.lastWasFixer()) {
                CultivarTrigger.fire(player, CultivarTrigger.ROTATION);
            }
        }
        serverLevel.playSound(null, cropPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0f, 1.0f);
        stack.consume(1, context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    public static void assignNaturalGenome(ItemStack stack, CropSpecies species, ServerLevel level) {
        if (stack.has(ModDataComponents.GENOME.get())) {
            return;
        }
        Genome genome = Catalog.get(level).naturalGenome(species.key(), Rng.of(level.getRandom()));
        long lineage = PedigreeData.get(level.getServer()).create(species.key(), 0, 0, PedigreeData.Kind.NATURAL, level.getGameTime(), genome.toHex());
        stack.set(ModDataComponents.GENOME.get(), genome);
        stack.set(ModDataComponents.LINEAGE.get(), lineage);
    }

    public static boolean isMature(BlockState state) {
        return state.getBlock() instanceof CultivarCropBlock && CultivarCropBlock.stage(state) == CultivarCropBlock.MAX_STAGE;
    }
}
