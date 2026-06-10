package com.hanielfialho.tablist.paper.render;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Fallback sorter for servers without the native list-order API: scoreboard teams whose names
 * encode the weight, so the client's alphabetical team ordering yields the desired order.
 *
 * <p>A higher order maps to a lexicographically smaller team name, which the vanilla client shows
 * first. Teams are created on demand on the main scoreboard and prefixed with {@code tablist_} so
 * they can be told apart from teams owned by other plugins.
 */
public final class TeamListSorter implements ListSorter {

  private static final String PREFIX = "tablist_";

  private final Scoreboard scoreboard;

  /**
   * Creates a team-based sorter on the given scoreboard.
   *
   * @param scoreboard the scoreboard to manage teams on; never {@code null}
   */
  public TeamListSorter(Scoreboard scoreboard) {
    this.scoreboard = Objects.requireNonNull(scoreboard, "scoreboard");
  }

  @Override
  public void apply(Player target, int order) {
    teamFor(order).addEntry(target.getName());
  }

  @Override
  public void clear(Player target) {
    Team team = scoreboard.getEntryTeam(target.getName());
    if (team == null || !team.getName().startsWith(PREFIX)) {
      return;
    }
    team.removeEntry(target.getName());
  }

  private Team teamFor(int order) {
    var weight = Integer.MAX_VALUE - Math.max(0, order);
    var suffix = String.format("%010d", weight);
    var name = PREFIX + suffix;

    Team existing = scoreboard.getTeam(name);
    if (existing != null) {
      return existing;
    }
    return scoreboard.registerNewTeam(name);
  }
}
