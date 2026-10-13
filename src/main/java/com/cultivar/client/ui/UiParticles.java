package com.cultivar.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class UiParticles {
    public enum Kind { MOTE, BUBBLE, STEAM, SPARK, GLINT }

    private static final int MAX = 220;
    private final List<P> live = new ArrayList<>();
    private final Random random = new Random();

    private static final class P {
        Kind kind;
        float x;
        float y;
        float vx;
        float vy;
        float age;
        float life;
        float size;
        int color;
        float wobble;
    }

    public Random random() {
        return random;
    }

    public int count() {
        return live.size();
    }

    public void spawn(Kind kind, float x, float y, float vx, float vy, float life, float size, int color) {
        if (live.size() >= MAX) {
            return;
        }
        P p = new P();
        p.kind = kind;
        p.x = x;
        p.y = y;
        p.vx = vx;
        p.vy = vy;
        p.life = life;
        p.size = size;
        p.color = color;
        p.wobble = random.nextFloat() * 6.28f;
        live.add(p);
    }

    public void emit(float perSecond, float dt, Runnable factory) {
        float expected = perSecond * dt;
        int whole = (int) expected;
        for (int i = 0; i < whole; i++) {
            factory.run();
        }
        if (random.nextFloat() < expected - whole) {
            factory.run();
        }
    }

    public void update(float dt) {
        float step = Math.min(dt, 0.1f);
        for (int i = live.size() - 1; i >= 0; i--) {
            P p = live.get(i);
            p.age += step;
            if (p.age >= p.life) {
                live.remove(i);
                continue;
            }
            switch (p.kind) {
                case BUBBLE -> {
                    p.vy -= 18f * step;
                    p.x += (float) Math.sin(p.age * 7f + p.wobble) * 6f * step;
                }
                case STEAM -> {
                    p.vx += (float) Math.sin(p.age * 2.2f + p.wobble) * 8f * step;
                    p.size += 4f * step;
                }
                case MOTE -> {
                    p.vx += (float) Math.sin(p.age * 1.3f + p.wobble) * 3f * step;
                    p.vy += (float) Math.cos(p.age * 1.1f + p.wobble) * 3f * step;
                }
                case SPARK -> p.vy += 40f * step;
                case GLINT -> {
                }
            }
            p.x += p.vx * step;
            p.y += p.vy * step;
        }
    }

    public void draw(Canvas c) {
        for (P p : live) {
            float t = p.age / p.life;
            float fadeIn = Math.min(1f, t * 6f);
            float fadeOut = 1f - t;
            float a = fadeIn * fadeOut;
            switch (p.kind) {
                case BUBBLE -> {
                    c.ring(p.x, p.y, p.size, 0.7f, Theme.fade(p.color, a));
                    c.rect(p.x - p.size * 0.45f, p.y - p.size * 0.5f, 0.8f, 0.8f, Theme.fade(0xFFFFFFFF, a * 0.8f));
                }
                case STEAM -> c.bloom(p.x, p.y, p.size, Theme.fade(p.color, a * 0.5f));
                case MOTE -> c.bloom(p.x, p.y, p.size, Theme.fade(p.color, a));
                case SPARK -> c.line(p.x, p.y, p.x - p.vx * 0.04f, p.y - p.vy * 0.04f, 0.8f, Theme.fade(p.color, a));
                case GLINT -> {
                    float s = p.size * (0.6f + 0.4f * (float) Math.sin(t * Math.PI));
                    c.bloom(p.x, p.y, s * 2f, Theme.fade(p.color, a * 0.6f));
                    c.line(p.x - s, p.y, p.x + s, p.y, 0.6f, Theme.fade(p.color, a));
                    c.line(p.x, p.y - s, p.x, p.y + s, 0.6f, Theme.fade(p.color, a));
                }
            }
        }
    }
}
