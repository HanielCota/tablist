package com.hanielfialho.tablist.core.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable identity of the player who sees a tab list.
 *
 * <p>This is the only handle the domain keeps for a player. A live {@code Player} reference is
 * <strong>never</strong> stored, which keeps the core free of platform state and lifecycle
 * concerns.
 *
 * @param value the player's unique id; never {@code null}
 */
public record ViewerId(UUID value) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public ViewerId {
    Objects.requireNonNull(value, "value");
  }
}
