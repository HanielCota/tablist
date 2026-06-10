package com.hanielfialho.tablist.core.config;

import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import java.util.Objects;

/**
 * Marks every currently known viewer as dirty in one shot.
 *
 * <p>Used after a configuration reload, when the whole tab list must be rebuilt for everyone. It
 * pairs the {@link ViewerDirectory} (who is online) with the {@link DirtyTracker} (what needs
 * re-rendering).
 */
public final class DirtyAllViewers {

  private final DirtyTracker dirty;
  private final ViewerDirectory directory;

  /**
   * Creates the bulk marker.
   *
   * @param dirty the tracker to mark into; never {@code null}
   * @param directory the source of current viewers; never {@code null}
   */
  public DirtyAllViewers(DirtyTracker dirty, ViewerDirectory directory) {
    this.dirty = Objects.requireNonNull(dirty, "dirty");
    this.directory = Objects.requireNonNull(directory, "directory");
  }

  /** Marks all viewers from the directory as dirty. */
  public void markAll() {
    directory.viewers().forEach(dirty::markDirty);
  }
}
