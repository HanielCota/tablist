package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class UpdateMetricsTest {

  @Test
  void countsUpdatesWithinTheLastMinute() {
    MutableClock clock = new MutableClock();
    UpdateMetrics metrics = new UpdateMetrics(clock);

    IntStream.range(0, 3).forEach(i -> metrics.recordUpdate());

    assertEquals(3, metrics.updatesInLastMinute());
  }

  @Test
  void dropsUpdatesOlderThanAMinute() {
    MutableClock clock = new MutableClock();
    UpdateMetrics metrics = new UpdateMetrics(clock);
    metrics.recordUpdate();
    metrics.recordUpdate();

    clock.advance(61_000);
    metrics.recordUpdate();

    assertEquals(1, metrics.updatesInLastMinute(), "only the recent update remains in the window");
  }

  /** A clock whose epoch millis can be advanced by hand. */
  private static final class MutableClock extends Clock {

    private long millis;

    void advance(long byMillis) {
      millis += byMillis;
    }

    @Override
    public Instant instant() {
      return Instant.ofEpochMilli(millis);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }
  }
}
