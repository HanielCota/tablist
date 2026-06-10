package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.TabRenderer;
import java.util.Objects;

/**
 * A {@link TabRenderer} decorator that records each render in {@link UpdateMetrics} before
 * delegating, so the {@code /tablist status} command can report how many updates were sent.
 */
public final class MeteredTabRenderer implements TabRenderer {

  private final TabRenderer delegate;
  private final UpdateMetrics metrics;

  /**
   * Wraps a renderer with update metering.
   *
   * @param delegate the renderer that actually sends the diff; never {@code null}
   * @param metrics the metrics to record into; never {@code null}
   */
  public MeteredTabRenderer(TabRenderer delegate, UpdateMetrics metrics) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.metrics = Objects.requireNonNull(metrics, "metrics");
  }

  @Override
  public void render(ViewerId viewer, TabDiff diff) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(diff, "diff");
    metrics.recordUpdate();
    delegate.render(viewer, diff);
  }
}
