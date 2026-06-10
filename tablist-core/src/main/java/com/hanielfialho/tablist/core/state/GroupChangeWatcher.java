package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.GroupWeightResolver;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when a viewer's sorting group changes at runtime and marks only that viewer dirty.
 *
 * <p>Group membership is driven by permissions, which can change while a player is online (a
 * promotion, a temporary rank) without any event Tablist can subscribe to portably. So the watcher
 * polls: each {@link #check()} re-resolves every viewer's order and, for the ones whose order
 * moved, marks <em>them</em> dirty — never everyone. Because a player's list name and order are
 * applied globally, re-rendering just the changed viewer is enough to update how they appear in
 * every tab, which is why this avoids a global refresh.
 *
 * <p>It is {@link ViewerScoped}: a quitting viewer is forgotten so nothing it tracked outlives the
 * connection.
 */
public final class GroupChangeWatcher implements ViewerScoped {

  private final GroupWeightResolver groups;
  private final ViewerDirectory viewers;
  private final DirtyTracker dirty;
  private final Map<ViewerId, Integer> lastOrder = new ConcurrentHashMap<>();

  /**
   * Creates the watcher.
   *
   * @param groups resolves a viewer's current order; never {@code null}
   * @param viewers the viewers to watch; never {@code null}
   * @param dirty the tracker to mark on a change; never {@code null}
   */
  public GroupChangeWatcher(
      GroupWeightResolver groups, ViewerDirectory viewers, DirtyTracker dirty) {
    this.groups = Objects.requireNonNull(groups, "groups");
    this.viewers = Objects.requireNonNull(viewers, "viewers");
    this.dirty = Objects.requireNonNull(dirty, "dirty");
  }

  /**
   * Re-resolves every viewer's order and marks dirty those whose order changed since the last call.
   *
   * <p>A viewer seen for the first time is only recorded, not marked: a fresh join is already
   * dirty.
   */
  public void check() {
    viewers.viewers().forEach(this::checkOne);
  }

  private void checkOne(ViewerId viewer) {
    int now = groups.orderOf(viewer);
    Integer previous = lastOrder.put(viewer, now);
    if (previous != null && previous.intValue() != now) {
      dirty.markDirty(viewer);
    }
  }

  @Override
  public void forget(ViewerId viewer) {
    lastOrder.remove(viewer);
  }
}
