package com.Chagui68.entities.boss.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * An attack's choreography, tick by tick: spans that run every tick between two marks (with their
 * progress), one-shot events at a mark, and periodic events.
 *
 * <p>The timeline knows nothing about the server. {@link #tick()} advances it one tick; the stage
 * that plays it calls that from a scheduled task in the game, and a test or a preview calls it in a
 * loop.
 */
public final class Timeline {

    /** Work done on every tick of a span: {@code tick} counts from the span's start, {@code progress} runs 0..1. */
    @FunctionalInterface
    public interface Step {
        void run(int tick, double progress);
    }

    private record Span(int from, int to, Step step) {
    }

    private final List<Span> spans = new ArrayList<>();
    private final List<Runnable> finishers = new ArrayList<>();
    private int length;
    private int now;
    private boolean stopped;

    /** Runs {@code step} on every tick in {@code [from, to)}. */
    public Timeline span(int from, int to, Step step) {
        spans.add(new Span(from, Math.max(from + 1, to), step));
        length = Math.max(length, Math.max(from + 1, to));
        return this;
    }

    /** Runs {@code event} once, on tick {@code at}. */
    public Timeline at(int at, Runnable event) {
        return span(at, at + 1, (tick, progress) -> event.run());
    }

    /** Runs {@code event} every {@code period} ticks in {@code [from, to)}, with the tick number. */
    public Timeline every(int from, int to, int period, IntConsumer event) {
        int p = Math.max(1, period);
        return span(from, to, (tick, progress) -> {
            if (tick % p == 0) event.accept(from + tick);
        });
    }

    /** Keeps the timeline alive at least until {@code at}, for effects that linger with no work to do. */
    public Timeline hold(int at) {
        length = Math.max(length, at);
        return this;
    }

    /** Runs once when the timeline ends, whether it ran out or was stopped. */
    public Timeline onFinish(Runnable finisher) {
        finishers.add(finisher);
        return this;
    }

    /** Ends the timeline early, e.g. when a projectile hits; finishers still run. */
    public void stop() {
        stopped = true;
    }

    public int length() {
        return length;
    }

    public int now() {
        return now;
    }

    /**
     * Plays one tick.
     *
     * @return whether there is more to play
     */
    public boolean tick() {
        if (stopped || now >= length) {
            finish();
            return false;
        }
        for (Span span : List.copyOf(spans)) {
            if (now < span.from || now >= span.to) continue;
            int local = now - span.from;
            int duration = span.to - span.from;
            double progress = duration <= 1 ? 1.0 : (double) local / (duration - 1);
            span.step.run(local, progress);
            if (stopped) break;
        }
        now++;
        if (stopped || now >= length) {
            finish();
            return false;
        }
        return true;
    }

    private boolean finished;

    private void finish() {
        if (finished) return;
        finished = true;
        for (Runnable finisher : finishers) finisher.run();
    }
}
