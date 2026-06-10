package com.hanielfialho.tablist.core.state;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The shared animation tick, advanced once per flush.
 *
 * <p>Extracting the tick into its own object lets the flush loop advance it while the snapshot
 * source and the status reporter read it, all seeing the same clock without threading it through
 * constructors.
 */
public final class AnimationClock {

  private final AtomicLong tick = new AtomicLong();

  /**
   * Advances the clock by one and returns the new value.
   *
   * @return the tick after advancing
   */
  public long advance() {
    return tick.incrementAndGet();
  }

  /**
   * Returns the current tick without advancing it.
   *
   * @return the current tick
   */
  public long tick() {
    return tick.get();
  }
}
