package com.hanielfialho.tablist.core.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * How often placeholder values are re-resolved.
 *
 * @param placeholderRefreshSeconds the interval, in seconds, between placeholder refreshes; at
 *     least {@code 1}
 */
@ConfigSerializable
public record Refresh(@Setting("placeholder-refresh-seconds") int placeholderRefreshSeconds) {

  /**
   * Canonical constructor.
   *
   * @throws IllegalArgumentException if {@code placeholderRefreshSeconds < 1}
   */
  public Refresh {
    requirePositive(placeholderRefreshSeconds);
  }

  private static void requirePositive(int seconds) {
    if (seconds < 1) {
      throw new IllegalArgumentException("placeholder-refresh-seconds must be >= 1");
    }
  }
}
