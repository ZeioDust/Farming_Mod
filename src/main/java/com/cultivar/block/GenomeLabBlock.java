package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GenomeLabBlock extends MachineBlock {
    public static final MapCodec<GenomeLabBlock> CODEC = simpleCodec(GenomeLabBlock::new);

    public GenomeLabBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GenomeLabBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.GENOME_LAB.get();
    }
}
