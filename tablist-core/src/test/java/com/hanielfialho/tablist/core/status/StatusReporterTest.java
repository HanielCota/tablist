package com.hanielfialho.tablist.core.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.AnimationClock;
import com.hanielfialho.tablist.core.state.UpdateMetrics;
import com.hanielfialho.tablist.core.text.ResolvedTextCache;
import com.hanielfialho.tablist.core.text.ResolvedTextKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class StatusReporterTest {

  @Test
  void reportsViewersHitRateUpdatesAndCurrentFrame() {
    List<ViewerId> viewers =
        List.of(new ViewerId(UUID.randomUUID()), new ViewerId(UUID.randomUUID()));
    ViewerDirectory directory = () -> viewers;

    ResolvedTextCache cache = ResolvedTextCache.create(Duration.ofSeconds(60));
    primeOneHit(cache); // one miss + one hit -> hit-rate 0.5

    UpdateMetrics metrics = new UpdateMetrics();
    metrics.recordUpdate();
    metrics.recordUpdate();
    metrics.recordUpdate();

    AnimationClock clock = new AnimationClock();
    advanceTo(clock, 20); // default header: 2 frames, interval 20 -> frame index 1

    StatusReporter reporter =
        new StatusReporter(new StatusSources(directory, cache, metrics, clock, defaultConfig()));

    TablistStatus status = reporter.report();

    assertEquals(2, status.activeViewers());
    assertEquals(0.5, status.cacheHitRate(), 0.0001);
    assertEquals(3, status.updatesLastMinute());
    assertEquals(1, status.currentFrame());
  }

  private static void primeOneHit(ResolvedTextCache cache) {
    ResolvedTextKey key = new ResolvedTextKey(new ViewerId(UUID.randomUUID()), "<red>x</red>");
    cache.get(key, () -> CompletableFuture.completedFuture(Component.empty())).join();
    cache.get(key, () -> CompletableFuture.completedFuture(Component.empty())).join();
  }

  private static void advanceTo(AnimationClock clock, long tick) {
    for (long i = 0; i < tick; i++) {
      clock.advance();
    }
  }

  private static ActiveConfig defaultConfig() {
    return new ActiveConfig(TabConfig.defaults(), () -> {});
  }
}
