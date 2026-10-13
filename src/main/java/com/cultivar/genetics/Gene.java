package com.cultivar.genetics;

import java.util.Locale;

public enum Gene {
    YIELD("yld", true, 0.1f, 2.5f),
    GROWTH_RATE("grw", true, 0.2f, 2.2f),
    COLD_TOLERANCE("cld", false, -1.0f, 1.2f),
    DROUGHT_TOLERANCE("drt", false, 0.0f, 1.0f),
    LIGHT_NEED("lgt", false, 0.0f, 15.0f),
    BLIGHT_RESISTANCE("bre", true, 0.0f, 0.97f),
    NUTRIENT_DRAW("ntr", false, 0.2f, 2.5f),
    MUTATION_RATE("mut", false, 0.0f, 0.25f);

    public static final Gene[] VALUES = values();
    public static final int COUNT = VALUES.length;

    private final String shortCode;
    private final boolean higherIsBetter;
    private final float min;
    private final float max;

    Gene(String shortCode, boolean higherIsBetter, float min, float max) {
        this.shortCode = shortCode;
        this.higherIsBetter = higherIsBetter;
        this.min = min;
        this.max = max;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String shortCode() {
        return shortCode;
    }

    public boolean higherIsBetter() {
        return higherIsBetter;
    }

    public float min() {
        return min;
    }

    public float max() {
        return max;
    }

    public float clamp(float value) {
        return Math.max(min, Math.min(max, value));
    }

    public String translationKey() {
        return "gene.cultivar." + id();
    }

    public String format(float value) {
        return switch (this) {
            case YIELD, GROWTH_RATE, NUTRIENT_DRAW -> String.format(Locale.ROOT, "x%.2f", value);
            case COLD_TOLERANCE -> String.format(Locale.ROOT, "min %.2f", value);
            case DROUGHT_TOLERANCE -> String.format(Locale.ROOT, "min %d%%", Math.round(value * 100));
            case LIGHT_NEED -> String.format(Locale.ROOT, "light %d", Math.round(value));
            case BLIGHT_RESISTANCE -> String.format(Locale.ROOT, "%d%%", Math.round(value * 100));
            case MUTATION_RATE -> String.format(Locale.ROOT, "%.1f%%", value * 100);
        };
    }

    public static Gene byId(String id) {
        for (Gene gene : VALUES) {
            if (gene.id().equals(id) || gene.shortCode.equals(id)) {
                return gene;
            }
        }
        return null;
    }
}
