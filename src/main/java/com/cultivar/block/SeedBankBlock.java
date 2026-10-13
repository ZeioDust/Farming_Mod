package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SeedBankBlock extends MachineBlock {
    public static final MapCodec<SeedBankBlock> CODEC = simpleCodec(SeedBankBlock::new);

    public SeedBankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SeedBankBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.SEED_BANK.get();
    }
}
