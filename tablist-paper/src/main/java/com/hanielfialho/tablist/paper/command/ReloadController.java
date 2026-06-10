package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.tablist.core.config.ConfigReloader;
import com.hanielfialho.tablist.core.config.ReloadResult;
import java.util.Objects;

/**
 * Application service behind {@code /tablist reload}: reloads the config and reports the outcome.
 * The command body only calls {@link #reload(CommandActor)}; all behaviour lives here and in the
 * core {@link ConfigReloader}.
 */
public final class ReloadController {

  private final ConfigReloader reloader;
  private final Feedback feedback;

  /**
   * Creates the controller.
   *
   * @param reloader the core reload service; never {@code null}
   * @param feedback the feedback renderer; never {@code null}
   */
  public ReloadController(ConfigReloader reloader, Feedback feedback) {
    this.reloader = Objects.requireNonNull(reloader, "reloader");
    this.feedback = Objects.requireNonNull(feedback, "feedback");
  }

  /**
   * Reloads the configuration and tells the sender whether it worked.
   *
   * @param actor the command sender
   */
  public void reload(CommandActor actor) {
    ReloadResult result = reloader.reload();
    if (result.failed()) {
      feedback.reloadError(actor, result.message());
      return;
    }
    feedback.reloadSuccess(actor);
  }
}
