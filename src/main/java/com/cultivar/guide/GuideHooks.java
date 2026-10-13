package com.cultivar.guide;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class GuideHooks {
    private static Consumer<Hint.@Nullable Chapter> opener = chapter -> {
    };

    private GuideHooks() {
    }

    public static void install(Consumer<Hint.@Nullable Chapter> manualOpener) {
        opener = manualOpener;
    }

    public static void openManual(Hint.@Nullable Chapter chapter) {
        opener.accept(chapter);
    }
}
