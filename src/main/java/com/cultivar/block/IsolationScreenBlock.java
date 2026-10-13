package com.cultivar.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class IsolationScreenBlock extends Block {
    public static final MapCodec<IsolationScreenBlock> CODEC = simpleCodec(IsolationScreenBlock::new);

    public IsolationScreenBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<IsolationScreenBlock> codec() {
        return CODEC;
    }
}
