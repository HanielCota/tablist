package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A first-class collection of {@link TabEntry} rows, keyed by their target, preserving insertion
 * order.
 *
 * <p>It owns the per-row diff logic so the flusher never has to reach into a raw map: {@link
 * #upsertsAgainst(TabEntries)} yields the rows that are new or changed, and {@link
 * #removalsAgainst(TabEntries)} yields the targets that disappeared.
 */
public final class TabEntries {

  private final Map<ViewerId, TabEntry> byTarget;

  private TabEntries(Map<ViewerId, TabEntry> byTarget) {
    this.byTarget = byTarget;
  }

  /**
   * Builds an immutable {@code TabEntries} from a collection of rows; later rows win on a duplicate
   * target.
   *
   * @param entries the rows to index; never {@code null}
   * @return an immutable, ordered {@code TabEntries}
   */
  public static TabEntries of(Collection<TabEntry> entries) {
    Objects.requireNonNull(entries, "entries");
    Map<ViewerId, TabEntry> indexed = new LinkedHashMap<>();
    entries.forEach(entry -> indexed.put(entry.target(), entry));
    return new TabEntries(Map.copyOf(indexed));
  }

  /**
   * Returns the empty collection.
   *
   * @return a {@code TabEntries} with no rows
   */
  public static TabEntries empty() {
    return new TabEntries(Map.of());
  }

  /**
   * Returns the rows of this collection.
   *
   * @return an immutable view of the rows
   */
  public Collection<TabEntry> values() {
    return byTarget.values();
  }

  /**
   * Returns the rows of this collection that are absent from, or differ from, {@code previous}.
   *
   * @param previous the previously sent rows to compare against; never {@code null}
   * @return the rows to upsert
   */
  public Collection<TabEntry> upsertsAgainst(TabEntries previous) {
    Objects.requireNonNull(previous, "previous");
    return byTarget.values().stream().filter(previous::isChanged).toList();
  }

  /**
   * Returns the targets present in {@code previous} but no longer present here.
   *
   * @param previous the previously sent rows to compare against; never {@code null}
   * @return the targets to remove
   */
  public Collection<ViewerId> removalsAgainst(TabEntries previous) {
    Objects.requireNonNull(previous, "previous");
    return previous.byTarget.keySet().stream()
        .filter(target -> !byTarget.containsKey(target))
        .toList();
  }

  private boolean isChanged(TabEntry candidate) {
    return !candidate.equals(byTarget.get(candidate.target()));
  }
}
