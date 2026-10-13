package com.cultivar.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CultivarParticle extends SingleQuadParticle {
    public enum Style {
        POLLEN(1.0f, 0.92f, 0.45f, 1.0f, -0.002f, 30, 50, 0.6f, false),
        SPORE(0.55f, 0.5f, 0.35f, 0.85f, 0.004f, 40, 70, 0.5f, true),
        WATER(0.55f, 0.7f, 1.0f, 0.9f, 0.06f, 10, 20, 0.45f, true),
        MIST(0.92f, 0.96f, 1.0f, 0.5f, -0.004f, 40, 80, 1.6f, true),
        SOIL(0.45f, 0.32f, 0.2f, 1.0f, 0.05f, 12, 24, 0.5f, false);

        final float r;
        final float g;
        final float b;
        final float alpha;
        final float gravity;
        final int minLife;
        final int maxLife;
        final float scale;
        final boolean translucent;

        Style(float r, float g, float b, float alpha, float gravity, int minLife, int maxLife, float scale, boolean translucent) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = alpha;
            this.gravity = gravity;
            this.minLife = minLife;
            this.maxLife = maxLife;
            this.scale = scale;
            this.translucent = translucent;
        }
    }

    private final Style style;
    private final float baseAlpha;

    protected CultivarParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite,
                               Style style) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.style = style;
        this.xd = xd + (random.nextDouble() - 0.5) * 0.01;
        this.yd = yd;
        this.zd = zd + (random.nextDouble() - 0.5) * 0.01;
        this.gravity = style.gravity;
        this.lifetime = style.minLife + random.nextInt(Math.max(1, style.maxLife - style.minLife));
        this.quadSize *= style.scale * (0.7f + random.nextFloat() * 0.6f);
        float shade = 0.9f + random.nextFloat() * 0.1f;
        this.rCol = style.r * shade;
        this.gCol = style.g * shade;
        this.bCol = style.b * shade;
        this.baseAlpha = style.alpha;
        this.alpha = style.alpha;
        this.hasPhysics = style == Style.WATER || style == Style.SOIL;
    }

    @Override
    public void tick() {
        super.tick();
        if (style == Style.MIST || style == Style.SPORE) {
            float remaining = 1f - age / (float) lifetime;
            setAlpha(baseAlpha * Math.max(0f, remaining));
        }
    }

    @Override
    protected Layer getLayer() {
        return style.translucent ? Layer.TRANSLUCENT : Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Style style;

        public Provider(SpriteSet sprites, Style style) {
            this.sprites = sprites;
            this.style = style;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux,
                                       double zAux, RandomSource random) {
            return new CultivarParticle(level, x, y, z, xAux, yAux, zAux, sprites.get(random), style);
        }
    }
}
