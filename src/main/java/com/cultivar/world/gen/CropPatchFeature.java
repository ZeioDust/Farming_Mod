package com.cultivar.world.gen;

import com.cultivar.block.CultivarCropBlock;
import com.cultivar.data.CropSpecies;
import com.cultivar.registry.ModBlocks;
import com.cultivar.registry.ModTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CropPatchFeature extends Feature<CropPatchFeature.Config> {
    public record Config(int tries, int spread, int minStage) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("tries").forGetter(Config::tries),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("spread").forGetter(Config::spread),
                ExtraCodecs.intRange(0, CultivarCropBlock.MAX_STAGE).fieldOf("min_stage").forGetter(Config::minStage)
        ).apply(i, Config::new));
    }

    public CropPatchFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        CropSpecies species = CropSpecies.VALUES[random.nextInt(CropSpecies.VALUES.length)];
        BlockPos origin = context.origin();
        int placed = 0;
        for (int i = 0; i < config.tries(); i++) {
            int x = origin.getX() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int z = origin.getZ() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).is(ModTags.Blocks.CROP_SOIL)) {
                continue;
            }
            int stage = config.minStage() + random.nextInt(CultivarCropBlock.MAX_STAGE - config.minStage() + 1);
            BlockState crop = ModBlocks.crop(species).defaultBlockState().setValue(CultivarCropBlock.STAGE, stage);
            level.setBlock(pos, crop, Block.UPDATE_CLIENTS);
            placed++;
        }
        return placed > 0;
    }
}
