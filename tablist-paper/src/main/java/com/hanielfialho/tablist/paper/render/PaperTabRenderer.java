package com.hanielfialho.tablist.paper.render;

import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.TabRenderer;
import com.hanielfialho.tablist.core.port.TabScheduler;
import com.hanielfialho.tablist.core.state.TabDiff;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Pushes tab-list changes to Paper using only the official API — no NMS, no manual packets, no
 * ProtocolLib.
 *
 * <p>Every change to a player is routed through the {@link TabScheduler} so it runs on that
 * player's own thread (the main thread on Paper, the player's region thread on Folia). The {@link
 * Component}s are resolved on the calling thread first and captured, so the scheduled action only
 * touches Bukkit. The target player is resolved with {@code Bukkit.getPlayer} at the moment the
 * action runs; if they are offline by then, it is silently skipped.
 *
 * <p>Header and footer are per-viewer (sent to the viewer as an audience); names and order are
 * per-target and therefore global, which matches vanilla behaviour without packets.
 */
public final class PaperTabRenderer implements TabRenderer {

  private final ComponentResolver resolver;
  private final ListSorter sorter;
  private final TabScheduler scheduler;

  /**
   * Creates the renderer.
   *
   * @param resolver resolves templates into components; never {@code null}
   * @param sorter applies the tab-list order; never {@code null}
   * @param scheduler routes each change onto the owning player's thread; never {@code null}
   */
  public PaperTabRenderer(ComponentResolver resolver, ListSorter sorter, TabScheduler scheduler) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.sorter = Objects.requireNonNull(sorter, "sorter");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
  }

  @Override
  public void render(ViewerId viewer, TabDiff diff) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(diff, "diff");
    diff.headerFooter().ifPresent(headerFooter -> renderHeaderFooter(viewer, headerFooter));
    diff.upsertedEntries().forEach(this::renderEntry);
    diff.removedTargets().forEach(this::clearEntry);
  }

  private void renderHeaderFooter(ViewerId viewer, HeaderFooter headerFooter) {
    Component header = resolver.resolve(viewer, headerFooter.header().frameAt(0));
    Component footer = resolver.resolve(viewer, headerFooter.footer().frameAt(0));
    onPlayer(viewer, player -> player.sendPlayerListHeaderAndFooter(header, footer));
  }

  private void renderEntry(TabEntry entry) {
    Component name = resolver.name(entry.target(), entry.text());
    int order = entry.order();
    onPlayer(entry.target(), player -> applyEntry(player, name, order));
  }

  private void applyEntry(Player player, Component name, int order) {
    player.playerListName(name);
    sorter.apply(player, order);
  }

  private void clearEntry(ViewerId target) {
    onPlayer(target, this::resetEntry);
  }

  private void resetEntry(Player player) {
    player.playerListName(null);
    sorter.clear(player);
  }

  private void onPlayer(ViewerId id, Consumer<Player> action) {
    scheduler.forViewer(id).schedule(() -> runIfOnline(id, action));
  }

  private static void runIfOnline(ViewerId id, Consumer<Player> action) {
    Player player = Bukkit.getPlayer(id.value());
    if (player == null) {
      return;
    }
    action.accept(player);
  }
}
