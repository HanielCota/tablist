package com.hanielfialho.tablist.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GroupWeightsTest {

  private static final List<SortGroup> SORTING =
      List.of(new SortGroup("admin", 0), new SortGroup("vip", 1), new SortGroup("default", 9));

  @Test
  void lowerWeightSortsFirst() {
    GroupWeights weights = GroupWeights.from(SORTING);

    int admin = weights.orderOf("admin");
    int vip = weights.orderOf("vip");
    int aDefault = weights.orderOf("default");

    // Higher order is shown first, so the lowest-weight group must carry the highest order.
    assertTrue(admin > vip, "admin should sort above vip");
    assertTrue(vip > aDefault, "vip should sort above default");
    assertTrue(aDefault >= 0, "orders must stay non-negative for the native sorter");
  }

  @Test
  void picksTheBestRankedGroupAPlayerBelongsTo() {
    GroupWeights weights = GroupWeights.from(SORTING);
    Set<String> held = Set.of("vip", "default");

    // Holding both vip and default grants the higher position (vip).
    assertEquals(weights.orderOf("vip"), weights.orderFor(held::contains));
  }

  @Test
  void unknownGroupFallsBackToDefault() {
    GroupWeights weights = GroupWeights.from(SORTING);

    assertEquals(weights.orderOf("default"), weights.orderOf("nonexistent"));
    assertEquals(weights.defaultOrder(), weights.orderFor(group -> false));
  }

  @Test
  void defaultsToBottomWhenNoDefaultGroupConfigured() {
    GroupWeights weights = GroupWeights.from(List.of(new SortGroup("admin", 0)));

    assertEquals(0, weights.defaultOrder());
  }
}
