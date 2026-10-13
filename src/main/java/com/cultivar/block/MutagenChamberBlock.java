package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MutagenChamberBlock extends MachineBlock {
    public static final MapCodec<MutagenChamberBlock> CODEC = simpleCodec(MutagenChamberBlock::new);

    public MutagenChamberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MutagenChamberBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.MUTAGEN_CHAMBER.get();
    }
}
