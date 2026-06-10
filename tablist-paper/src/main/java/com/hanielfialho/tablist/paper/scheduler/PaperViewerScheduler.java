package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.port.ScheduledTask;
import com.hanielfialho.tablist.core.port.ViewerScheduler;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Paper scheduling for one viewer: the global main thread, which owns every player on a non-Folia
 * server. The viewer binding is a formality here.
 */
public final class PaperViewerScheduler implements ViewerScheduler {

  private final Plugin plugin;

  /**
   * Creates a scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never {@code null}
   */
  public PaperViewerScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public ScheduledTask schedule(Runnable task) {
    return new BukkitScheduledTask(Bukkit.getScheduler().runTask(plugin, task));
  }

  @Override
  public ScheduledTask scheduleRepeating(Runnable task, long period) {
    return new BukkitScheduledTask(
        Bukkit.getScheduler().runTaskTimer(plugin, task, period, period));
  }
}
