package com.cultivar.blockentity;

import com.cultivar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TrialMarkerBlockEntity extends SyncedBlockEntity {
    public static final String DEFAULT_LABEL = "Trial";

    private String label = DEFAULT_LABEL;
    private int number;

    public TrialMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_MARKER.get(), pos, state);
    }

    public String label() {
        return label;
    }

    public int number() {
        return number;
    }

    public void set(String label, int number) {
        this.label = label.length() > 32 ? label.substring(0, 32) : label;
        this.number = number;
        markUpdated();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("label", label);
        output.putInt("number", number);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        label = input.getStringOr("label", DEFAULT_LABEL);
        number = input.getIntOr("number", 0);
    }
}
