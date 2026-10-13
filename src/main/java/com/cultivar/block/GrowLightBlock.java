package com.cultivar.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class GrowLightBlock extends Block {
    public static final MapCodec<GrowLightBlock> CODEC = simpleCodec(GrowLightBlock::new);

    public GrowLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GrowLightBlock> codec() {
        return CODEC;
    }
}
