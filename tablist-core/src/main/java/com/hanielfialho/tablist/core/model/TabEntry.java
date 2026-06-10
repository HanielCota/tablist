package com.hanielfialho.tablist.core.model;

import java.util.Objects;

/**
 * One row in a viewer's tab list: the player it represents, the text to show, and its sort order.
 *
 * <p>Two entries are equal when they target the same player and carry the same {@link EntryText}
 * and order. That equality is what lets the flusher detect, per row, whether anything actually
 * changed — including a change of position.
 *
 * @param target the player this row represents; never {@code null}
 * @param text the renderable prefix/name/suffix of the row; never {@code null}
 * @param order the relative sort order; higher values are shown first
 */
public record TabEntry(ViewerId target, EntryText text, int order) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code target} or {@code text} is {@code null}
   */
  public TabEntry {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(text, "text");
  }
}
