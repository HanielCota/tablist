package com.hanielfialho.tablist.core.state;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Counts the tab-list updates actually sent, over a rolling one-minute window.
 *
 * <p>Each render records a timestamp; the window prunes entries older than a minute on every read
 * or write, so {@link #updatesInLastMinute()} is always current. The {@link Clock} is injected so
 * the window can be driven deterministically in tests.
 */
public final class UpdateMetrics {

  private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();

  private final Clock clock;
  private final Deque<Long> timestamps = new ArrayDeque<>();

  /** Creates metrics backed by the system UTC clock. */
  public UpdateMetrics() {
    this(Clock.systemUTC());
  }

  /**
   * Creates metrics backed by the given clock.
   *
   * @param clock the clock measuring the window; never {@code null}
   */
  public UpdateMetrics(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /** Records that one update was sent now. */
  public synchronized void recordUpdate() {
    long now = clock.millis();
    timestamps.addLast(now);
    prune(now);
  }

  /**
   * Returns how many updates were sent in the last minute.
   *
   * @return the rolling one-minute count
   */
  public synchronized int updatesInLastMinute() {
    prune(clock.millis());
    return timestamps.size();
  }

  private void prune(long now) {
    while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
      timestamps.removeFirst();
    }
  }
}
