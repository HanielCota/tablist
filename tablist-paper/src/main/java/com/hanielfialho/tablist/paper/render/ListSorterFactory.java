package com.hanielfialho.tablist.paper.render;

import org.bukkit.Bukkit;

/**
 * Chooses the {@link ListSorter} the running server supports.
 *
 * <p>Prefers Paper's native list ordering and falls back to scoreboard teams only when the native
 * API is absent (older servers), probed once via reflection.
 */
public final class ListSorterFactory {

  private static final boolean NATIVE = detectNativeOrder();

  private ListSorterFactory() {}

  /**
   * Creates the best available sorter.
   *
   * @return a native sorter when supported, otherwise a team-based sorter
   */
  public static ListSorter create() {
    if (NATIVE) {
      return new NativeListSorter();
    }
    return new TeamListSorter(Bukkit.getScoreboardManager().getMainScoreboard());
  }

  private static boolean detectNativeOrder() {
    try {
      org.bukkit.entity.Player.class.getMethod("setPlayerListOrder", int.class);
      return true;
    } catch (NoSuchMethodException noNativeOrder) {
      return false;
    }
  }
}
