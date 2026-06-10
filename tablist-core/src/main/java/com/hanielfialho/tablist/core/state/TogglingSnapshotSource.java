package com.hanielfialho.tablist.core.state;

import com.hanielfialho.tablist.core.model.HeaderFooter;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.SnapshotSource;
import java.util.Objects;

/**
 * A {@link SnapshotSource} decorator that blanks the header and footer for viewers who switched the
 * custom tab list off.
 *
 * <p>The toggle is genuinely per-viewer, but a player's list <em>name</em> and sort order are
 * applied globally (the same value every other client sees), so they cannot be hidden for one
 * viewer without changing them for everyone. Only the header/footer is per-viewer, so that is what
 * the toggle controls: an off-viewer keeps the shared rows but sees no custom header/footer.
 * Returning a fully empty snapshot here would diff as "remove every row" and wipe the global names
 * for all players, which is why the rows are preserved.
 */
public final class TogglingSnapshotSource implements SnapshotSource {

  private final SnapshotSource delegate;
  private final TablistToggle toggle;

  /**
   * Wraps a snapshot source with the per-viewer toggle.
   *
   * @param delegate the source that builds the custom snapshot; never {@code null}
   * @param toggle the per-viewer on/off state; never {@code null}
   */
  public TogglingSnapshotSource(SnapshotSource delegate, TablistToggle toggle) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.toggle = Objects.requireNonNull(toggle, "toggle");
  }

  @Override
  public TabSnapshot snapshotOf(ViewerId viewer) {
    TabSnapshot custom = delegate.snapshotOf(viewer);
    if (toggle.isEnabled(viewer)) {
      return custom;
    }
    return new TabSnapshot(viewer, custom.entries(), HeaderFooter.empty());
  }
}
