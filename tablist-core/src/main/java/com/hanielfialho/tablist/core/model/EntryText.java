package com.hanielfialho.tablist.core.model;

import java.util.Objects;

/**
 * The renderable parts of a single tab-list row: prefix, name and suffix.
 *
 * <p>Grouping the three renderables into one value keeps {@link TabEntry} small and gives the row's
 * text a single identity to compare during diffing.
 *
 * @param prefix the text shown before the name; never {@code null}
 * @param name the player's display name; never {@code null}
 * @param suffix the text shown after the name; never {@code null}
 */
public record EntryText(Renderable prefix, Renderable name, Renderable suffix) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if any component is {@code null}
   */
  public EntryText {
    Objects.requireNonNull(prefix, "prefix");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(suffix, "suffix");
  }

  /**
   * Builds an {@code EntryText} from raw template strings.
   *
   * @param prefix the prefix template
   * @param name the name template
   * @param suffix the suffix template
   * @return a new {@code EntryText} wrapping the given templates
   */
  public static EntryText of(String prefix, String name, String suffix) {
    return new EntryText(new Renderable(prefix), new Renderable(name), new Renderable(suffix));
  }
}
