package com.hanielfialho.tablist.paper.sort;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.GroupWeights;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.GroupWeightResolver;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Resolves a player's tab-list order from their {@code tablist.group.<name>} permissions.
 *
 * <p>For each configured sorting group it checks whether the player holds {@code
 * tablist.group.<group>}; the matching group with the best (lowest) weight wins, and its order is
 * read from {@link GroupWeights}. The configuration is read live through {@link ActiveConfig}, so a
 * {@code /tablist reload} that changes the weights takes effect on the next resolve. An offline or
 * unknown player falls back to the default order.
 */
public final class PermissionGroupWeightResolver implements GroupWeightResolver {

  private static final String PERMISSION_PREFIX = "tablist.group.";

  private final ActiveConfig config;

  /**
   * Creates the resolver.
   *
   * @param config the active configuration to read sorting weights from; never {@code null}
   */
  public PermissionGroupWeightResolver(ActiveConfig config) {
    this.config = Objects.requireNonNull(config, "config");
  }

  @Override
  public int orderOf(ViewerId viewer) {
    Objects.requireNonNull(viewer, "viewer");
    GroupWeights weights = GroupWeights.from(config.current().sorting());
    Player player = Bukkit.getPlayer(viewer.value());
    if (player == null) {
      return weights.defaultOrder();
    }
    return weights.orderFor(group -> player.hasPermission(PERMISSION_PREFIX + group));
  }
}
