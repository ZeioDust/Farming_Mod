package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ShowBenchBlock extends MachineBlock {
    public static final MapCodec<ShowBenchBlock> CODEC = simpleCodec(ShowBenchBlock::new);

    public ShowBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ShowBenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.SHOW_BENCH.get();
    }
}
