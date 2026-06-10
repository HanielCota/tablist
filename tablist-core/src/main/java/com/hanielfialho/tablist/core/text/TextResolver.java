package com.hanielfialho.tablist.core.text;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Resolves a template's placeholders and parses the result into a {@link Component}.
 *
 * <p>This is the one place the MiniMessage parse happens. It is invoked by the cache loader on a
 * miss, so each {@code (viewer, template)} is parsed once per cache window.
 */
public final class TextResolver {

  private final PlaceholderResolver placeholders;
  private final MiniMessage miniMessage;

  /**
   * Creates a resolver.
   *
   * @param placeholders resolves placeholder values asynchronously; never {@code null}
   * @param miniMessage parses the resolved string into a component; never {@code null}
   */
  public TextResolver(PlaceholderResolver placeholders, MiniMessage miniMessage) {
    this.placeholders = Objects.requireNonNull(placeholders, "placeholders");
    this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
  }

  /**
   * Resolves the placeholders in {@code template} for {@code viewer}, then parses the result.
   *
   * @param viewer the viewer to resolve for; never {@code null}
   * @param template the raw template; never {@code null}
   * @return a future of the parsed component
   */
  public CompletableFuture<Component> resolve(ViewerId viewer, String template) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(template, "template");
    return placeholders.resolve(viewer, template).thenApply(miniMessage::deserialize);
  }
}
