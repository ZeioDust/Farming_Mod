package com.cultivar.client.ui;

import com.cultivar.data.Catalog;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.cultivar.genetics.Phenotype;
import org.jspecify.annotations.Nullable;

public final class GenomeArt {
    private GenomeArt() {
    }

    public static int alleleColor(Catalog catalog, Allele allele) {
        int base = catalog.gene(allele.gene()).color() | 0xFF000000;
        int c = Theme.lerp(Theme.shade(base, 0.75f), 0xFFFFFFFF, Math.min(0.45f, allele.code() * 0.08f));
        if (allele.wild()) {
            c = Theme.lerp(c, Theme.WILD, 0.55f);
        }
        if (allele.mutationOnly()) {
            c = Theme.lerp(c, Theme.MUTANT, 0.55f);
        }
        return c;
    }

    public static int alleleColor(Catalog catalog, Gene gene, int code) {
        AlleleTable table = catalog.table();
        if (!table.has(gene, code)) {
            return 0xFF556066;
        }
        return alleleColor(catalog, table.get(gene, code));
    }

    public static void helix(Canvas c, float x0, float x1, float cy, float radius, float phase, @Nullable Genome genome,
                             Catalog catalog, float reveal, boolean active, int strand, float time) {
        int rungs = 16;
        int steps = 96;
        float len = x1 - x0;
        float twist = (float) (Math.PI * 2 * 2.25f) / len;
        for (int pass = 0; pass < 2; pass++) {
            for (int s = 0; s < 2; s++) {
                float off = s == 0 ? 0f : (float) Math.PI;
                for (int i = 0; i < steps; i++) {
                    float xa = x0 + len * i / steps;
                    float xb = x0 + len * (i + 1) / steps;
                    float ta = phase + (xa - x0) * twist + off;
                    float tb = phase + (xb - x0) * twist + off;
                    float za = Canvas.cos(ta);
                    boolean front = za >= 0f;
                    if (front != (pass == 1)) {
                        continue;
                    }
                    float depth = 0.45f + 0.55f * (za * 0.5f + 0.5f);
                    float taper = edgeTaper((xa - x0) / len);
                    int col = Theme.fade(Theme.shade(strand, depth), taper);
                    c.line(xa, cy + Canvas.sin(ta) * radius, xb, cy + Canvas.sin(tb) * radius, 1.2f + 1.1f * depth, col);
                }
            }
            if (pass == 0) {
                for (int r = 0; r < rungs; r++) {
                    float x = x0 + len * (r + 0.5f) / rungs;
                    float t = phase + (x - x0) * twist;
                    float ya = cy + Canvas.sin(t) * radius;
                    float yb = cy + Canvas.sin(t + (float) Math.PI) * radius;
                    float depth = 0.55f + 0.45f * Math.abs(Canvas.cos(t));
                    float taper = edgeTaper((x - x0) / len);
                    Gene gene = Gene.VALUES[r % Gene.COUNT];
                    boolean shown = genome != null && (r + 0.5f) / rungs <= reveal;
                    int ca;
                    int cb;
                    if (shown) {
                        ca = alleleColor(catalog, gene, genome.first(gene));
                        cb = alleleColor(catalog, gene, genome.second(gene));
                    } else if (genome != null || active) {
                        float flicker = Anim.hash((int) (time * 18f) * 31 + r);
                        ca = Theme.lerp(0xFF203238, strand, flicker * 0.5f);
                        cb = Theme.lerp(0xFF203238, strand, (1f - flicker) * 0.5f);
                    } else {
                        ca = 0xFF27383E;
                        cb = 0xFF27383E;
                    }
                    float mid = (ya + yb) * 0.5f;
                    c.line(x, ya, x, mid, 1.6f * depth + 0.6f, Theme.fade(Theme.shade(ca, depth), taper));
                    c.line(x, mid, x, yb, 1.6f * depth + 0.6f, Theme.fade(Theme.shade(cb, depth), taper));
                    if (shown) {
                        c.bloom(x, ya, 3f, Theme.fade(ca, 0.5f * taper));
                        c.bloom(x, yb, 3f, Theme.fade(cb, 0.5f * taper));
                    }
                }
            }
        }
    }

    private static float edgeTaper(float t) {
        return Anim.clamp01(Math.min(t, 1f - t) * 7f);
    }

    public static void barcode(Canvas c, float x, float y, float w, float h, Genome genome, Catalog catalog) {
        AlleleTable table = catalog.table();
        Phenotype phenotype = Phenotype.resolve(genome, table);
        float cw = w / Gene.COUNT;
        float ch = h / 2f;
        for (Gene gene : Gene.VALUES) {
            float gx = x + gene.ordinal() * cw;
            for (int slot = 0; slot < 2; slot++) {
                int code = genome.allele(gene, slot);
                int col = alleleColor(catalog, gene, code);
                boolean expressed = genome.isHomozygous(gene) || phenotype.isExpressed(gene, slot);
                float cy = y + slot * ch;
                c.rect(gx + 0.5f, cy + 0.5f, cw - 1f, ch - 1f, expressed ? col : Theme.fade(Theme.shade(col, 0.55f), 0.9f));
                if (expressed) {
                    c.frame(gx + 0.5f, cy + 0.5f, cw - 1f, ch - 1f, 0.6f, 0xCCFFFFFF);
                }
            }
        }
    }
}
