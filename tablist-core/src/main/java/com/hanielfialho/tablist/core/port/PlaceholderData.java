package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;

/**
 * Supplies the live values the built-in placeholders need, decoupled from any {@code Player}.
 *
 * <p>The core resolves {@code %player_name%}, {@code %online%} and {@code %ping%} purely from this
 * port; the Paper adapter implements it by reading the corresponding Bukkit state.
 */
public interface PlaceholderData {

  /**
   * Returns the display name of the given viewer.
   *
   * @param viewer the viewer to look up; never {@code null}
   * @return the viewer's name; never {@code null}
   */
  String playerName(ViewerId viewer);

  /**
   * Returns the number of players currently online.
   *
   * @return the online count, never negative
   */
  int onlineCount();

  /**
   * Returns the round-trip latency of the given viewer, in milliseconds.
   *
   * @param viewer the viewer to look up; never {@code null}
   * @return the viewer's ping in milliseconds
   */
  int ping(ViewerId viewer);
}
