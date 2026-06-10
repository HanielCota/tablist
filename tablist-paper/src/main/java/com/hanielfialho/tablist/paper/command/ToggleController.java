package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import com.hanielfialho.tablist.core.state.TablistToggle;
import java.util.Objects;

/**
 * Application service behind {@code /tablist toggle}: flips the sender's custom tab list and marks
 * them dirty so the next flush applies the change (custom or vanilla). The command body only calls
 * {@link #toggle(CommandActor)}.
 */
public final class ToggleController {

  private final TablistToggle toggle;
  private final DirtyTracker dirty;
  private final Feedback feedback;

  /**
   * Creates the controller.
   *
   * @param toggle the core per-viewer toggle; never {@code null}
   * @param dirty the dirty tracker; never {@code null}
   * @param feedback the feedback renderer; never {@code null}
   */
  public ToggleController(TablistToggle toggle, DirtyTracker dirty, Feedback feedback) {
    this.toggle = Objects.requireNonNull(toggle, "toggle");
    this.dirty = Objects.requireNonNull(dirty, "dirty");
    this.feedback = Objects.requireNonNull(feedback, "feedback");
  }

  /**
   * Toggles the sender's custom tab list and confirms the new state.
   *
   * @param actor the command sender (always a player; the command is player-only)
   */
  public void toggle(CommandActor actor) {
    if (actor.playerId().isEmpty()) {
      return;
    }
    ViewerId viewer = new ViewerId(actor.playerId().orElseThrow().value());
    boolean enabled = toggle.toggle(viewer);
    dirty.markDirty(viewer);
    feedback.toggle(actor, enabled);
  }
}
