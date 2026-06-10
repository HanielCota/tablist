package com.hanielfialho.tablist.paper.snapshot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.GroupWeights;
import com.hanielfialho.tablist.core.config.SortGroup;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.GroupWeightResolver;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.AnimationClock;
import com.hanielfialho.tablist.core.state.TabSnapshot;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies the snapshot carries each row's group order, so the rendered tab is sorted by weight.
 * Uses the same weights as the spec's example: {@code admin=0, vip=1, default=9}.
 */
class PaperSnapshotSourceTest {

  private static final ViewerId ADMIN = new ViewerId(UUID.randomUUID());
  private static final ViewerId VIP = new ViewerId(UUID.randomUUID());
  private static final ViewerId DEFAULT = new ViewerId(UUID.randomUUID());

  @Test
  void ordersRowsByGroupWeight() {
    GroupWeights weights =
        GroupWeights.from(
            List.of(
                new SortGroup("admin", 0), new SortGroup("vip", 1), new SortGroup("default", 9)));
    Map<ViewerId, Integer> orders =
        Map.of(
            ADMIN, weights.orderOf("admin"),
            VIP, weights.orderOf("vip"),
            DEFAULT, weights.orderOf("default"));
    GroupWeightResolver groups = orders::get;
    ViewerDirectory directory = () -> List.of(ADMIN, VIP, DEFAULT);

    ActiveConfig config = new ActiveConfig(TabConfig.defaults(), () -> {});
    PaperSnapshotSource source =
        new PaperSnapshotSource(config, directory, new AnimationClock(), groups);

    Map<ViewerId, Integer> rowOrders = orderByTarget(source.snapshotOf(ADMIN));

    assertTrue(rowOrders.get(ADMIN) > rowOrders.get(VIP), "admin should sort above vip");
    assertTrue(rowOrders.get(VIP) > rowOrders.get(DEFAULT), "vip should sort above default");
  }

  private static Map<ViewerId, Integer> orderByTarget(TabSnapshot snapshot) {
    return snapshot.entries().values().stream()
        .collect(java.util.stream.Collectors.toMap(TabEntry::target, TabEntry::order));
  }
}
