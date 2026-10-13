package com.cultivar.menu;

import java.util.function.IntUnaryOperator;
import net.minecraft.world.inventory.ContainerData;

public final class ComputedData implements ContainerData {
    private final int count;
    private final IntUnaryOperator source;
    private final int[] overrides;
    private final boolean[] overridden;

    public ComputedData(int count, IntUnaryOperator source) {
        this.count = count;
        this.source = source;
        this.overrides = new int[count];
        this.overridden = new boolean[count];
    }

    @Override
    public int get(int index) {
        return overridden[index] ? overrides[index] : source.applyAsInt(index);
    }

    @Override
    public void set(int index, int value) {
        overrides[index] = value;
        overridden[index] = true;
    }

    @Override
    public int getCount() {
        return count;
    }
}
