package com.cultivar.world;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TickStats {
    public static final String GROWTH = "growth+soil";
    public static final String POLLINATION = "pollination";
    public static final String BLIGHT = "blight";
    public static final String CLIMATE = "climate";
    public static final String WIND = "wind";
    public static final String ASYNC_APPLY = "async-apply";
    public static final int WINDOW = 200;

    private static final class Series {
        long current;
        int currentItems;
        final long[] window = new long[WINDOW];
        int last;
        int lastItems;
        int filled;
        int head;

        void roll() {
            window[head] = current;
            head = (head + 1) % WINDOW;
            filled = Math.min(WINDOW, filled + 1);
            last = (int) Math.min(Integer.MAX_VALUE, current);
            lastItems = currentItems;
            current = 0;
            currentItems = 0;
        }

        double average() {
            if (filled == 0) {
                return 0;
            }
            long sum = 0;
            for (int i = 0; i < filled; i++) {
                sum += window[i];
            }
            return sum / (double) filled;
        }

        long peak() {
            long max = 0;
            for (int i = 0; i < filled; i++) {
                max = Math.max(max, window[i]);
            }
            return max;
        }
    }

    public record Figures(long last, double average, long peak, int items) {
    }

    private static final Map<String, Series> SERIES = new LinkedHashMap<>();

    static {
        for (String name : new String[]{GROWTH, POLLINATION, BLIGHT, CLIMATE, WIND, ASYNC_APPLY}) {
            SERIES.put(name, new Series());
        }
    }

    private TickStats() {
    }

    public static synchronized void record(String subsystem, long nanos) {
        SERIES.computeIfAbsent(subsystem, k -> new Series()).current += nanos;
    }

    public static synchronized void record(String subsystem, long nanos, int workItems) {
        Series series = SERIES.computeIfAbsent(subsystem, k -> new Series());
        series.current += nanos;
        series.currentItems += workItems;
    }

    public static synchronized void endTick() {
        for (Series series : SERIES.values()) {
            series.roll();
        }
    }

    public static synchronized Map<String, Figures> figures() {
        Map<String, Figures> out = new LinkedHashMap<>();
        SERIES.forEach((name, s) -> out.put(name, new Figures(s.last, s.average(), s.peak(), s.lastItems)));
        return out;
    }
}
