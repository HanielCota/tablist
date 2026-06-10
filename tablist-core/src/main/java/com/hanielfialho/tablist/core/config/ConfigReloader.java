package com.hanielfialho.tablist.core.config;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Reloads the configuration and, on success, swaps it in atomically and forces a full re-render.
 *
 * <p>A reload either fully succeeds or changes nothing. On success it (1) publishes the new {@link
 * TabConfig} through {@link ActiveConfig} — which also drops the derived caches — and (2) marks
 * every viewer dirty so the next flush rebuilds the whole tab list. On any parse or validation
 * failure the previous configuration stays active and the readable error is returned.
 */
public final class ConfigReloader {

  private final ConfigLoader loader;
  private final ActiveConfig active;
  private final DirtyAllViewers dirtyAllViewers;
  private final ConfigValidator validator;
  private final Consumer<String> warningSink;

  /**
   * Creates a reloader that swaps and re-renders but emits no warnings.
   *
   * @param loader reads and validates the YAML; never {@code null}
   * @param active holds the configuration in effect; never {@code null}
   * @param dirtyAllViewers marks every viewer dirty on success; never {@code null}
   */
  public ConfigReloader(ConfigLoader loader, ActiveConfig active, DirtyAllViewers dirtyAllViewers) {
    this(loader, active, dirtyAllViewers, new ConfigValidator(), warning -> {});
  }

  /**
   * Creates a reloader that also reports non-fatal configuration warnings on a successful reload.
   *
   * @param loader reads and validates the YAML; never {@code null}
   * @param active holds the configuration in effect; never {@code null}
   * @param dirtyAllViewers marks every viewer dirty on success; never {@code null}
   * @param validator inspects the freshly-loaded config for likely mistakes; never {@code null}
   * @param warningSink receives each warning line (e.g. a logger); never {@code null}
   */
  public ConfigReloader(
      ConfigLoader loader,
      ActiveConfig active,
      DirtyAllViewers dirtyAllViewers,
      ConfigValidator validator,
      Consumer<String> warningSink) {
    this.loader = Objects.requireNonNull(loader, "loader");
    this.active = Objects.requireNonNull(active, "active");
    this.dirtyAllViewers = Objects.requireNonNull(dirtyAllViewers, "dirtyAllViewers");
    this.validator = Objects.requireNonNull(validator, "validator");
    this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
  }

  /**
   * Attempts to reload the configuration from disk.
   *
   * @return a successful result, or a failed result carrying the readable error
   */
  public ReloadResult reload() {
    try {
      return apply(loader.load());
    } catch (ConfigException error) {
      return ReloadResult.error(error.getMessage());
    }
  }

  private ReloadResult apply(TabConfig parsed) {
    active.swap(parsed);
    dirtyAllViewers.markAll();
    validator.report(parsed, warningSink);
    return ReloadResult.ok();
  }
}
