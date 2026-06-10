package com.hanielfialho.tablist.core.text;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderData;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Resolves the built-in placeholders {@code %player_name%}, {@code %online%} and {@code %ping%}.
 *
 * <p>All values come from the {@link PlaceholderData} port, so this resolver stays free of any
 * {@code Player} reference. A template containing none of these placeholders is returned unchanged,
 * which lets a {@link CompositePlaceholderResolver} fall through to the next resolver.
 */
public final class BuiltinPlaceholderResolver implements PlaceholderResolver {

  private final PlaceholderData data;

  /**
   * Creates the built-in resolver.
   *
   * @param data the source of player name, online count and ping; never {@code null}
   */
  public BuiltinPlaceholderResolver(PlaceholderData data) {
    this.data = Objects.requireNonNull(data, "data");
  }

  @Override
  public CompletableFuture<String> resolve(ViewerId viewer, String raw) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(raw, "raw");
    return CompletableFuture.completedFuture(substitute(viewer, raw));
  }

  private String substitute(ViewerId viewer, String raw) {
    var playerName = data.playerName(viewer);
    var online = Integer.toString(data.onlineCount());
    var ping = Integer.toString(data.ping(viewer));

    return raw.replace("%player_name%", playerName)
        .replace("%online%", online)
        .replace("%ping%", ping);
  }
}
