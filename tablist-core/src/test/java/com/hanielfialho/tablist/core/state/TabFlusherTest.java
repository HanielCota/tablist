package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.port.TabRenderer;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabFlusherTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  @Mock private TabRenderer renderer;

  private DirtyTracker dirty;
  private TabFlusher flusher;

  @BeforeEach
  void setUp() {
    dirty = new DirtyTracker();
    flusher = new TabFlusher(dirty, renderer, new SnapshotStore());
  }

  @Test
  void coalescesManyMarksIntoASingleRender() {
    TabSnapshot snapshot = snapshot(HeaderFooter.of("header", "footer"), entry("Steve"));

    IntStream.range(0, 50).forEach(i -> dirty.markDirty(VIEWER));
    flusher.flush(sourceOf(snapshot));

    verify(renderer, times(1)).render(eq(VIEWER), any(TabDiff.class));
  }

  @Test
  void skipsRenderWhenSnapshotIsUnchanged() {
    TabSnapshot snapshot = snapshot(HeaderFooter.of("header", "footer"), entry("Steve"));
    prime(snapshot);

    dirty.markDirty(VIEWER);
    flusher.flush(sourceOf(snapshot));

    verify(renderer, never()).render(eq(VIEWER), any(TabDiff.class));
  }

  @Test
  void diffsOnlyHeaderFooterWhenOnlyTheFooterChanged() {
    TabEntry steve = entry("Steve");
    prime(snapshot(HeaderFooter.of("header", "old-footer"), steve));

    dirty.markDirty(VIEWER);
    flusher.flush(sourceOf(snapshot(HeaderFooter.of("header", "new-footer"), steve)));

    TabDiff diff = captureRenderedDiff();
    assertTrue(diff.upsertedEntries().isEmpty(), "entries must not be in the diff");
    assertTrue(diff.removedTargets().isEmpty(), "no entry should be removed");
    assertTrue(diff.headerFooter().isPresent(), "header/footer change must be in the diff");
  }

  private void prime(TabSnapshot snapshot) {
    dirty.markDirty(VIEWER);
    flusher.flush(sourceOf(snapshot));
    clearInvocations(renderer);
  }

  private TabDiff captureRenderedDiff() {
    ArgumentCaptor<TabDiff> captor = ArgumentCaptor.forClass(TabDiff.class);
    verify(renderer, times(1)).render(eq(VIEWER), captor.capture());
    return captor.getValue();
  }

  private static SnapshotSource sourceOf(TabSnapshot snapshot) {
    return viewer -> snapshot;
  }

  private static TabSnapshot snapshot(HeaderFooter headerFooter, TabEntry... entries) {
    return new TabSnapshot(VIEWER, TabEntries.of(List.of(entries)), headerFooter);
  }

  private static TabEntry entry(String name) {
    return new TabEntry(new ViewerId(UUID.randomUUID()), EntryText.of("", name, ""), 0);
  }
}
