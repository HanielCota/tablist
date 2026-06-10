package com.hanielfialho.tablist.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hanielfialho.tablist.core.config.GroupWeights;
import com.hanielfialho.tablist.core.config.SortGroup;
import com.hanielfialho.tablist.core.state.SnapshotStore;
import com.hanielfialho.tablist.core.status.StatusSources;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Locks in the fail-fast guards on the spots where a {@code null} used to slip through silently —
 * the snapshot store returned a default, the group resolver fell back to the default order, and the
 * status sources record accepted nulls. These now reject {@code null} at the boundary.
 */
class NullChecksTest {

  @Test
  void snapshotStoreRejectsNullViewer() {
    assertThrows(NullPointerException.class, () -> new SnapshotStore().lastFor(null));
  }

  @Test
  void groupWeightsRejectNullGroup() {
    GroupWeights weights = GroupWeights.from(List.of(new SortGroup("admin", 0)));
    assertThrows(NullPointerException.class, () -> weights.orderOf(null));
  }

  @Test
  void statusSourcesRejectNullComponents() {
    assertThrows(NullPointerException.class, () -> new StatusSources(null, null, null, null, null));
  }
}
