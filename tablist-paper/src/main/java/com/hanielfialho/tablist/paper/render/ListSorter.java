package com.hanielfialho.tablist.paper.render;

import org.bukkit.entity.Player;

/**
 * Applies a tab-list sort order to a player, hiding whether it is done with Paper's native list
 * ordering or with scoreboard teams.
 */
public interface ListSorter {

  /**
   * Places the player at the given relative order in the tab list.
   *
   * @param target the player to order; never {@code null}
   * @param order the relative order; higher values are shown first
   */
  void apply(Player target, int order);

  /**
   * Removes any ordering this sorter applied to the player.
   *
   * @param target the player to reset; never {@code null}
   */
  void clear(Player target);
}
