package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;

/**
 * Resolves a viewer's tab-list sort order from the sorting group they belong to.
 *
 * <p>The core defines the contract; the adapter decides how a viewer maps to a group (on Paper, by
 * the {@code tablist.group.<name>} permission) and how the configured group weights become an
 * order. The returned value follows the {@link com.hanielfialho.tablist.core.model.TabEntry#order()
 * entry order} convention — higher is shown first — so it can be dropped straight into a row.
 */
@FunctionalInterface
public interface GroupWeightResolver {

  /**
   * Returns the sort order for the given player's group.
   *
   * @param viewer the player whose group decides the order; never {@code null}
   * @return the sort order; higher is shown first, never negative
   */
  int orderOf(ViewerId viewer);
}
