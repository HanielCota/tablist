package com.hanielfialho.tablist.paper.placeholder;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Resolves placeholders through PlaceholderAPI.
 *
 * <p>This class is the only place that touches the {@code PlaceholderAPI} type, and it is
 * instantiated solely when PlaceholderAPI is installed (see {@link PaperPlaceholderResolvers}). The
 * core module never references it, so Tablist compiles and runs without PlaceholderAPI on the
 * classpath.
 *
 * <p>A template is handed verbatim to {@link PlaceholderAPI#setPlaceholders(Player, String)}; any
 * tokens PlaceholderAPI does not recognise are left untouched. An offline or unknown viewer yields
 * the input unchanged, which lets the {@link
 * com.hanielfialho.tablist.core.text.CompositePlaceholderResolver composite} fall through cleanly.
 */
public final class PapiPlaceholderResolver implements PlaceholderResolver {

  @Override
  public CompletableFuture<String> resolve(ViewerId viewer, String raw) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(raw, "raw");
    Player player = Bukkit.getPlayer(viewer.value());
    if (player == null) {
      return CompletableFuture.completedFuture(raw);
    }
    return CompletableFuture.completedFuture(PlaceholderAPI.setPlaceholders(player, raw));
  }
}
