package com.hanielfialho.tablist.core.config;

import java.util.Objects;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * A sorting group and the weight that decides its position in the list.
 *
 * <p>The weight is a <em>rank</em>: the lower it is, the higher up the list the group appears (so
 * {@code admin: 0} sits above {@code default: 100}). The order of equal weights is decided
 * downstream. The core only carries the data here — {@link GroupWeights} turns these weights into
 * tab orders.
 *
 * @param group the permission/group name this entry matches; never {@code null}
 * @param weight the sort rank; lower sorts earlier (towards the top)
 */
@ConfigSerializable
public record SortGroup(String group, int weight) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code group} is {@code null}
   */
  public SortGroup {
    Objects.requireNonNull(group, "group");
  }
}
