package com.hanielfialho.tablist.paper.render;

import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.Renderable;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.text.TextResolutionPipeline;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;

/**
 * Resolves a template into a {@link Component} without ever blocking the render thread.
 *
 * <p>It reads the {@link TextResolutionPipeline} non-blockingly: a cache hit yields the parsed
 * component immediately, while a miss yields {@link Component#empty()} for now and lets the
 * pipeline resolve asynchronously and mark the viewer dirty, so the next flush shows the finished
 * text.
 */
public final class ComponentResolver {

  private final TextResolutionPipeline pipeline;

  /**
   * Creates a resolver over the given pipeline.
   *
   * @param pipeline the resolution pipeline; never {@code null}
   */
  public ComponentResolver(TextResolutionPipeline pipeline) {
    this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
  }

  /**
   * Resolves a single template for the given placeholder context.
   *
   * @param context the viewer the placeholders are evaluated for; never {@code null}
   * @param template the template to resolve; never {@code null}
   * @return the parsed component, or {@link Component#empty()} if not yet available
   */
  public Component resolve(ViewerId context, Renderable template) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(template, "template");
    CompletableFuture<Component> future = pipeline.resolve(context, template.template());
    if (!future.isDone() || future.isCompletedExceptionally()) {
      return Component.empty();
    }
    return future.join();
  }

  /**
   * Composes a row's name from its prefix, name and suffix, resolved for the target player.
   *
   * @param target the player the row represents; never {@code null}
   * @param text the row's renderable parts; never {@code null}
   * @return the composed component
   */
  public Component name(ViewerId target, EntryText text) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(text, "text");
    return resolve(target, text.prefix())
        .append(resolve(target, text.name()))
        .append(resolve(target, text.suffix()));
  }
}
