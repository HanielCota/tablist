package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ScheduledTask;
import com.hanielfialho.tablist.core.port.TabScheduler;
import com.hanielfialho.tablist.core.port.ViewerScheduler;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * The Paper scheduling strategy: every interaction runs on the global main thread, which owns every
 * player on a non-Folia server.
 */
public final class PaperTabScheduler implements TabScheduler {

  private final Plugin plugin;

  /**
   * Creates a Paper scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never {@code null}
   */
  public PaperTabScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public ViewerScheduler forViewer(ViewerId viewer) {
    return new PaperViewerScheduler(plugin);
  }

  @Override
  public Executor global() {
    return command -> Bukkit.getScheduler().runTask(plugin, command);
  }

  @Override
  public ScheduledTask scheduleGlobalRepeating(Runnable task, long period) {
    return new BukkitScheduledTask(
        Bukkit.getScheduler().runTaskTimer(plugin, task, period, period));
  }
}
