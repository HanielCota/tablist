package com.hanielfialho.tablist.core.model;

import java.util.Objects;

/**
 * A piece of renderable text, possibly containing placeholders, kept as an unresolved template.
 *
 * <p>The core never interprets the template: it carries it verbatim. Placeholder resolution and the
 * conversion into a rich component happen in the platform adapter, just before rendering.
 *
 * @param template the raw template text; never {@code null}, may be empty
 */
public record Renderable(String template) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code template} is {@code null}
   */
  public Renderable {
    Objects.requireNonNull(template, "template");
  }

  /**
   * Returns the shared empty renderable.
   *
   * @return a renderable whose template is the empty string
   */
  public static Renderable empty() {
    return new Renderable("");
  }

  /**
   * Tells whether this renderable carries no text.
   *
   * @return {@code true} if the template is empty
   */
  public boolean isEmpty() {
    return template.isEmpty();
  }
}
