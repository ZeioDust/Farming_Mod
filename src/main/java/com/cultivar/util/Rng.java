package com.cultivar.util;

import java.util.random.RandomGenerator;
import net.minecraft.util.RandomSource;

public final class Rng {
    private Rng() {
    }

    public static RandomGenerator of(RandomSource source) {
        return new RandomGenerator() {
            @Override
            public long nextLong() {
                return source.nextLong();
            }

            @Override
            public int nextInt(int bound) {
                return source.nextInt(bound);
            }

            @Override
            public float nextFloat() {
                return source.nextFloat();
            }

            @Override
            public double nextDouble() {
                return source.nextDouble();
            }
        };
    }
}
