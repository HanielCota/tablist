package com.hanielfialho.tablist.paper.snapshot;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.AnimationFrameCycler;
import com.hanielfialho.tablist.core.config.FrameSection;
import com.hanielfialho.tablist.core.config.NameFormat;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.model.EntryText;
import com.hanielfialho.tablist.core.model.Frames;
import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.Renderable;
import com.hanielfialho.tablist.core.model.TabEntry;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.GroupWeightResolver;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.AnimationClock;
import com.hanielfialho.tablist.core.state.TabEntries;
import com.hanielfialho.tablist.core.state.TabSnapshot;
import java.util.List;
import java.util.Objects;

/**
 * Builds each viewer's snapshot from the active configuration and the shared animation clock.
 *
 * <p>The header and footer are reduced to the frame the clock currently selects, so the flusher's
 * diff naturally re-renders only when the frame advances. Every online viewer becomes a row
 * carrying the configured name template; placeholders inside the templates are resolved later by
 * the renderer's pipeline.
 *
 * <p>Rows, orders and header/footer depend only on the current tick, not on which viewer is asking,
 * so they are built once per tick and shared by every viewer in that flush. Without this the
 * flusher — which rebuilds all viewers each tick — would recompute the whole roster, and one {@link
 * GroupWeightResolver} lookup per row, once per viewer: quadratic in the player count. {@code
 * snapshotOf} is only ever called from the single flush thread, so the per-tick memo needs no lock.
 */
public final class PaperSnapshotSource implements SnapshotSource {

  private final ActiveConfig config;
  private final ViewerDirectory viewers;
  private final AnimationClock clock;
  private final GroupWeightResolver groups;

  private long cachedTick = Long.MIN_VALUE;
  private TabEntries cachedEntries;
  private HeaderFooter cachedHeaderFooter;

  /**
   * Creates the snapshot source.
   *
   * @param config the active configuration; never {@code null}
   * @param viewers the directory of viewers to list; never {@code null}
   * @param clock the shared animation clock; never {@code null}
   * @param groups resolves each row's sort order from the target's group; never {@code null}
   */
  public PaperSnapshotSource(
      ActiveConfig config,
      ViewerDirectory viewers,
      AnimationClock clock,
      GroupWeightResolver groups) {
    this.config = Objects.requireNonNull(config, "config");
    this.viewers = Objects.requireNonNull(viewers, "viewers");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.groups = Objects.requireNonNull(groups, "groups");
  }

  private static Frames frame(FrameSection section, long tick) {
    return Frames.single(AnimationFrameCycler.from(section).frameAt(tick));
  }

  private static EntryText entryText(NameFormat format) {
    return new EntryText(
        new Renderable(format.prefix()),
        new Renderable(format.name()),
        new Renderable(format.suffix()));
  }

  @Override
  public TabSnapshot snapshotOf(ViewerId viewer) {
    refreshIfStale(clock.tick());
    return new TabSnapshot(viewer, cachedEntries, cachedHeaderFooter);
  }

  private void refreshIfStale(long tick) {
    if (tick == cachedTick) {
      return;
    }
    TabConfig current = config.current();
    cachedHeaderFooter =
        new HeaderFooter(frame(current.header(), tick), frame(current.footer(), tick));
    cachedEntries = TabEntries.of(rows(current.nameFormat()));
    cachedTick = tick;
  }

  private List<TabEntry> rows(NameFormat format) {
    EntryText text = entryText(format);
    return viewers.viewers().stream()
        .map(target -> new TabEntry(target, text, groups.orderOf(target)))
        .toList();
  }
}
