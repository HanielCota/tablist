package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.annotation.Command;
import com.hanielfialho.commandframework.annotation.Description;
import com.hanielfialho.commandframework.annotation.Permission;
import com.hanielfialho.commandframework.annotation.PlayerOnly;
import com.hanielfialho.commandframework.annotation.Subcommand;
import com.hanielfialho.commandframework.execution.CommandActor;
import java.util.Objects;

/**
 * The {@code /tablist} command — a thin facade over the controllers, with no business logic.
 *
 * <p>Each subcommand body does exactly one thing: delegate. Permissions and player-only constraints
 * are declared with the framework's annotations; the controllers (and the core services behind
 * them) own all behaviour. The controllers are injected through the constructor by a custom {@code
 * CommandInstanceResolver}, never a static singleton.
 */
@Command("tablist")
@Description("Tablist administration and per-player tab list toggle")
public final class TablistCommand {

  private final ReloadController reload;
  private final StatusController status;
  private final ToggleController toggle;
  private final PreviewController preview;
  private final PlaceholdersController placeholders;

  /**
   * Creates the command over its controllers.
   *
   * @param reload the reload controller; never {@code null}
   * @param status the status controller; never {@code null}
   * @param toggle the toggle controller; never {@code null}
   * @param preview the preview controller; never {@code null}
   * @param placeholders the placeholders controller; never {@code null}
   */
  public TablistCommand(
      ReloadController reload,
      StatusController status,
      ToggleController toggle,
      PreviewController preview,
      PlaceholdersController placeholders) {
    this.reload = Objects.requireNonNull(reload, "reload");
    this.status = Objects.requireNonNull(status, "status");
    this.toggle = Objects.requireNonNull(toggle, "toggle");
    this.preview = Objects.requireNonNull(preview, "preview");
    this.placeholders = Objects.requireNonNull(placeholders, "placeholders");
  }

  /**
   * {@code /tablist reload} — reloads the configuration.
   *
   * @param actor the command sender
   */
  @Subcommand("reload")
  @Permission("tablist.admin")
  @Description("Reload the Tablist configuration")
  public void reload(CommandActor actor) {
    reload.reload(actor);
  }

  /**
   * {@code /tablist status} — shows runtime status.
   *
   * @param actor the command sender
   */
  @Subcommand("status")
  @Permission("tablist.admin")
  @Description("Show Tablist runtime status")
  public void status(CommandActor actor) {
    status.status(actor);
  }

  /**
   * {@code /tablist toggle} — toggles the sender's custom tab list.
   *
   * @param actor the command sender
   */
  @Subcommand("toggle")
  @Permission("tablist.use")
  @PlayerOnly
  @Description("Toggle your own custom tab list on or off")
  public void toggle(CommandActor actor) {
    toggle.toggle(actor);
  }

  /**
   * {@code /tablist preview} — echoes the header and footer as they render for the sender.
   *
   * @param actor the command sender
   */
  @Subcommand("preview")
  @Permission("tablist.admin")
  @PlayerOnly
  @Description("Preview the header and footer as they render for you")
  public void preview(CommandActor actor) {
    preview.preview(actor);
  }

  /**
   * {@code /tablist placeholders} — lists the placeholders usable in the configuration.
   *
   * @param actor the command sender
   */
  @Subcommand("placeholders")
  @Permission("tablist.admin")
  @Description("List the placeholders you can use in the config")
  public void placeholders(CommandActor actor) {
    placeholders.placeholders(actor);
  }
}
