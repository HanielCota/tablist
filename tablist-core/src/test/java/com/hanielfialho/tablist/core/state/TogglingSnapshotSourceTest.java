package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Guards the toggle behaviour: switching the custom tab off must blank only the per-viewer
 * header/footer, never remove rows — because rows map to globally-applied list names, so removing
 * them would wipe every other player's tab too.
 */
class TogglingSnapshotSourceTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  private final TabSnapshot full =
      new TabSnapshot(
          VIEWER, TabEntries.of(List.of(row(), row())), HeaderFooter.of("head", "foot"));
  private final SnapshotSource delegate = viewer -> full;
  private final TablistToggle toggle = new TablistToggle();
  private final TogglingSnapshotSource source = new TogglingSnapshotSource(delegate, toggle);

  private static TabEntry row() {
    return new TabEntry(new ViewerId(UUID.randomUUID()), EntryText.of("", "Steve", ""), 0);
  }

  @Test
  void passesTheCustomSnapshotThroughWhenEnabled() {
    assertSame(full, source.snapshotOf(VIEWER), "an enabled viewer gets the delegate's snapshot");
  }

  @Test
  void blanksOnlyHeaderFooterWhenDisabled() {
    toggle.toggle(VIEWER); // now off

    TabSnapshot off = source.snapshotOf(VIEWER);

    assertEquals(full.entries().values(), off.entries().values(), "rows must be preserved");
    assertEquals(HeaderFooter.empty(), off.headerFooter(), "header/footer must be blanked");
  }

  @Test
  void disabledSnapshotRemovesNoRows() {
    toggle.toggle(VIEWER); // now off

    // Diffing the off-snapshot against the full one previously sent must not remove any row —
    // that was the bug that wiped every player's global list name.
    TabDiff diff = source.snapshotOf(VIEWER).diffFrom(full);

    assertTrue(diff.removedTargets().isEmpty(), "no row may be removed when toggling off");
    assertTrue(diff.upsertedEntries().isEmpty(), "rows are unchanged, so nothing to upsert");
    assertTrue(diff.headerFooter().isPresent(), "only the header/footer changes");
  }
}
