package com.hanielfialho.tablist.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;

/**
 * Inspects a fully-loaded {@link TabConfig} for likely mistakes that pass schema validation but
 * still misbehave at render time, and reports them as readable warnings.
 *
 * <p>The configuration is already structurally valid by the time this runs (the {@link
 * ConfigLoader} rejects malformed YAML and out-of-range values). What it cannot catch are two
 * common, silent mistakes:
 *
 * <ul>
 *   <li>an <strong>unclosed MiniMessage tag</strong> — {@code <gradient:#a:#b>Name} with no {@code
 *       </gradient>} — which renders as plain text with no error, leaving an admin asking "why is
 *       my gradient not showing?". Every template is parsed in MiniMessage's strict mode, which
 *       flags an opened-but-not-closed tag while leaving Tablist's own self-closing tags ({@code
 *       <error>}, {@code <viewers>}) and {@code %placeholders%} untouched.
 *   <li>a <strong>name format with no placeholder at all</strong> — every player's row would then
 *       show the exact same static text, which is almost never intended.
 * </ul>
 *
 * <p>It deliberately does <strong>not</strong> warn about unknown {@code %placeholders%}: with
 * PlaceholderAPI installed almost any token may be valid, so such a check would be mostly false
 * positives.
 */
public final class ConfigValidator {

  private final MiniMessage strict;

  /** Creates a validator that parses templates in MiniMessage's strict mode. */
  public ConfigValidator() {
    this(MiniMessage.builder().strict(true).build());
  }

  /**
   * Creates a validator with an explicit MiniMessage instance (for tests).
   *
   * @param strict the instance to parse templates with; should be strict; never {@code null}
   */
  public ConfigValidator(MiniMessage strict) {
    this.strict = Objects.requireNonNull(strict, "strict");
  }

  /**
   * Returns the warnings for the given configuration, in reading order.
   *
   * @param config the loaded configuration to inspect; never {@code null}
   * @return the readable warnings; empty when nothing looks wrong
   */
  public List<String> warningsFor(TabConfig config) {
    Objects.requireNonNull(config, "config");
    List<String> warnings = new ArrayList<>();
    checkFrames("header", config.header(), warnings);
    checkFrames("footer", config.footer(), warnings);
    checkNameFormat(config.nameFormat(), warnings);
    check("messages.reload-success", config.messages().reloadSuccess(), warnings);
    check("messages.reload-error", config.messages().reloadError(), warnings);
    check("messages.status", config.messages().status(), warnings);
    check("messages.toggle-on", config.messages().toggleOn(), warnings);
    check("messages.toggle-off", config.messages().toggleOff(), warnings);
    return List.copyOf(warnings);
  }

  /**
   * Computes the warnings for {@code config} and, when there are any, emits a count header followed
   * by one line per warning to {@code sink}. Nothing is emitted for a clean configuration.
   *
   * @param config the loaded configuration to inspect; never {@code null}
   * @param sink receives each ready-to-log line; never {@code null}
   */
  public void report(TabConfig config, Consumer<String> sink) {
    Objects.requireNonNull(sink, "sink");
    List<String> warnings = warningsFor(config);
    if (warnings.isEmpty()) {
      return;
    }
    sink.accept(warnings.size() + " Tablist configuration warning(s) — the tab list still loads:");
    warnings.forEach(warning -> sink.accept("  • " + warning));
  }

  private void checkFrames(String where, FrameSection section, List<String> warnings) {
    List<String> frames = section.frames();
    for (int i = 0; i < frames.size(); i++) {
      check(where + ".frames[" + i + "]", frames.get(i), warnings);
    }
  }

  private void checkNameFormat(NameFormat format, List<String> warnings) {
    check("name-format.prefix", format.prefix(), warnings);
    check("name-format.name", format.name(), warnings);
    check("name-format.suffix", format.suffix(), warnings);
    String combined = format.prefix() + format.name() + format.suffix();
    if (!combined.contains("%")) {
      warnings.add(
          "name-format: no %placeholder% anywhere — every player's row would show identical text"
              + " (did you mean to include %player_name%?)");
    }
  }

  private void check(String where, String template, List<String> warnings) {
    try {
      strict.deserialize(template);
    } catch (ParsingException invalid) {
      warnings.add(where + ": " + firstLine(invalid.getMessage()) + " (is a tag left unclosed?)");
    }
  }

  private static String firstLine(String message) {
    if (message == null) {
      return "invalid MiniMessage";
    }
    int newline = message.indexOf('\n');
    return newline < 0 ? message : message.substring(0, newline);
  }
}
