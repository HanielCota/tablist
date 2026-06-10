package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A first-class collection of the viewers that need re-rendering.
 *
 * <p>Marking a viewer is idempotent within a flush window: a viewer marked any number of times is
 * drained exactly once. {@link #drainDirty()} atomically returns the pending viewers and clears the
 * set, so producers on other threads may keep marking while a flush is in progress.
 */
public final class DirtyTracker implements ViewerScoped {

  private final Set<ViewerId> dirty = new LinkedHashSet<>();

  /**
   * Marks a viewer as needing a re-render.
   *
   * @param viewer the viewer to mark; never {@code null}
   */
  public synchronized void markDirty(ViewerId viewer) {
    dirty.add(Objects.requireNonNull(viewer, "viewer"));
  }

  /**
   * Atomically returns the currently dirty viewers and clears the set.
   *
   * @return an immutable snapshot of the drained viewers, in the order first marked
   */
  public synchronized Collection<ViewerId> drainDirty() {
    Collection<ViewerId> drained = List.copyOf(dirty);
    dirty.clear();
    return drained;
  }

  /**
   * Tells whether there are no dirty viewers.
   *
   * @return {@code true} if nothing is pending
   */
  public synchronized boolean isEmpty() {
    return dirty.isEmpty();
  }

  /**
   * Removes a viewer from the dirty set so a disconnected viewer is never flushed.
   *
   * @param viewer the viewer to forget; never {@code null}
   */
  @Override
  public synchronized void forget(ViewerId viewer) {
    dirty.remove(viewer);
  }
}
