package com.hanielfialho.tablist.core.config;

import com.hanielfialho.tablist.core.port.CacheInvalidator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the configuration currently in effect behind an atomic reference.
 *
 * <p>Readers see a consistent {@link TabConfig} at all times: a {@link #swap(TabConfig)} publishes
 * the new configuration in one atomic write and then drops the caches that were derived from the
 * old one, so no stale value can outlive the reload.
 */
public final class ActiveConfig {

  private final AtomicReference<TabConfig> current;
  private final CacheInvalidator caches;

  /**
   * Creates the holder with an initial configuration.
   *
   * @param initial the configuration to start with; never {@code null}
   * @param caches the caches to drop on every swap; never {@code null}
   */
  public ActiveConfig(TabConfig initial, CacheInvalidator caches) {
    this.current = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    this.caches = Objects.requireNonNull(caches, "caches");
  }

  /**
   * Returns the configuration currently in effect.
   *
   * @return the current configuration; never {@code null}
   */
  public TabConfig current() {
    return current.get();
  }

  /**
   * Atomically publishes a new configuration and invalidates the derived caches.
   *
   * @param next the configuration to activate; never {@code null}
   */
  public void swap(TabConfig next) {
    current.set(Objects.requireNonNull(next, "next"));
    caches.invalidateAll();
  }
}
