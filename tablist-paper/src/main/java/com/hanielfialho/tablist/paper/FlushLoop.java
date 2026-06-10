package com.hanielfialho.tablist.paper;

import com.hanielfialho.tablist.core.config.Heartbeat;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.state.TabFlusher;
import java.util.Objects;

/**
 * One iteration of the global flush loop: beat (advance the clock and mark every viewer dirty),
 * then flush.
 *
 * <p>The flusher's per-entry diff ensures only genuinely changed content is sent, so a tick with no
 * visible change produces no packets. The flush runs on the global thread and only dispatches — the
 * renderer routes each player interaction back to that player's own thread.
 */
public final class FlushLoop {

  private final Heartbeat heartbeat;
  private final TabFlusher flusher;
  private final SnapshotSource source;

  /**
   * Creates the loop.
   *
   * @param heartbeat advances the clock and marks all viewers dirty each tick; never {@code null}
   * @param flusher coalesces and renders the diffs; never {@code null}
   * @param source builds the snapshots; never {@code null}
   */
  public FlushLoop(Heartbeat heartbeat, TabFlusher flusher, SnapshotSource source) {
    this.heartbeat = Objects.requireNonNull(heartbeat, "heartbeat");
    this.flusher = Objects.requireNonNull(flusher, "flusher");
    this.source = Objects.requireNonNull(source, "source");
  }

  /** Runs one flush iteration. */
  public void run() {
    heartbeat.beat();
    flusher.flush(source);
  }
}
