package com.hanielfialho.tablist.paper.placeholder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderData;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies that, with PlaceholderAPI absent, the resolver chain is just the built-in resolver and
 * resolves Tablist's own placeholders. The {@code PapiPlaceholderResolver} is never constructed on
 * this path, so {@code PlaceholderAPI} stays off the classpath and the plugin starts normally.
 */
class PaperPlaceholderResolversTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  @Test
  void builtinResolvesWithoutPlaceholderApi() {
    PlaceholderResolver resolver =
        PaperPlaceholderResolvers.create(new StubData("Steve", 7, 12), false);

    assertEquals(
        "Steve (7) 12ms", resolver.resolve(VIEWER, "%player_name% (%online%) %ping%ms").join());
  }

  @Test
  void leavesUnknownTokensUntouchedWithoutPlaceholderApi() {
    PlaceholderResolver resolver =
        PaperPlaceholderResolvers.create(new StubData("Steve", 1, 0), false);

    assertEquals("%vault_prefix%", resolver.resolve(VIEWER, "%vault_prefix%").join());
  }

  /** Minimal {@link PlaceholderData} stub so the test never touches Bukkit. */
  private record StubData(String name, int online, int ping) implements PlaceholderData {

    @Override
    public String playerName(ViewerId viewer) {
      return name;
    }

    @Override
    public int onlineCount() {
      return online;
    }

    @Override
    public int ping(ViewerId viewer) {
      return ping;
    }
  }
}
