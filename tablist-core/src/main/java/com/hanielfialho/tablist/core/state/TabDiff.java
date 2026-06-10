package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The difference between two {@link TabSnapshot}s of the same viewer: what must be sent to bring
 * the client up to date.
 *
 * <p>The three parts are independent, which is what lets the renderer touch only the rows or only
 * the header/footer. A diff whose three parts are all absent is {@linkplain #isEmpty() empty}, and
 * an empty diff is never handed to the renderer.
 *
 * @param upsertedEntries the rows that are new or changed; copied defensively, never {@code null}
 * @param removedTargets the targets whose rows disappeared; copied defensively, never {@code null}
 * @param headerFooter the new header/footer, present only when it changed; never {@code null}
 */
public record TabDiff(
    Collection<TabEntry> upsertedEntries,
    Collection<ViewerId> removedTargets,
    Optional<HeaderFooter> headerFooter) {

  /**
   * Canonical constructor: copies the collections and rejects {@code null} input.
   *
   * @throws NullPointerException if any component (or element) is {@code null}
   */
  public TabDiff {
    upsertedEntries = List.copyOf(upsertedEntries);
    removedTargets = List.copyOf(removedTargets);
    Objects.requireNonNull(headerFooter, "headerFooter");
  }

  /**
   * Returns the empty diff (nothing to upsert, remove or re-send).
   *
   * @return an empty {@code TabDiff}
   */
  public static TabDiff empty() {
    return new TabDiff(List.of(), List.of(), Optional.empty());
  }

  /**
   * Tells whether this diff carries no change at all.
   *
   * @return {@code true} when there is nothing to render
   */
  public boolean isEmpty() {
    return upsertedEntries.isEmpty() && removedTargets.isEmpty() && headerFooter.isEmpty();
  }
}
