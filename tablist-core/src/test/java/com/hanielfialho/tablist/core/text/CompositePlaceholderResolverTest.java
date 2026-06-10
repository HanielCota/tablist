package com.hanielfialho.tablist.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderData;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class CompositePlaceholderResolverTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());

  @Test
  void takesTheFirstResolverThatChangesTheInput() {
    PlaceholderResolver declines = (viewer, raw) -> CompletableFuture.completedFuture(raw);
    PlaceholderResolver winner = (viewer, raw) -> CompletableFuture.completedFuture("resolved");
    PlaceholderResolver mustNotRun =
        (viewer, raw) -> fail("a later resolver must not be consulted");

    CompositePlaceholderResolver composite =
        CompositePlaceholderResolver.of(declines, winner, mustNotRun);

    assertEquals("resolved", composite.resolve(VIEWER, "raw").join());
  }

  @Test
  void returnsTheInputUnchangedWhenNoResolverApplies() {
    PlaceholderResolver declines = (viewer, raw) -> CompletableFuture.completedFuture(raw);

    CompositePlaceholderResolver composite = CompositePlaceholderResolver.of(declines, declines);

    assertEquals("raw", composite.resolve(VIEWER, "raw").join());
  }

  @Test
  void builtinResolvesTheKnownPlaceholders() {
    PlaceholderData data = mock(PlaceholderData.class);
    when(data.playerName(VIEWER)).thenReturn("Steve");
    when(data.onlineCount()).thenReturn(5);
    when(data.ping(VIEWER)).thenReturn(42);

    BuiltinPlaceholderResolver builtin = new BuiltinPlaceholderResolver(data);

    assertEquals(
        "Steve (5) 42ms", builtin.resolve(VIEWER, "%player_name% (%online%) %ping%ms").join());
  }
}
