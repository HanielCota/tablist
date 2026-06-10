package com.hanielfialho.tablist.core.config;

/**
 * Raised when the configuration cannot be parsed or fails validation.
 *
 * <p>Its message is meant to be shown to a server administrator: it names the offending line (for a
 * YAML syntax error) or the node path (for an invalid value), as reported by Configurate.
 */
public final class ConfigException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a configuration exception.
   *
   * @param message the human-readable description; never {@code null}
   * @param cause the underlying Configurate failure; never {@code null}
   */
  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
