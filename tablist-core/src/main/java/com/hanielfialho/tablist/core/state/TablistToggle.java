package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which viewers have switched the custom tab list off for themselves.
 *
 * <p>Viewers are enabled by default; a viewer present in this set sees the vanilla tab. Toggling
 * flips the state and returns the new one. On quit a viewer is {@linkplain #forget(ViewerId)
 * forgotten}, so it reverts to the default and nothing is retained.
 */
public final class TablistToggle implements ViewerScoped {

  private final Set<ViewerId> disabled = ConcurrentHashMap.newKeySet();

  /**
   * Flips the custom tab list on or off for the viewer.
   *
   * @param viewer the viewer toggling their tab; never {@code null}
   * @return {@code true} if the custom tab list is now enabled, {@code false} if now vanilla
   */
  public boolean toggle(ViewerId viewer) {
    Objects.requireNonNull(viewer, "viewer");
    if (disabled.remove(viewer)) {
      return true;
    }
    disabled.add(viewer);
    return false;
  }

  /**
   * Tells whether the viewer currently sees the custom tab list.
   *
   * @param viewer the viewer to check; never {@code null}
   * @return {@code true} unless the viewer has toggled it off
   */
  public boolean isEnabled(ViewerId viewer) {
    return !disabled.contains(viewer);
  }

  @Override
  public void forget(ViewerId viewer) {
    disabled.remove(viewer);
  }
}
