package com.hanielfialho.tablist.core.config;

import com.hanielfialho.tablist.core.state.AnimationClock;
import java.util.Objects;

/**
 * One beat of the flush loop's clock: advance the animation tick and mark every viewer dirty.
 *
 * <p>Bundling the two keeps the loop driver small. Marking all viewers dirty each beat is what
 * drives animation and periodic refresh; the flusher's diff still suppresses redundant sends.
 */
public final class Heartbeat {

  private final AnimationClock clock;
  private final DirtyAllViewers dirtyAllViewers;

  /**
   * Creates the heartbeat.
   *
   * @param clock the animation clock to advance; never {@code null}
   * @param dirtyAllViewers marks every viewer dirty; never {@code null}
   */
  public Heartbeat(AnimationClock clock, DirtyAllViewers dirtyAllViewers) {
    this.clock = Objects.requireNonNull(clock, "clock");
    this.dirtyAllViewers = Objects.requireNonNull(dirtyAllViewers, "dirtyAllViewers");
  }

  /** Advances the clock and marks every viewer dirty. */
  public void beat() {
    clock.advance();
    dirtyAllViewers.markAll();
  }
}
