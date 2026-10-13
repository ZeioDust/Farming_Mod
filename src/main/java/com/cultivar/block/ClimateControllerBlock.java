package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClimateControllerBlock extends MachineBlock {
    public static final MapCodec<ClimateControllerBlock> CODEC = simpleCodec(ClimateControllerBlock::new);

    public ClimateControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ClimateControllerBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.CLIMATE_CONTROLLER.get();
    }
}
