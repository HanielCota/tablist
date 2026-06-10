package com.hanielfialho.tablist.core.text;

import java.util.List;

/**
 * The catalogue of placeholders Tablist resolves on its own, used to power the {@code /tablist
 * placeholders} help listing. These mirror exactly what {@link BuiltinPlaceholderResolver}
 * substitutes, so an admin can discover them without reading the source or guessing.
 *
 * <p>This is a presentation-only catalogue: the actual substitution still lives in {@link
 * BuiltinPlaceholderResolver}. Keep the two in sync when adding a built-in placeholder.
 */
public final class PlaceholderCatalog {

  /**
   * One discoverable placeholder: the literal token an admin writes and a one-line description.
   *
   * @param token the placeholder as written in the config, e.g. {@code %player_name%}
   * @param description a short, human-readable explanation
   */
  public record Entry(String token, String description) {}

  private static final List<Entry> BUILTINS =
      List.of(
          new Entry("%player_name%", "the viewer's own name"),
          new Entry("%online%", "the number of players currently online"),
          new Entry("%ping%", "the viewer's latency, in milliseconds"));

  private PlaceholderCatalog() {}

  /**
   * Returns the built-in placeholders, in display order.
   *
   * @return an immutable list of the built-in placeholder entries
   */
  public static List<Entry> builtins() {
    return BUILTINS;
  }
}
