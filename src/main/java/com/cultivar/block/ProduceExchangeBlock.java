package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ProduceExchangeBlock extends MachineBlock {
    public static final MapCodec<ProduceExchangeBlock> CODEC = simpleCodec(ProduceExchangeBlock::new);

    public ProduceExchangeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ProduceExchangeBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.PRODUCE_EXCHANGE.get();
    }
}
