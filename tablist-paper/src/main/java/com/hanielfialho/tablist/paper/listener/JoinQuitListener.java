package com.hanielfialho.tablist.paper.listener;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.state.ViewerLifecycle;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Bridges Bukkit join/quit events to the {@link ViewerLifecycle}.
 *
 * <p>On join the viewer is registered and marked dirty; on quit it is forgotten everywhere, so no
 * structure retains a disconnected player.
 */
public final class JoinQuitListener implements Listener {

  private final ViewerLifecycle lifecycle;

  /**
   * Creates the listener.
   *
   * @param lifecycle the lifecycle coordinator; never {@code null}
   */
  public JoinQuitListener(ViewerLifecycle lifecycle) {
    this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
  }

  /**
   * Registers and dirties the joining viewer.
   *
   * @param event the join event
   */
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    lifecycle.join(new ViewerId(event.getPlayer().getUniqueId()));
  }

  /**
   * Forgets the quitting viewer everywhere.
   *
   * @param event the quit event
   */
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    lifecycle.quit(new ViewerId(event.getPlayer().getUniqueId()));
  }
}
