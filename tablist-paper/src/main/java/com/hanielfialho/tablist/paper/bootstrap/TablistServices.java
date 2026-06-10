package com.hanielfialho.tablist.paper.bootstrap;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.ConfigException;
import com.hanielfialho.tablist.core.config.ConfigLoader;
import com.hanielfialho.tablist.core.config.ConfigReloader;
import com.hanielfialho.tablist.core.config.ConfigValidator;
import com.hanielfialho.tablist.core.config.DirtyAllViewers;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import com.hanielfialho.tablist.core.state.AnimationClock;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import com.hanielfialho.tablist.core.state.SnapshotStore;
import com.hanielfialho.tablist.core.state.TablistToggle;
import com.hanielfialho.tablist.core.state.UpdateMetrics;
import com.hanielfialho.tablist.core.state.ViewerRegistry;
import com.hanielfialho.tablist.core.status.StatusReporter;
import com.hanielfialho.tablist.core.status.StatusSources;
import com.hanielfialho.tablist.core.text.ResolvedTextCache;
import com.hanielfialho.tablist.core.text.TextResolutionPipeline;
import com.hanielfialho.tablist.core.text.TextResolver;
import com.hanielfialho.tablist.paper.placeholder.PaperPlaceholderResolvers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * The shared object graph built once and used by both the flush loop (in {@link
 * com.hanielfialho.tablist.paper.TablistPlugin}) and the commands (resolved lazily through the
 * bootstrap). Holding one instance of each collaborator is what lets a {@code /tablist reload} or
 * {@code toggle} affect what the running flush loop renders.
 *
 * @param config the active configuration behind an atomic reference
 * @param cache the resolved-text cache
 * @param dirty the dirty tracker
 * @param store the per-viewer last-snapshot store
 * @param registry the active viewer registry
 * @param toggle the per-viewer custom/vanilla toggle
 * @param metrics the rolling update counter
 * @param clock the shared animation clock
 * @param pipeline the text resolution pipeline
 * @param reloader the configuration reloader
 * @param statusReporter the runtime status reporter
 */
public record TablistServices(
    ActiveConfig config,
    ResolvedTextCache cache,
    DirtyTracker dirty,
    SnapshotStore store,
    ViewerRegistry registry,
    TablistToggle toggle,
    UpdateMetrics metrics,
    AnimationClock clock,
    TextResolutionPipeline pipeline,
    ConfigReloader reloader,
    StatusReporter statusReporter) {

  /**
   * Builds the whole shared graph, loading the configuration from {@code configFile} (falling back
   * to defaults if it is missing or invalid).
   *
   * @param configFile the path to {@code config.yml}; need not exist
   * @return the wired services
   */
  public static TablistServices create(Path configFile) {
    return create(configFile, warning -> {});
  }

  /**
   * Builds the whole shared graph, additionally reporting non-fatal configuration warnings (from
   * the initial load and from every later {@code /tablist reload}) to {@code warningSink}.
   *
   * @param configFile the path to {@code config.yml}; need not exist
   * @param warningSink receives each warning line, e.g. the plugin logger; never {@code null}
   * @return the wired services
   */
  public static TablistServices create(Path configFile, Consumer<String> warningSink) {
    ConfigLoader loader = new ConfigLoader(configFile);
    TabConfig initial = loadOrDefault(loader);

    ConfigValidator validator = new ConfigValidator();
    validator.report(initial, warningSink);

    ResolvedTextCache cache =
        ResolvedTextCache.create(Duration.ofSeconds(initial.refresh().placeholderRefreshSeconds()));
    ActiveConfig config = new ActiveConfig(initial, cache);

    DirtyTracker dirty = new DirtyTracker();
    SnapshotStore store = new SnapshotStore();
    ViewerRegistry registry = new ViewerRegistry();
    TablistToggle toggle = new TablistToggle();
    UpdateMetrics metrics = new UpdateMetrics();
    AnimationClock clock = new AnimationClock();

    PlaceholderResolver placeholders = PaperPlaceholderResolvers.create();
    TextResolutionPipeline pipeline =
        new TextResolutionPipeline(
            cache, new TextResolver(placeholders, MiniMessage.miniMessage()), dirty);

    ConfigReloader reloader =
        new ConfigReloader(
            loader, config, new DirtyAllViewers(dirty, registry), validator, warningSink);
    StatusReporter statusReporter =
        new StatusReporter(new StatusSources(registry, cache, metrics, clock, config));

    return new TablistServices(
        config,
        cache,
        dirty,
        store,
        registry,
        toggle,
        metrics,
        clock,
        pipeline,
        reloader,
        statusReporter);
  }

  private static TabConfig loadOrDefault(ConfigLoader loader) {
    try {
      return loader.load();
    } catch (ConfigException invalid) {
      return TabConfig.defaults();
    }
  }
}
