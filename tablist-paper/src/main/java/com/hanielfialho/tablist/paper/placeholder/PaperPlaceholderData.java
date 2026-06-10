package com.hanielfialho.tablist.paper.placeholder;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Paper implementation of {@link PlaceholderData}, reading the built-in placeholder values from
 * Bukkit. Offline viewers resolve to safe defaults rather than throwing.
 */
public final class PaperPlaceholderData implements PlaceholderData {

  @Override
  public String playerName(ViewerId viewer) {
    Player player = Bukkit.getPlayer(viewer.value());
    if (player == null) {
      return "";
    }
    return player.getName();
  }

  @Override
  public int onlineCount() {
    return Bukkit.getOnlinePlayers().size();
  }

  @Override
  public int ping(ViewerId viewer) {
    Player player = Bukkit.getPlayer(viewer.value());
    if (player == null) {
      return 0;
    }
    return player.getPing();
  }
}
