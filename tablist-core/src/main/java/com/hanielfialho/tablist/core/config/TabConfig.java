package com.hanielfialho.tablist.core.config;

import java.util.List;
import java.util.Objects;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * The root of the Tablist configuration, mapped one-to-one to {@code config.yml}.
 *
 * <p>This is a pure, immutable data record: its components mirror the top-level YAML keys (header,
 * footer, name-format, sorting, refresh). Behaviour lives in the surrounding classes ({@link
 * AnimationFrameCycler}, {@link ConfigReloader}); this type only carries validated values.
 *
 * @param header the animated header slot; never {@code null}
 * @param footer the animated footer slot; never {@code null}
 * @param nameFormat how each name is rendered; never {@code null}
 * @param sorting the ordered sorting groups; never {@code null}
 * @param refresh the placeholder refresh settings; never {@code null}
 * @param messages the command-feedback templates; never {@code null}
 */
@ConfigSerializable
public record TabConfig(
    FrameSection header,
    FrameSection footer,
    @Setting("name-format") NameFormat nameFormat,
    List<SortGroup> sorting,
    Refresh refresh,
    Messages messages) {

  /**
   * Canonical constructor: copies the sorting list and rejects {@code null} components.
   *
   * @throws NullPointerException if any component (or sorting entry) is {@code null}
   */
  public TabConfig {
    Objects.requireNonNull(header, "header");
    Objects.requireNonNull(footer, "footer");
    Objects.requireNonNull(nameFormat, "nameFormat");
    sorting = List.copyOf(sorting);
    Objects.requireNonNull(refresh, "refresh");
    Objects.requireNonNull(messages, "messages");
  }

  /**
   * Returns the built-in default configuration, mirrored by the bundled {@code config.yml}.
   *
   * @return the default {@code TabConfig}
   */
  public static TabConfig defaults() {
    return new TabConfig(
        new FrameSection(
            List.of(
                "<gradient:#00c6ff:#0072ff>Tablist</gradient>",
                "<gradient:#0072ff:#00c6ff>Tablist</gradient>"),
            20),
        new FrameSection(List.of("<gray>Online: <white>%online%</white></gray>"), 20),
        new NameFormat("", "<white>%player_name%</white>", ""),
        List.of(new SortGroup("admin", 0), new SortGroup("default", 100)),
        new Refresh(3),
        Messages.defaults());
  }
}
