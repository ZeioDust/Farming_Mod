package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ContractBoardBlock extends MachineBlock {
    public static final MapCodec<ContractBoardBlock> CODEC = simpleCodec(ContractBoardBlock::new);

    public ContractBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ContractBoardBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.CONTRACT_BOARD.get();
    }
}
