package com.hanielfialho.tablist.core.status;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.AnimationClock;
import com.hanielfialho.tablist.core.state.UpdateMetrics;
import com.hanielfialho.tablist.core.text.ResolvedTextCache;
import java.util.Objects;

/**
 * The live sources {@link StatusReporter} reads to assemble a {@link TablistStatus}. A plain bundle
 * of references so the reporter keeps a single collaborator.
 *
 * @param viewers the directory of active viewers
 * @param cache the resolved-text cache (for its hit-rate)
 * @param metrics the rolling update counter
 * @param clock the animation clock (for the current frame)
 * @param config the active configuration (for the header frame layout)
 */
public record StatusSources(
    ViewerDirectory viewers,
    ResolvedTextCache cache,
    UpdateMetrics metrics,
    AnimationClock clock,
    ActiveConfig config) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if any source is {@code null}
   */
  public StatusSources {
    Objects.requireNonNull(viewers, "viewers");
    Objects.requireNonNull(cache, "cache");
    Objects.requireNonNull(metrics, "metrics");
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(config, "config");
  }
}
