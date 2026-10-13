package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ProcessingStationBlock extends MachineBlock {
    public static final MapCodec<ProcessingStationBlock> CODEC = simpleCodec(ProcessingStationBlock::new);

    public ProcessingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ProcessingStationBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.PROCESSING_STATION.get();
    }
}
