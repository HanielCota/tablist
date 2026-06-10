package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Collection;

/**
 * Supplies the set of viewers currently tracked by the plugin.
 *
 * <p>The core uses it when an event affects everyone at once — most notably a configuration reload,
 * after which every viewer must be re-rendered. How the set is gathered (online players, a managed
 * registry, ...) is the adapter's concern.
 */
@FunctionalInterface
public interface ViewerDirectory {

  /**
   * Returns the viewers known right now.
   *
   * @return an immutable snapshot of the current viewers; never {@code null}
   */
  Collection<ViewerId> viewers();
}
