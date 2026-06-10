package com.hanielfialho.tablist.core.text;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.CacheInvalidator;
import com.hanielfialho.tablist.core.state.ViewerScoped;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;

/**
 * A Caffeine-backed cache of parsed {@link Component}s, keyed by {@link ResolvedTextKey}.
 *
 * <p>It is an asynchronous cache: an in-flight resolution is stored as a future, so concurrent
 * requests for the same key share a single computation and the MiniMessage parse runs only once per
 * key while the entry lives. Entries expire {@code expireAfterWrite} after they are written (driven
 * by {@code TabConfig.refresh}) and the cache is bounded to {@value #MAXIMUM_SIZE} entries.
 */
public final class ResolvedTextCache implements CacheInvalidator, ViewerScoped {

  private static final long MAXIMUM_SIZE = 10_000L;
  private final AsyncCache<ResolvedTextKey, Component> cache;

  private ResolvedTextCache(AsyncCache<ResolvedTextKey, Component> cache) {
    this.cache = cache;
  }

  /**
   * Creates a cache that expires entries after the given duration.
   *
   * @param expireAfterWrite how long a parsed component stays cached; never {@code null}
   * @return a new cache
   */
  public static ResolvedTextCache create(Duration expireAfterWrite) {
    return create(expireAfterWrite, Ticker.systemTicker());
  }

  static ResolvedTextCache create(Duration expireAfterWrite, Ticker ticker) {
    Objects.requireNonNull(expireAfterWrite, "expireAfterWrite");
    Objects.requireNonNull(ticker, "ticker");
    return new ResolvedTextCache(
        Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(expireAfterWrite)
            .ticker(ticker)
            .recordStats()
            .buildAsync());
  }

  /**
   * Returns the cached component for the key, computing it through {@code loader} on a miss.
   *
   * <p>The loader runs at most once per key while the entry is cached; a hit returns the stored
   * (possibly still in-flight) future without invoking it.
   *
   * @param key the cache key; never {@code null}
   * @param loader computes the component on a miss; never {@code null}
   * @return the future of the resolved component
   */
  public CompletableFuture<Component> get(
      ResolvedTextKey key, Supplier<CompletableFuture<Component>> loader) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(loader, "loader");
    return cache.get(key, (k, executor) -> loader.get());
  }

  @Override
  public void invalidateAll() {
    cache.synchronous().invalidateAll();
  }

  /**
   * Returns the cache hit-rate since creation, between {@code 0.0} and {@code 1.0}.
   *
   * @return the ratio of hits to lookups, or {@code 0.0} when nothing has been requested yet
   */
  public double hitRate() {
    return cache.synchronous().stats().hitRate();
  }

  /**
   * Invalidates every cached entry belonging to the given viewer.
   *
   * @param viewer the viewer whose entries to drop; never {@code null}
   */
  @Override
  public void forget(ViewerId viewer) {
    cache.synchronous().asMap().keySet().removeIf(key -> key.viewer().equals(viewer));
  }
}
