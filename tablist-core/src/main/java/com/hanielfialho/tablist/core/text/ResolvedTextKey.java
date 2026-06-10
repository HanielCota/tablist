package com.hanielfialho.tablist.core.text;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Objects;

/**
 * The cache key for a resolved piece of text: the viewer it was resolved for and the raw template.
 *
 * <p>The key is intentionally {@code (ViewerId, String)} and never holds a {@code Player}, so cache
 * entries cannot pin platform objects in memory.
 *
 * @param viewer the viewer the text is resolved for; never {@code null}
 * @param template the raw, unresolved template; never {@code null}
 */
public record ResolvedTextKey(ViewerId viewer, String template) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code viewer} or {@code template} is {@code null}
   */
  public ResolvedTextKey {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(template, "template");
  }
}
