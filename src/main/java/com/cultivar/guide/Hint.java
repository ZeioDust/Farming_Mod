package com.cultivar.guide;

import org.jspecify.annotations.Nullable;

public enum Hint {
    PLANTED("planted", Chapter.START),
    FLOWERING("flowering", Chapter.POLLINATION),
    HARVESTED("harvested", Chapter.LAB),
    CARD("card", Chapter.GENES),
    DEPLETED("depleted", Chapter.SOIL),
    BLIGHT("blight", Chapter.BLIGHT),
    WILD("wild", Chapter.CANOPY),
    PRIME("prime", Chapter.MARKET),
    PROCESSED("processed", Chapter.PROCESSING);

    private final String id;
    private final Chapter chapter;

    Hint(String id, Chapter chapter) {
        this.id = id;
        this.chapter = chapter;
    }

    public String id() {
        return id;
    }

    public Chapter chapter() {
        return chapter;
    }

    public String titleKey() {
        return "hint.cultivar." + id + ".title";
    }

    public String bodyKey() {
        return "hint.cultivar." + id + ".body";
    }

    public static @Nullable Hint byId(String id) {
        for (Hint hint : values()) {
            if (hint.id.equals(id)) {
                return hint;
            }
        }
        return null;
    }

    public enum Chapter { START, GENES, POLLINATION, SOIL, BLIGHT, LAB, GREENHOUSE, MUTATION, CANOPY, MARKET, PROCESSING, MASTERY }
}
