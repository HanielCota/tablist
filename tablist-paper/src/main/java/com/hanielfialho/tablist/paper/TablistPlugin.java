package com.hanielfialho.tablist.paper;

import com.hanielfialho.tablist.core.config.DirtyAllViewers;
import com.hanielfialho.tablist.core.config.Heartbeat;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.GroupWeightResolver;
import com.hanielfialho.tablist.core.port.ScheduledTask;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.port.TabRenderer;
import com.hanielfialho.tablist.core.port.TabScheduler;
import com.hanielfialho.tablist.core.state.GroupChangeWatcher;
import com.hanielfialho.tablist.core.state.MeteredTabRenderer;
import com.hanielfialho.tablist.core.state.TabFlusher;
import com.hanielfialho.tablist.core.state.TogglingSnapshotSource;
import com.hanielfialho.tablist.core.state.ViewerLifecycle;
import com.hanielfialho.tablist.paper.bootstrap.TablistServices;
import com.hanielfialho.tablist.paper.listener.JoinQuitListener;
import com.hanielfialho.tablist.paper.render.ComponentResolver;
import com.hanielfialho.tablist.paper.render.ListSorterFactory;
import com.hanielfialho.tablist.paper.render.PaperTabRenderer;
import com.hanielfialho.tablist.paper.scheduler.TabSchedulerFactory;
import com.hanielfialho.tablist.paper.snapshot.PaperSnapshotSource;
import com.hanielfialho.tablist.paper.sort.PermissionGroupWeightResolver;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper entry point for Tablist.
 *
 * <p>It builds the shared {@link TablistServices} graph and starts the flush loop through the
 * Folia-aware {@link TabScheduler}. The same graph is handed to the command bootstrap (via {@link
 * #services()}), so {@code /tablist reload}, {@code status} and {@code toggle} act on exactly the
 * objects the running loop renders. No dependency-injection framework, no static singletons.
 *
 * <p>Not {@code final}: Paper's plugin loader may subclass the plugin main.
 */
public class TablistPlugin extends JavaPlugin {

  private static final long FLUSH_PERIOD_TICKS = 1L;
  private static final long GROUP_CHECK_PERIOD_TICKS = 20L;

  private TablistServices services;
  private ScheduledTask flushTask;
  private ScheduledTask groupTask;

  @Override
  public void onEnable() {
    saveBundledConfig();
    this.services =
        TablistServices.create(
            getDataFolder().toPath().resolve("config.yml"), getLogger()::warning);

    TabScheduler scheduler = TabSchedulerFactory.create(this);
    GroupWeightResolver groups = new PermissionGroupWeightResolver(services.config());
    GroupChangeWatcher groupWatcher =
        new GroupChangeWatcher(groups, services.registry(), services.dirty());

    this.flushTask = startFlushLoop(services, scheduler, groups);
    this.groupTask =
        scheduler.scheduleGlobalRepeating(groupWatcher::check, GROUP_CHECK_PERIOD_TICKS);
    registerViewers(services, groupWatcher);
    getLogger().info("Tablist enabled.");
  }

  @Override
  public void onDisable() {
    if (groupTask != null) {
      groupTask.cancel();
    }
    if (flushTask != null) {
      flushTask.cancel();
    }
    getLogger().info("Tablist disabled.");
  }

  /**
   * Returns the shared service graph, so the command bootstrap can inject it into the commands.
   *
   * @return the services, or {@code null} before {@link #onEnable()} has run
   */
  public TablistServices services() {
    return services;
  }

  private ScheduledTask startFlushLoop(
      TablistServices services, TabScheduler scheduler, GroupWeightResolver groups) {
    TabRenderer renderer =
        new MeteredTabRenderer(
            new PaperTabRenderer(
                new ComponentResolver(services.pipeline()), ListSorterFactory.create(), scheduler),
            services.metrics());
    TabFlusher flusher = new TabFlusher(services.dirty(), renderer, services.store());
    SnapshotSource source =
        new TogglingSnapshotSource(
            new PaperSnapshotSource(
                services.config(), services.registry(), services.clock(), groups),
            services.toggle());
    Heartbeat heartbeat =
        new Heartbeat(services.clock(), new DirtyAllViewers(services.dirty(), services.registry()));
    FlushLoop loop = new FlushLoop(heartbeat, flusher, source);

    return scheduler.scheduleGlobalRepeating(loop::run, FLUSH_PERIOD_TICKS);
  }

  private void registerViewers(TablistServices services, GroupChangeWatcher groupWatcher) {
    ViewerLifecycle lifecycle =
        new ViewerLifecycle(
            services.registry(),
            services.dirty(),
            List.of(services.store(), services.cache(), services.toggle(), groupWatcher));
    getServer().getPluginManager().registerEvents(new JoinQuitListener(lifecycle), this);
    getServer()
        .getOnlinePlayers()
        .forEach(player -> lifecycle.join(new ViewerId(player.getUniqueId())));
  }

  // Renamed from saveDefaultConfig(): that name illegally narrowed the public
  // JavaPlugin#saveDefaultConfig() to private. This bundled-config variant only
  // writes the file when one is actually shaded in, avoiding the missing-resource throw.
  private void saveBundledConfig() {
    if (getResource("config.yml") != null) {
      saveResource("config.yml", false);
    }
  }
}
