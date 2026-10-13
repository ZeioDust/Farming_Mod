package com.cultivar.client.ui;

import net.minecraft.util.Util;

public final class Anim {
    private Anim() {
    }

    public static float now() {
        return (Util.getMillis() % 3_600_000L) / 1000f;
    }

    public static float clamp01(float t) {
        return Math.max(0f, Math.min(1f, t));
    }

    public static float easeOutCubic(float t) {
        float k = 1f - clamp01(t);
        return 1f - k * k * k;
    }

    public static float easeInOut(float t) {
        float k = clamp01(t);
        return k * k * (3f - 2f * k);
    }

    public static float easeOutBack(float t) {
        float k = clamp01(t) - 1f;
        float c = 1.70158f;
        return 1f + (c + 1f) * k * k * k + c * k * k;
    }

    public static float pulse(float time, float hz) {
        return 0.5f + 0.5f * (float) Math.sin(time * hz * Math.PI * 2);
    }

    public static float hash(int seed) {
        int h = seed * 0x9E3779B1;
        h ^= h >>> 15;
        h *= 0x85EBCA77;
        h ^= h >>> 13;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }

    public static final class Smooth {
        private float value;
        private boolean primed;
        private final float rate;

        public Smooth(float rate) {
            this.rate = rate;
        }

        public float update(float target, float dt) {
            if (!primed) {
                value = target;
                primed = true;
            } else {
                value += (target - value) * (1f - (float) Math.exp(-rate * dt));
            }
            return value;
        }

        public Smooth from(float start) {
            value = start;
            primed = true;
            return this;
        }

        public float value() {
            return value;
        }
    }
}
