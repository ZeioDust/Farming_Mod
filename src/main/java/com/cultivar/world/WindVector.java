package com.cultivar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WindVector(float x, float z, float strength, long updated) {
    public static final WindVector CALM = new WindVector(1f, 0f, 0f, Long.MIN_VALUE);

    public static final MapCodec<WindVector> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("x").forGetter(WindVector::x),
            Codec.FLOAT.fieldOf("z").forGetter(WindVector::z),
            Codec.FLOAT.fieldOf("strength").forGetter(WindVector::strength),
            Codec.LONG.fieldOf("updated").forGetter(WindVector::updated)
    ).apply(i, WindVector::new));

    public float angleDegrees() {
        return (float) Math.toDegrees(Math.atan2(z, x));
    }

    public String compass() {
        String[] names = {"east", "south-east", "south", "south-west", "west", "north-west", "north", "north-east"};
        float angle = angleDegrees();
        int index = Math.floorMod(Math.round(angle / 45f), 8);
        return names[index];
    }
}
