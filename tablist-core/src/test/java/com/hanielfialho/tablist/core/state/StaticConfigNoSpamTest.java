package com.hanielfialho.tablist.core.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.config.DirtyAllViewers;
import com.hanielfialho.tablist.core.config.Heartbeat;
import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.port.TabRenderer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * The performance contract of Tablist: with a static configuration (no animation, no dynamic
 * placeholders), the dirty-flag + diff pipeline must send the tab list exactly once per viewer and
 * then go completely silent — zero further renders, however long the loop runs.
 *
 * <p>This simulates the production loop directly: a {@link Heartbeat} marks every viewer dirty each
 * tick (as the real flush loop does) and a {@link TabFlusher} drains, diffs and renders. If a
 * future change reintroduces per-tick rendering, this test fails — that is the alarm that the
 * design broke.
 */
class StaticConfigNoSpamTest {

  private static final int VIEWERS = 100;
  private static final int TICKS_PER_SECOND = 20;
  private static final int SECONDS = 60;

  private static void tick(Heartbeat heartbeat, TabFlusher flusher, SnapshotSource source) {
    heartbeat.beat();
    flusher.flush(source);
  }

  private static TabRenderer counting(AtomicInteger renders) {
    return (viewer, diff) -> renders.incrementAndGet();
  }

  private static ViewerRegistry registryOf(int count) {
    ViewerRegistry registry = new ViewerRegistry();
    IntStream.range(0, count).forEach(i -> registry.add(new ViewerId(UUID.randomUUID())));
    return registry;
  }

  @Test
  void staticConfigSendsZeroRendersAfterTheFirstCycle() {
    ViewerRegistry registry = registryOf(VIEWERS);
    DirtyTracker dirty = new DirtyTracker();
    AtomicInteger renders = new AtomicInteger();
    UpdateMetrics metrics = new UpdateMetrics();
    TabRenderer renderer = new MeteredTabRenderer(counting(renders), metrics);

    TabFlusher flusher = new TabFlusher(dirty, renderer, new SnapshotStore());
    Heartbeat heartbeat = new Heartbeat(new AnimationClock(), new DirtyAllViewers(dirty, registry));
    SnapshotSource staticConfig = new StaticSnapshotSource(registry.viewers());

    tick(heartbeat, flusher, staticConfig);
    assertEquals(VIEWERS, renders.get(), "the first cycle paints every viewer exactly once");

    renders.set(0);
    int remainingTicks = SECONDS * TICKS_PER_SECOND - 1;
    IntStream.range(0, remainingTicks).forEach(i -> tick(heartbeat, flusher, staticConfig));

    assertEquals(0, renders.get(), "a static config must send zero renders after the first cycle");
    assertEquals(
        VIEWERS, metrics.updatesInLastMinute(), "the update counter must match the renders sent");
  }

  /**
   * A snapshot source modelling a static configuration: the same roster, header and footer for
   * every tick, so a rebuilt snapshot always equals the last one stored.
   */
  private static final class StaticSnapshotSource implements SnapshotSource {

    private final HeaderFooter headerFooter = HeaderFooter.of("Tablist", "Static footer");
    private final List<TabEntry> roster;

    StaticSnapshotSource(Collection<ViewerId> viewers) {
      this.roster = viewers.stream().map(StaticSnapshotSource::row).toList();
    }

    private static TabEntry row(ViewerId target) {
      return new TabEntry(target, EntryText.of("", "Player", ""), 0);
    }

    @Override
    public TabSnapshot snapshotOf(ViewerId viewer) {
      return new TabSnapshot(viewer, TabEntries.of(roster), headerFooter);
    }
  }
}
