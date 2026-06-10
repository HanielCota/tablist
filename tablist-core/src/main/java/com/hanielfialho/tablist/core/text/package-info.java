/**
 * The text resolution pipeline: placeholder resolution, MiniMessage parsing and caching.
 *
 * <p>{@link com.hanielfialho.tablist.core.text.TextResolutionPipeline TextResolutionPipeline} turns
 * a raw template into a parsed {@link net.kyori.adventure.text.Component} for a viewer. A {@link
 * com.hanielfialho.tablist.core.text.ResolvedTextCache ResolvedTextCache} (Caffeine) guarantees the
 * MiniMessage parse happens at most once per {@code (viewer, template)} within the cache window,
 * and {@link com.hanielfialho.tablist.core.text.CompositePlaceholderResolver
 * CompositePlaceholderResolver} composes ordered resolvers. Adventure is platform-agnostic, so this
 * stays free of Bukkit/Paper.
 */
package com.hanielfialho.tablist.core.text;
