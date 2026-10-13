package com.cultivar.world;

import com.cultivar.Cultivar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AsyncWork {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();
    private static final ThreadFactory FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "Cultivar-Worker-" + THREAD_ID.incrementAndGet());
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    };
    private static ExecutorService executor;
    private static final ConcurrentLinkedQueue<Runnable> RESULTS = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger PENDING = new AtomicInteger();
    private static final AtomicInteger GENERATION = new AtomicInteger();
    private static final int MAX_APPLIES_PER_TICK = 64;

    private AsyncWork() {
    }

    private static synchronized ExecutorService executor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(2, FACTORY);
        }
        return executor;
    }

    public static <T> void submit(String name, Supplier<T> job, Consumer<T> apply) {
        PENDING.incrementAndGet();
        int generation = GENERATION.get();
        executor().execute(() -> {
            try {
                T result = job.get();
                if (generation != GENERATION.get()) {
                    return;
                }
                RESULTS.add(() -> {
                    if (generation == GENERATION.get()) {
                        apply.accept(result);
                    }
                });
            } catch (RuntimeException e) {
                Cultivar.LOGGER.error("Cultivar async job '{}' failed", name, e);
                PENDING.decrementAndGet();
            }
        });
    }

    public static void drain() {
        long start = System.nanoTime();
        int applied = 0;
        Runnable next;
        while (applied < MAX_APPLIES_PER_TICK && (next = RESULTS.poll()) != null) {
            try {
                next.run();
            } catch (RuntimeException e) {
                Cultivar.LOGGER.error("Cultivar async result failed to apply", e);
            }
            PENDING.decrementAndGet();
            applied++;
        }
        if (applied > 0) {
            TickStats.record(TickStats.ASYNC_APPLY, System.nanoTime() - start);
        }
    }

    public static int pending() {
        return PENDING.get();
    }

    public static synchronized void shutdown() {
        GENERATION.incrementAndGet();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        RESULTS.clear();
        PENDING.set(0);
    }
}
