package com.hanielfialho.tablist.core.text;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;

/**
 * Turns a raw template into a parsed {@link Component} for a viewer, caching the result.
 *
 * <p>On a cache hit the stored component is returned directly. On a miss the placeholders are
 * resolved asynchronously, the result is parsed by MiniMessage exactly once and cached, and the
 * viewer is marked dirty when the resolution completes — so a slow resolver never blocks the flush,
 * and its result is picked up by the next flush cycle.
 */
public final class TextResolutionPipeline {

  private final ResolvedTextCache cache;
  private final TextResolver resolver;
  private final DirtyTracker dirty;

  /**
   * Creates the pipeline.
   *
   * @param cache the cache of parsed components; never {@code null}
   * @param resolver resolves and parses on a miss; never {@code null}
   * @param dirty marked when an asynchronous resolution completes; never {@code null}
   */
  public TextResolutionPipeline(
      ResolvedTextCache cache, TextResolver resolver, DirtyTracker dirty) {
    this.cache = Objects.requireNonNull(cache, "cache");
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.dirty = Objects.requireNonNull(dirty, "dirty");
  }

  /**
   * Resolves the template for the viewer, returning immediately with a cached or in-flight future.
   *
   * @param viewer the viewer to resolve for; never {@code null}
   * @param template the raw template; never {@code null}
   * @return the future of the parsed component; never blocks the caller
   */
  public CompletableFuture<Component> resolve(ViewerId viewer, String template) {
    return cache.get(new ResolvedTextKey(viewer, template), () -> load(viewer, template));
  }

  private CompletableFuture<Component> load(ViewerId viewer, String template) {
    return resolver
        .resolve(viewer, template)
        .whenComplete((component, error) -> dirty.markDirty(viewer));
  }
}
