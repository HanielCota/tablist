package com.hanielfialho.tablist.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Ticker;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

class TextResolutionPipelineTest {

  private static final ViewerId VIEWER = new ViewerId(UUID.randomUUID());
  private static final String TEMPLATE = "<red>hello <player_name></red>";

  private static TextResolutionPipeline pipeline(
      PlaceholderResolver resolver,
      MiniMessage parser,
      Duration ttl,
      Ticker ticker,
      DirtyTracker dirty) {
    return new TextResolutionPipeline(
        ResolvedTextCache.create(ttl, ticker), new TextResolver(resolver, parser), dirty);
  }

  private static MiniMessage countingParser() {
    MiniMessage parser = mock(MiniMessage.class);
    when(parser.deserialize(anyString())).thenReturn(Component.empty());
    return parser;
  }

  private static PlaceholderResolver identity() {
    return (viewer, raw) -> CompletableFuture.completedFuture(raw);
  }

  @Test
  void parsesMiniMessageOncePerKeyWithinTheCacheWindow() {
    MiniMessage parser = countingParser();
    TextResolutionPipeline pipeline =
        pipeline(
            identity(), parser, Duration.ofSeconds(60), Ticker.systemTicker(), new DirtyTracker());

    IntStream.range(0, 100).forEach(i -> pipeline.resolve(VIEWER, TEMPLATE).join());

    verify(parser, times(1)).deserialize(TEMPLATE);
  }

  @Test
  void reParsesAfterTheCacheWindowExpires() {
    MiniMessage parser = countingParser();
    AtomicLong clock = new AtomicLong();
    TextResolutionPipeline pipeline =
        pipeline(identity(), parser, Duration.ofSeconds(3), clock::get, new DirtyTracker());

    pipeline.resolve(VIEWER, TEMPLATE).join();
    clock.addAndGet(Duration.ofSeconds(4).toNanos());
    pipeline.resolve(VIEWER, TEMPLATE).join();

    verify(parser, times(2)).deserialize(TEMPLATE);
  }

  @Test
  void slowResolutionDoesNotBlockAndMarksTheViewerDirtyWhenItCompletes() {
    CompletableFuture<String> pending = new CompletableFuture<>();
    PlaceholderResolver slow = (viewer, raw) -> pending;
    DirtyTracker dirty = new DirtyTracker();
    TextResolutionPipeline pipeline =
        pipeline(
            slow, MiniMessage.miniMessage(), Duration.ofSeconds(60), Ticker.systemTicker(), dirty);

    CompletableFuture<Component> result = pipeline.resolve(VIEWER, TEMPLATE);

    assertFalse(result.isDone(), "the pipeline must not block on a slow resolver");
    assertTrue(dirty.isEmpty(), "the viewer must not be dirty before resolution completes");

    pending.complete("<red>hello Steve</red>");
    result.join();

    assertEquals(
        List.of(VIEWER), List.copyOf(dirty.drainDirty()), "completion must mark the viewer dirty");
  }
}
