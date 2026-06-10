package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.state.TabSnapshot;

/**
 * Builds the current, complete {@link TabSnapshot} for a viewer on demand.
 *
 * <p>The flusher asks this source for the up-to-date state of each dirty viewer when it runs. How
 * the snapshot is assembled (from online players, scoreboard teams, configuration, ...) is the
 * application's concern, not the flusher's.
 */
@FunctionalInterface
public interface SnapshotSource {

  /**
   * Returns the current snapshot for the given viewer.
   *
   * @param viewer the player whose tab state to build; never {@code null}
   * @return the complete current snapshot; never {@code null}
   */
  TabSnapshot snapshotOf(ViewerId viewer);
}
