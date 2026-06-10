package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;

/**
 * Something that holds per-viewer state and can drop everything it keeps for one viewer.
 *
 * <p>Implemented by every structure that would otherwise retain a viewer after they disconnect
 * (dirty set, snapshot store, resolved-text cache, viewer registry). {@link ViewerLifecycle} calls
 * {@link #forget(ViewerId)} on each of them on quit, which is the anti-leak guarantee.
 */
public interface ViewerScoped {

  /**
   * Drops all state held for the given viewer.
   *
   * @param viewer the viewer to forget; never {@code null}
   */
  void forget(ViewerId viewer);
}
