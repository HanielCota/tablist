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
 * The Folia scheduling strategy: each viewer is bound to their region thread, and global work runs
 * on the global region scheduler. No interaction ever runs on a thread that does not own its
 * target.
 */
public final class FoliaTabScheduler implements TabScheduler {

  private final Plugin plugin;

  /**
   * Creates a Folia scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never {@code null}
   */
  public FoliaTabScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public ViewerScheduler forViewer(ViewerId viewer) {
    return new FoliaViewerScheduler(plugin, viewer);
  }

  @Override
  public Executor global() {
    return command -> Bukkit.getGlobalRegionScheduler().execute(plugin, command);
  }

  @Override
  public ScheduledTask scheduleGlobalRepeating(Runnable task, long period) {
    return new FoliaScheduledTask(
        Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(plugin, ignored -> task.run(), period, period));
  }
}
