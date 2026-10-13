package com.cultivar.block;

import com.cultivar.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GeneEditorBlock extends MachineBlock {
    public static final MapCodec<GeneEditorBlock> CODEC = simpleCodec(GeneEditorBlock::new);

    public GeneEditorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GeneEditorBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<?> blockEntityType() {
        return ModBlockEntities.GENE_EDITOR.get();
    }
}
