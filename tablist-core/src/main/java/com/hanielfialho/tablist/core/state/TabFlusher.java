package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.port.TabRenderer;
import java.util.Objects;

/**
 * Coalesces tab-list updates: once per flush it drains the dirty viewers, rebuilds each viewer's
 * snapshot, diffs it against the last one sent and renders only the difference.
 *
 * <p>Two viewers marked many times between flushes are still rendered once each, and a viewer whose
 * rebuilt snapshot is unchanged is not rendered at all. The renderer therefore receives the minimum
 * set of changes, never a redundant push.
 */
public final class TabFlusher {

  private final DirtyTracker dirty;
  private final TabRenderer renderer;
  private final SnapshotStore store;

  /**
   * Creates a flusher over the given collaborators.
   *
   * @param dirty the set of viewers awaiting a re-render; never {@code null}
   * @param renderer the port that pushes diffs to the platform; never {@code null}
   * @param store the memory of the last snapshot sent per viewer; never {@code null}
   */
  public TabFlusher(DirtyTracker dirty, TabRenderer renderer, SnapshotStore store) {
    this.dirty = Objects.requireNonNull(dirty, "dirty");
    this.renderer = Objects.requireNonNull(renderer, "renderer");
    this.store = Objects.requireNonNull(store, "store");
  }

  /**
   * Runs one flush: drains the dirty viewers and renders the diff for each.
   *
   * @param source builds the current snapshot for a viewer; never {@code null}
   */
  public void flush(SnapshotSource source) {
    dirty.drainDirty().forEach(viewer -> flushViewer(viewer, source));
  }

  private void flushViewer(ViewerId viewer, SnapshotSource source) {
    TabSnapshot next = source.snapshotOf(viewer);
    renderChanges(viewer, next.diffFrom(store.lastFor(viewer)));
    store.store(next);
  }

  private void renderChanges(ViewerId viewer, TabDiff diff) {
    if (diff.isEmpty()) {
      return;
    }
    renderer.render(viewer, diff);
  }
}
