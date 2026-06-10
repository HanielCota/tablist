package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.Objects;
import java.util.Optional;

/**
 * The immutable, complete state of a single viewer's tab list: its rows and its header/footer.
 *
 * <p>A snapshot is a pure value. The flusher stores the last one it sent per viewer and asks each
 * fresh snapshot to {@link #diffFrom(TabSnapshot) diff itself} against that previous state.
 *
 * @param viewer the player this snapshot belongs to; never {@code null}
 * @param entries the rows of the tab list; never {@code null}
 * @param headerFooter the header and footer; never {@code null}
 */
public record TabSnapshot(ViewerId viewer, TabEntries entries, HeaderFooter headerFooter) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if any component is {@code null}
   */
  public TabSnapshot {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(entries, "entries");
    Objects.requireNonNull(headerFooter, "headerFooter");
  }

  /**
   * Returns the empty snapshot for a viewer: no rows and a blank header/footer.
   *
   * @param viewer the owner of the snapshot
   * @return an empty {@code TabSnapshot}
   */
  public static TabSnapshot empty(ViewerId viewer) {
    return new TabSnapshot(viewer, TabEntries.empty(), HeaderFooter.empty());
  }

  /**
   * Computes the change set needed to turn {@code previous} into this snapshot.
   *
   * @param previous the last snapshot sent to the viewer; never {@code null}
   * @return the diff to render
   */
  public TabDiff diffFrom(TabSnapshot previous) {
    Objects.requireNonNull(previous, "previous");
    return new TabDiff(
        entries.upsertsAgainst(previous.entries()),
        entries.removalsAgainst(previous.entries()),
        changedHeaderFooter(previous));
  }

  private Optional<HeaderFooter> changedHeaderFooter(TabSnapshot previous) {
    if (headerFooter.equals(previous.headerFooter())) {
      return Optional.empty();
    }
    return Optional.of(headerFooter);
  }
}
