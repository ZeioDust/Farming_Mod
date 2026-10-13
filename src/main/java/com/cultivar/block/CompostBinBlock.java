package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CompostBinBlock extends MachineBlock {
    public static final MapCodec<CompostBinBlock> CODEC = simpleCodec(CompostBinBlock::new);

    public CompostBinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CompostBinBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.COMPOST_BIN.get();
    }
}
