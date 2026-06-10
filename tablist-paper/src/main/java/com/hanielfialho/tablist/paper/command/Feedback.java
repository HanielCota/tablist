package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.commandframework.paper.feedback.FeedbackAudience;
import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.Messages;
import com.hanielfialho.tablist.core.status.TablistStatus;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Renders the configured MiniMessage feedback templates and sends them to the command sender as
 * Components. Every line comes from {@link Messages} in the live config — nothing is hardcoded.
 */
public final class Feedback {

  private final ActiveConfig config;
  private final MiniMessage miniMessage;

  /**
   * Creates feedback over the live config, using the shared MiniMessage instance.
   *
   * @param config the active configuration supplying the templates; never {@code null}
   */
  public Feedback(ActiveConfig config) {
    this(config, MiniMessage.miniMessage());
  }

  /**
   * Creates feedback with an explicit MiniMessage instance (for tests).
   *
   * @param config the active configuration supplying the templates; never {@code null}
   * @param miniMessage the MiniMessage instance to render with; never {@code null}
   */
  public Feedback(ActiveConfig config, MiniMessage miniMessage) {
    this.config = Objects.requireNonNull(config, "config");
    this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
  }

  private static String percent(double rate) {
    return String.format(Locale.ROOT, "%.1f%%", rate * 100);
  }

  /**
   * Sends the reload-success line.
   *
   * @param actor the command sender
   */
  public void reloadSuccess(CommandActor actor) {
    send(actor, messages().reloadSuccess());
  }

  /**
   * Sends the reload-error line, with {@code <error>} resolved.
   *
   * @param actor the command sender
   * @param error the readable parse/validation error
   */
  public void reloadError(CommandActor actor, String error) {
    send(actor, messages().reloadError(), Placeholder.unparsed("error", error));
  }

  /**
   * Sends the status line, with every metric resolved.
   *
   * @param actor the command sender
   * @param status the current runtime status
   */
  public void status(CommandActor actor, TablistStatus status) {
    send(
        actor,
        messages().status(),
        Placeholder.unparsed("viewers", Integer.toString(status.activeViewers())),
        Placeholder.unparsed("hitrate", percent(status.cacheHitRate())),
        Placeholder.unparsed("updates", Integer.toString(status.updatesLastMinute())),
        Placeholder.unparsed("frame", Integer.toString(status.currentFrame())));
  }

  /**
   * Sends the toggle line matching the new state.
   *
   * @param actor the command sender
   * @param enabled whether the custom tab list is now enabled
   */
  public void toggle(CommandActor actor, boolean enabled) {
    send(actor, enabled ? messages().toggleOn() : messages().toggleOff());
  }

  /**
   * Sends the {@code preview-header} label that precedes the rendered header.
   *
   * @param actor the command sender
   */
  public void previewHeaderLabel(CommandActor actor) {
    send(actor, messages().previewHeader());
  }

  /**
   * Sends the {@code preview-footer} label that precedes the rendered footer.
   *
   * @param actor the command sender
   */
  public void previewFooterLabel(CommandActor actor) {
    send(actor, messages().previewFooter());
  }

  /**
   * Sends an already-rendered component verbatim — used to echo a resolved preview frame.
   *
   * @param actor the command sender
   * @param component the component to send; never {@code null}
   */
  public void component(CommandActor actor, Component component) {
    Objects.requireNonNull(component, "component");
    if (actor instanceof FeedbackAudience audience) {
      audience.sendInfo(component);
    }
  }

  /**
   * Sends the {@code placeholders-header} heading of the placeholder listing.
   *
   * @param actor the command sender
   */
  public void placeholdersHeader(CommandActor actor) {
    send(actor, messages().placeholdersHeader());
  }

  /**
   * Sends one {@code placeholders-entry} row, with {@code <name>} and {@code <description>}
   * resolved.
   *
   * @param actor the command sender
   * @param token the placeholder as written in the config, e.g. {@code %online%}
   * @param description the placeholder's one-line description
   */
  public void placeholdersEntry(CommandActor actor, String token, String description) {
    send(
        actor,
        messages().placeholdersEntry(),
        Placeholder.unparsed("name", token),
        Placeholder.unparsed("description", description));
  }

  /**
   * Sends the {@code placeholders-papi} note shown when PlaceholderAPI is installed.
   *
   * @param actor the command sender
   */
  public void placeholdersPapi(CommandActor actor) {
    send(actor, messages().placeholdersPapi());
  }

  private Messages messages() {
    return config.current().messages();
  }

  private void send(CommandActor actor, String template, TagResolver... placeholders) {
    if (!(actor instanceof FeedbackAudience audience)) {
      return;
    }
    audience.sendInfo(miniMessage.deserialize(template, placeholders));
  }
}
