package com.cultivar.client.ui;

public final class History {
    private final float[] samples;
    private int size;
    private int head;

    public History(int capacity) {
        this.samples = new float[capacity];
    }

    public void push(float value) {
        samples[head] = value;
        head = (head + 1) % samples.length;
        size = Math.min(size + 1, samples.length);
    }

    public int size() {
        return size;
    }

    public float get(int i) {
        int start = (head - size + samples.length) % samples.length;
        return samples[(start + i) % samples.length];
    }

    public void draw(Canvas c, float x, float y, float w, float h, float min, float max, int color) {
        if (size < 2) {
            return;
        }
        float span = Math.max(1e-4f, max - min);
        float step = w / (samples.length - 1);
        float startX = x + w - step * (size - 1);
        float prevX = 0;
        float prevY = 0;
        int fillTop = Theme.fade(color, 0.35f);
        int fillBottom = Theme.fade(color, 0f);
        for (int i = 0; i < size; i++) {
            float v = Anim.clamp01((get(i) - min) / span);
            float px = startX + i * step;
            float py = y + h - v * h;
            if (i > 0) {
                c.quad(prevX, prevY, fillTop, prevX, y + h, fillBottom, px, y + h, fillBottom, px, py, fillTop);
                c.line(prevX, prevY, px, py, 1.1f, color);
            }
            prevX = px;
            prevY = py;
        }
        c.bloom(prevX, prevY, 4, Theme.fade(color, 0.8f));
        c.disc(prevX, prevY, 1.2f, 0xFFFFFFFF, color);
    }
}
