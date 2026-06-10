package com.hanielfialho.tablist.core.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * Turns the configured {@link SortGroup} weights into tab-list orders.
 *
 * <p>A weight is a <em>rank</em>: the lower the weight, the higher up the list the group appears
 * (so {@code admin: 0} sits above {@code default: 100}). Tab orders run the other way — higher is
 * shown first — so this type inverts the rank against the largest configured weight, which keeps
 * every order non-negative while preserving the relative order. Only the relative order matters to
 * the renderer, so the absolute base is irrelevant.
 *
 * <p>A viewer that matches no configured group falls back to the {@code default} group's order, or
 * to the bottom of the list when no such group is configured.
 */
public final class GroupWeights {

  private static final String DEFAULT_GROUP = "default";

  private final Map<String, Integer> weightByGroup;
  private final int maxWeight;

  private GroupWeights(Map<String, Integer> weightByGroup, int maxWeight) {
    this.weightByGroup = weightByGroup;
    this.maxWeight = maxWeight;
  }

  /**
   * Builds the mapping from the configured sorting groups.
   *
   * @param groups the ordered sorting groups; never {@code null}, may be empty
   * @return the weight-to-order mapping
   */
  public static GroupWeights from(List<SortGroup> groups) {
    Objects.requireNonNull(groups, "groups");
    Map<String, Integer> byGroup = new LinkedHashMap<>();
    groups.forEach(group -> byGroup.put(group.group(), group.weight()));
    int max = byGroup.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    return new GroupWeights(Map.copyOf(byGroup), max);
  }

  /**
   * Returns the order for a named group, or the {@linkplain #defaultOrder() default order} when the
   * group is not configured.
   *
   * @param group the group name; never {@code null}
   * @return the sort order; higher is shown first
   */
  public int orderOf(String group) {
    Objects.requireNonNull(group, "group");
    Integer weight = weightByGroup.get(group);
    return weight == null ? defaultOrder() : toOrder(weight);
  }

  /**
   * Returns the order of the best-ranked group the predicate accepts, or the {@linkplain
   * #defaultOrder() default order} when it accepts none.
   *
   * <p>"Best" is the lowest configured weight, so a player in several groups inherits the highest
   * position any of them grants.
   *
   * @param member tells whether the player belongs to a given group name; never {@code null}
   * @return the sort order; higher is shown first
   */
  public int orderFor(Predicate<String> member) {
    Objects.requireNonNull(member, "member");
    OptionalInt best =
        weightByGroup.entrySet().stream()
            .filter(entry -> member.test(entry.getKey()))
            .mapToInt(Map.Entry::getValue)
            .min();
    return best.isPresent() ? toOrder(best.getAsInt()) : defaultOrder();
  }

  /**
   * Returns the order used for viewers in no configured group.
   *
   * @return the {@code default} group's order, or {@code 0} (bottom) when it is not configured
   */
  public int defaultOrder() {
    Integer weight = weightByGroup.get(DEFAULT_GROUP);
    return weight == null ? 0 : toOrder(weight);
  }

  private int toOrder(int weight) {
    return maxWeight - weight;
  }
}
