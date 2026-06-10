package com.hanielfialho.tablist.core.config;

import java.util.Objects;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * How a player's name is rendered in the tab list: a prefix and suffix wrapping the name.
 *
 * <p>All three are MiniMessage templates that may contain placeholders; the core keeps them
 * verbatim and the adapter resolves and renders them.
 *
 * @param prefix the text shown before the name; never {@code null}, may be empty
 * @param name the player's name template; never {@code null}, may be empty
 * @param suffix the text shown after the name; never {@code null}, may be empty
 */
@ConfigSerializable
public record NameFormat(String prefix, String name, String suffix) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if any component is {@code null}
   */
  public NameFormat {
    Objects.requireNonNull(prefix, "prefix");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(suffix, "suffix");
  }
}
