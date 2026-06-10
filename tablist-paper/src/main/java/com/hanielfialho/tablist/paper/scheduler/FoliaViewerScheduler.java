package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ScheduledTask;
import com.hanielfialho.tablist.core.port.ViewerScheduler;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Folia scheduling for one viewer: work runs on that player's region thread via their {@code
 * EntityScheduler}, the only thread allowed to touch their connection state.
 *
 * <p>If the player has logged out (or their region is unavailable) the task is dropped — there is
 * nothing to render and nothing to touch.
 */
public final class FoliaViewerScheduler implements ViewerScheduler {

  private final Plugin plugin;
  private final ViewerId viewer;

  /**
   * Creates a scheduler bound to one viewer.
   *
   * @param plugin the owning plugin; never {@code null}
   * @param viewer the viewer whose region runs the work; never {@code null}
   */
  public FoliaViewerScheduler(Plugin plugin, ViewerId viewer) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.viewer = Objects.requireNonNull(viewer, "viewer");
  }

  private static ScheduledTask wrap(
      io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduled) {
    if (scheduled == null) {
      return new SkippedTask();
    }
    return new FoliaScheduledTask(scheduled);
  }

  @Override
  public ScheduledTask schedule(Runnable task) {
    Player online = Bukkit.getPlayer(viewer.value());
    if (online == null) {
      return new SkippedTask();
    }
    return run(online, task);
  }

  @Override
  public ScheduledTask scheduleRepeating(Runnable task, long period) {
    Player online = Bukkit.getPlayer(viewer.value());
    if (online == null) {
      return new SkippedTask();
    }
    return runRepeating(online, task, period);
  }

  private ScheduledTask run(Player online, Runnable task) {
    try {
      return wrap(online.getScheduler().run(plugin, ignored -> task.run(), () -> {}));
    } catch (IllegalStateException regionUnavailable) {
      return new SkippedTask();
    }
  }

  private ScheduledTask runRepeating(Player online, Runnable task, long period) {
    try {
      return wrap(
          online
              .getScheduler()
              .runAtFixedRate(plugin, ignored -> task.run(), () -> {}, period, period));
    } catch (IllegalStateException regionUnavailable) {
      return new SkippedTask();
    }
  }
}
