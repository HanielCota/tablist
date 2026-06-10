package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.port.TabScheduler;
import org.bukkit.plugin.Plugin;

/**
 * Chooses the right {@link TabScheduler} for the running server.
 *
 * <p>Folia is detected once, at class-load time, by probing for its {@code RegionizedServer} class
 * — the same runtime check MenuFramework uses. The choice is fixed for the life of the server.
 */
public final class TabSchedulerFactory {

  private static final boolean FOLIA = detectFolia();

  private TabSchedulerFactory() {}

  /**
   * Creates the scheduler matching the current platform.
   *
   * @param plugin the owning plugin; never {@code null}
   * @return a Folia scheduler on Folia, otherwise a Paper scheduler
   */
  public static TabScheduler create(Plugin plugin) {
    if (FOLIA) {
      return new FoliaTabScheduler(plugin);
    }
    return new PaperTabScheduler(plugin);
  }

  private static boolean detectFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException notFolia) {
      return false;
    }
  }
}
