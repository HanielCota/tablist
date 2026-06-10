package com.hanielfialho.tablist.paper.placeholder;

import com.hanielfialho.tablist.core.port.PlaceholderData;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import com.hanielfialho.tablist.core.text.BuiltinPlaceholderResolver;
import com.hanielfialho.tablist.core.text.CompositePlaceholderResolver;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;

/**
 * Builds the placeholder resolver chain for the running server.
 *
 * <p>The built-in resolver is always first, so Tablist's own placeholders ({@code %player_name%},
 * {@code %online%}, {@code %ping%}) resolve identically whether or not PlaceholderAPI is installed.
 * The {@link PapiPlaceholderResolver} is appended <strong>only</strong> when PlaceholderAPI is
 * present, and it is the single class that references the {@code PlaceholderAPI} type — so it is
 * never loaded on a server without it.
 */
public final class PaperPlaceholderResolvers {

  private static final String PLACEHOLDER_API = "PlaceholderAPI";

  private PaperPlaceholderResolvers() {}

  /**
   * Builds the resolver chain for the live server, detecting PlaceholderAPI through Bukkit.
   *
   * @return the composite resolver to install in the pipeline
   */
  public static PlaceholderResolver create() {
    return create(new PaperPlaceholderData(), placeholderApiPresent());
  }

  /**
   * Builds the resolver chain from explicit inputs. Package-private so a test can exercise the
   * branching without a running server.
   *
   * @param data the source of the built-in placeholder values; never {@code null}
   * @param placeholderApiPresent whether PlaceholderAPI is installed
   * @return the composite resolver
   */
  static PlaceholderResolver create(PlaceholderData data, boolean placeholderApiPresent) {
    List<PlaceholderResolver> chain = new ArrayList<>();
    chain.add(new BuiltinPlaceholderResolver(data));
    if (placeholderApiPresent) {
      chain.add(new PapiPlaceholderResolver());
    }
    return new CompositePlaceholderResolver(chain);
  }

  /**
   * Returns whether PlaceholderAPI is installed on the running server.
   *
   * @return {@code true} when the PlaceholderAPI plugin is present
   */
  public static boolean placeholderApiPresent() {
    return Bukkit.getPluginManager().getPlugin(PLACEHOLDER_API) != null;
  }
}
