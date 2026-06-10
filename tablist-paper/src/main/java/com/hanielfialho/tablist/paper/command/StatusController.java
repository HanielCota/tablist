package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.tablist.core.status.StatusReporter;
import java.util.Objects;

/**
 * Application service behind {@code /tablist status}: asks the core {@link StatusReporter} for the
 * current health snapshot and renders it. The command body only calls {@link
 * #status(CommandActor)}.
 */
public final class StatusController {

  private final StatusReporter reporter;
  private final Feedback feedback;

  /**
   * Creates the controller.
   *
   * @param reporter the core status service; never {@code null}
   * @param feedback the feedback renderer; never {@code null}
   */
  public StatusController(StatusReporter reporter, Feedback feedback) {
    this.reporter = Objects.requireNonNull(reporter, "reporter");
    this.feedback = Objects.requireNonNull(feedback, "feedback");
  }

  /**
   * Reports the current runtime status to the sender.
   *
   * @param actor the command sender
   */
  public void status(CommandActor actor) {
    feedback.status(actor, reporter.report());
  }
}
