package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The set of viewers the plugin currently tracks: a first-class collection that doubles as the
 * {@link ViewerDirectory}.
 *
 * <p>It is the authoritative answer to "who should be rendered". A viewer is added on join and
 * {@linkplain #forget(ViewerId) forgotten} on quit, so it never retains a disconnected player.
 */
public final class ViewerRegistry implements ViewerDirectory, ViewerScoped {

  private final Set<ViewerId> viewers = ConcurrentHashMap.newKeySet();

  /**
   * Registers a viewer.
   *
   * @param viewer the viewer to track; never {@code null}
   */
  public void add(ViewerId viewer) {
    viewers.add(Objects.requireNonNull(viewer, "viewer"));
  }

  @Override
  public void forget(ViewerId viewer) {
    viewers.remove(viewer);
  }

  @Override
  public Collection<ViewerId> viewers() {
    return List.copyOf(viewers);
  }
}
