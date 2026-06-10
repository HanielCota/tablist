package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The anti-leak contract: after a viewer quits, no structure may still hold anything about them —
 * not the registry, the dirty set, the snapshot store, nor any other {@link ViewerScoped}
 * collaborator (caches, the group-change watcher, ...).
 */
class ViewerLifecycleTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  @Test
  void quitLeavesNoTraceInAnyStructure() {
    ViewerRegistry registry = new ViewerRegistry();
    DirtyTracker dirty = new DirtyTracker();
    SnapshotStore store = new SnapshotStore();
    RecordingScoped extra = new RecordingScoped();
    ViewerLifecycle lifecycle = new ViewerLifecycle(registry, dirty, List.of(store, extra));

    lifecycle.join(VIEWER);
    store.store(snapshotWithOneRow());

    assertFalse(registry.viewers().isEmpty(), "precondition: registry tracks the viewer");
    assertFalse(dirty.isEmpty(), "precondition: viewer is dirty after join");
    assertFalse(
        store.lastFor(VIEWER).entries().values().isEmpty(), "precondition: a snapshot is stored");

    lifecycle.quit(VIEWER);

    assertTrue(registry.viewers().isEmpty(), "registry must forget the viewer");
    assertTrue(dirty.isEmpty(), "dirty tracker must forget the viewer");
    assertTrue(
        store.lastFor(VIEWER).entries().values().isEmpty(),
        "snapshot store must forget the viewer");
    assertTrue(extra.forgot(VIEWER), "every ViewerScoped collaborator must be told to forget");
  }

  private static TabSnapshot snapshotWithOneRow() {
    TabEntry row = new TabEntry(new ViewerId(UUID.randomUUID()), EntryText.of("", "X", ""), 0);
    return new TabSnapshot(VIEWER, TabEntries.of(List.of(row)), HeaderFooter.empty());
  }

  /** Records which viewers it was asked to forget. */
  private static final class RecordingScoped implements ViewerScoped {

    private final java.util.Set<ViewerId> forgotten =
        java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Override
    public void forget(ViewerId viewer) {
      forgotten.add(viewer);
    }

    boolean forgot(ViewerId viewer) {
      return forgotten.contains(viewer);
    }
  }
}
