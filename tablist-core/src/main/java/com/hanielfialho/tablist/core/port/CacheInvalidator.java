package com.hanielfialho.tablist.core.port;

/**
 * Drops every cached value the core holds (resolved placeholders, composed names, ...).
 *
 * <p>The core owns no cache directly; it depends on this seam so a reload can force a full
 * recompute. Implementations are backed by the real caches (for example Caffeine) supplied by the
 * application.
 */
@FunctionalInterface
public interface CacheInvalidator {

  /** Invalidates all cached entries so the next render recomputes them. */
  void invalidateAll();
}
