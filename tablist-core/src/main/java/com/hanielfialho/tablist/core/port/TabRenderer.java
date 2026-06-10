package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.state.TabDiff;

/**
 * The platform-facing port that pushes tab-list changes to a viewer — "the thing that talks to
 * Paper".
 *
 * <p>The renderer is given a {@link TabDiff}, not a full snapshot, because the flusher only ever
 * asks it to apply <em>what changed</em>. A diff cleanly separates entry changes from header/footer
 * changes, so an implementation can send exactly the packets that are needed and nothing more.
 */
@FunctionalInterface
public interface TabRenderer {

  /**
   * Applies the given change set to the viewer's tab list.
   *
   * <p>The flusher never calls this with an {@linkplain TabDiff#isEmpty() empty} diff.
   *
   * @param viewer the player whose tab list should change; never {@code null}
   * @param diff the entries to upsert, the targets to remove and the header/footer to re-send;
   *     never {@code null}
   */
  void render(ViewerId viewer, TabDiff diff);
}
