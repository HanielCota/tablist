package com.hanielfialho.tablist.core.config;

import java.util.Objects;

/**
 * The outcome of a reload attempt: whether the new configuration was applied and a message to show.
 *
 * @param success {@code true} if the new configuration is now active
 * @param message a human-readable summary or the validation error; never {@code null}
 */
public record ReloadResult(boolean success, String message) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code message} is {@code null}
   */
  public ReloadResult {
    Objects.requireNonNull(message, "message");
  }

  /**
   * Returns a successful result.
   *
   * @return an applied result
   */
  public static ReloadResult ok() {
    return new ReloadResult(true, "Configuration reloaded.");
  }

  /**
   * Returns a failed result that kept the previous configuration.
   *
   * @param message the readable error
   * @return a failed result
   */
  public static ReloadResult error(String message) {
    return new ReloadResult(false, message);
  }

  /**
   * Tells whether the reload failed and the previous configuration is still active.
   *
   * @return {@code true} when nothing changed
   */
  public boolean failed() {
    return !success;
  }
}
