package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A first-class collection holding the last {@link TabSnapshot} sent to each viewer.
 *
 * <p>It is the flusher's memory of "what the client currently shows", against which the next
 * snapshot is diffed. An unknown viewer reads back as {@link TabSnapshot#empty(ViewerId) empty}, so
 * the first diff naturally contains the whole tab.
 */
public final class SnapshotStore implements ViewerScoped {

  private final Map<ViewerId, TabSnapshot> lastByViewer = new HashMap<>();

  /**
   * Returns the last snapshot sent to the viewer, or the empty snapshot if none was sent yet.
   *
   * @param viewer the viewer to look up; never {@code null}
   * @return the last sent snapshot, or {@link TabSnapshot#empty(ViewerId)}
   */
  public TabSnapshot lastFor(ViewerId viewer) {
    Objects.requireNonNull(viewer, "viewer");
    return lastByViewer.getOrDefault(viewer, TabSnapshot.empty(viewer));
  }

  /**
   * Records {@code snapshot} as the latest state sent to its viewer.
   *
   * @param snapshot the snapshot just rendered; never {@code null}
   */
  public void store(TabSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot");
    lastByViewer.put(snapshot.viewer(), snapshot);
  }

  /**
   * Discards the last snapshot remembered for a viewer.
   *
   * @param viewer the viewer to forget; never {@code null}
   */
  @Override
  public void forget(ViewerId viewer) {
    lastByViewer.remove(viewer);
  }
}
