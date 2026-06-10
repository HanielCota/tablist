package com.hanielfialho.tablist.core.config;

import java.util.Objects;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * The command-feedback templates, in MiniMessage, configured under {@code messages} in {@code
 * config.yml}. Nothing the commands say is hardcoded; every line comes from here.
 *
 * <p>Placeholders are written as MiniMessage tags: {@code reload-error} supports {@code <error>};
 * {@code status} supports {@code <viewers>}, {@code <hitrate>}, {@code <updates>} and {@code
 * <frame>}.
 *
 * @param reloadSuccess shown when {@code /tablist reload} succeeds
 * @param reloadError shown when a reload fails; supports {@code <error>}
 * @param status the status line; supports {@code <viewers>}, {@code <hitrate>}, {@code <updates>},
 *     {@code <frame>}
 * @param toggleOn shown when a viewer enables the custom tab list
 * @param toggleOff shown when a viewer falls back to the vanilla tab list
 * @param previewHeader the label printed above the header in {@code /tablist preview}
 * @param previewFooter the label printed above the footer in {@code /tablist preview}
 * @param placeholdersHeader the heading of the {@code /tablist placeholders} listing
 * @param placeholdersEntry one catalogue row; supports {@code <name>} and {@code <description>}
 * @param placeholdersPapi the PlaceholderAPI note, shown only when PlaceholderAPI is installed
 */
@ConfigSerializable
public record Messages(
    @Setting("reload-success") String reloadSuccess,
    @Setting("reload-error") String reloadError,
    String status,
    @Setting("toggle-on") String toggleOn,
    @Setting("toggle-off") String toggleOff,
    @Setting("preview-header") String previewHeader,
    @Setting("preview-footer") String previewFooter,
    @Setting("placeholders-header") String placeholdersHeader,
    @Setting("placeholders-entry") String placeholdersEntry,
    @Setting("placeholders-papi") String placeholdersPapi) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if any template is {@code null}
   */
  public Messages {
    Objects.requireNonNull(reloadSuccess, "reloadSuccess");
    Objects.requireNonNull(reloadError, "reloadError");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(toggleOn, "toggleOn");
    Objects.requireNonNull(toggleOff, "toggleOff");
    Objects.requireNonNull(previewHeader, "previewHeader");
    Objects.requireNonNull(previewFooter, "previewFooter");
    Objects.requireNonNull(placeholdersHeader, "placeholdersHeader");
    Objects.requireNonNull(placeholdersEntry, "placeholdersEntry");
    Objects.requireNonNull(placeholdersPapi, "placeholdersPapi");
  }

  /**
   * The built-in default templates, mirrored by the bundled {@code config.yml}.
   *
   * @return the default messages
   */
  public static Messages defaults() {
    return new Messages(
        "<green>Tablist configuration reloaded.</green>",
        "<red>Reload failed: <error></red>",
        "<gray>Viewers: <white><viewers></white> · Cache hit-rate: <white><hitrate></white>"
            + " · Updates/min: <white><updates></white> · Frame: <white><frame></white></gray>",
        "<green>Custom tab list enabled.</green>",
        "<yellow>Custom tab list disabled — showing the vanilla tab.</yellow>",
        "<gray>──── <white>Header preview</white> ────</gray>",
        "<gray>──── <white>Footer preview</white> ────</gray>",
        "<gray>Available placeholders <white>(resolved per viewer)</white>:</gray>",
        "<white><name></white> <gray>— <description></gray>",
        "<gray>PlaceholderAPI detected — <white>all its placeholders</white> work here too.</gray>");
  }
}
