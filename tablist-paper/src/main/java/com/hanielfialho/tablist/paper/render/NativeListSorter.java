package com.hanielfialho.tablist.paper.render;

import org.bukkit.entity.Player;

/**
 * Sorts the tab list with Paper's native list-order API ({@code Player#setPlayerListOrder}),
 * available since the 1.21.2 player-list ordering feature. No scoreboard, no packets.
 */
public final class NativeListSorter implements ListSorter {

  @Override
  public void apply(Player target, int order) {
    target.setPlayerListOrder(Math.max(0, order));
  }

  @Override
  public void clear(Player target) {
    target.setPlayerListOrder(0);
  }
}
