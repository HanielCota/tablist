package com.hanielfialho.tablist.core.config;

import java.util.Objects;

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

  /**
   * Creates a reloader over its collaborators.
   *
   * @param loader reads and validates the YAML; never {@code null}
   * @param active holds the configuration in effect; never {@code null}
   * @param dirtyAllViewers marks every viewer dirty on success; never {@code null}
   */
  public ConfigReloader(ConfigLoader loader, ActiveConfig active, DirtyAllViewers dirtyAllViewers) {
    this.loader = Objects.requireNonNull(loader, "loader");
    this.active = Objects.requireNonNull(active, "active");
    this.dirtyAllViewers = Objects.requireNonNull(dirtyAllViewers, "dirtyAllViewers");
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
    return ReloadResult.ok();
  }
}
