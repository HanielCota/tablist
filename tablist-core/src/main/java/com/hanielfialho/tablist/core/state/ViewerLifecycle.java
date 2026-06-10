package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates what happens when a viewer joins or quits.
 *
 * <p>On join a viewer is registered and marked dirty so the next flush renders them. On quit every
 * per-viewer structure is told to {@linkplain ViewerScoped#forget(ViewerId) forget} the viewer —
 * the registry, the dirty set, the snapshot store and the caches — so nothing retains a
 * disconnected player. This is the single place that owns the anti-leak guarantee.
 */
public final class ViewerLifecycle {

  private final ViewerRegistry registry;
  private final DirtyTracker dirty;
  private final List<ViewerScoped> forgettables;

  /**
   * Creates the lifecycle coordinator.
   *
   * @param registry the viewer registry; never {@code null}
   * @param dirty the dirty tracker; never {@code null}
   * @param forgettables the other per-viewer structures to clear on quit (store, caches); never
   *     {@code null}
   */
  public ViewerLifecycle(
      ViewerRegistry registry, DirtyTracker dirty, List<ViewerScoped> forgettables) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.dirty = Objects.requireNonNull(dirty, "dirty");
    this.forgettables = List.copyOf(forgettables);
  }

  /**
   * Registers the viewer and marks it dirty for the next flush.
   *
   * @param viewer the viewer that joined; never {@code null}
   */
  public void join(ViewerId viewer) {
    Objects.requireNonNull(viewer, "viewer");
    registry.add(viewer);
    dirty.markDirty(viewer);
  }

  /**
   * Forgets the viewer everywhere, leaving no structure holding a reference to it.
   *
   * @param viewer the viewer that quit; never {@code null}
   */
  public void quit(ViewerId viewer) {
    Objects.requireNonNull(viewer, "viewer");
    registry.forget(viewer);
    dirty.forget(viewer);
    forgettables.forEach(scoped -> scoped.forget(viewer));
  }
}
