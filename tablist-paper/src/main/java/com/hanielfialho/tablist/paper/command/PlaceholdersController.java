package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.tablist.core.text.PlaceholderCatalog;
import java.util.Objects;

/**
 * Application service behind {@code /tablist placeholders}: prints the placeholders an admin can
 * use in the config, so they never have to guess one exists or dig through the source. It lists
 * every built-in placeholder from the {@link PlaceholderCatalog} and, when PlaceholderAPI is
 * installed, adds a note that its placeholders work too.
 */
public final class PlaceholdersController {

  private final Feedback feedback;
  private final boolean placeholderApiPresent;

  /**
   * Creates the controller.
   *
   * @param feedback renders the listing; never {@code null}
   * @param placeholderApiPresent whether PlaceholderAPI is installed on this server
   */
  public PlaceholdersController(Feedback feedback, boolean placeholderApiPresent) {
    this.feedback = Objects.requireNonNull(feedback, "feedback");
    this.placeholderApiPresent = placeholderApiPresent;
  }

  /**
   * Sends the placeholder listing to the sender.
   *
   * @param actor the command sender
   */
  public void placeholders(CommandActor actor) {
    feedback.placeholdersHeader(actor);
    for (PlaceholderCatalog.Entry entry : PlaceholderCatalog.builtins()) {
      feedback.placeholdersEntry(actor, entry.token(), entry.description());
    }
    if (placeholderApiPresent) {
      feedback.placeholdersPapi(actor);
    }
  }
}
