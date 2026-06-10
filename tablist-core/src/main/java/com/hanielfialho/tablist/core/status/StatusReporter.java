package com.hanielfialho.tablist.core.status;

import com.hanielfialho.tablist.core.config.FrameSection;
import java.util.Objects;

/**
 * Assembles the current {@link TablistStatus} from the live runtime sources.
 *
 * <p>This is the service {@code /tablist status} delegates to; the command only renders what this
 * returns. The current frame is derived from the animation clock and the header's frame layout, the
 * same way the snapshot source selects it.
 */
public final class StatusReporter {

  private final StatusSources sources;

  /**
   * Creates the reporter over its sources.
   *
   * @param sources the live runtime sources; never {@code null}
   */
  public StatusReporter(StatusSources sources) {
    this.sources = Objects.requireNonNull(sources, "sources");
  }

  /**
   * Captures a snapshot of the current status.
   *
   * @return the current status
   */
  public TablistStatus report() {
    return new TablistStatus(
        sources.viewers().viewers().size(),
        sources.cache().hitRate(),
        sources.metrics().updatesInLastMinute(),
        currentFrame());
  }

  private int currentFrame() {
    FrameSection header = sources.config().current().header();
    long step = Math.floorDiv(sources.clock().tick(), header.intervalTicks());
    return Math.floorMod(step, header.frames().size());
  }
}
