package com.hanielfialho.tablist.core.config;

import java.util.List;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Expands the {@code lines:} convenience syntax of a header/footer section into the canonical
 * {@code frames:} the rest of the configuration understands.
 *
 * <p>Writing a multi-line, static slot with explicit {@code <newline>} tags is error-prone, so a
 * section may instead list its lines:
 *
 * <pre>{@code
 * header:
 *   lines:
 *     - "MY SERVER"
 *     - ""            # a blank line — the "skip a line" an admin reaches for
 *     - "<gray>welcome</gray>"
 * }</pre>
 *
 * <p>which this expands to a single static frame {@code "MY
 * SERVER<newline><newline><gray>welcome</gray>"}. Each list entry becomes one rendered line; an
 * empty entry becomes a blank line. The expansion runs on the raw node before defaults are merged,
 * so a {@code lines}-only section is never overwritten by the default {@code frames}. If a section
 * sets both, {@code frames} wins and {@code lines} is dropped, keeping the animated form
 * authoritative.
 */
final class LineFrameSugar {

  private static final String NEWLINE = "<newline>";
  private static final String FRAMES = "frames";
  private static final String LINES = "lines";

  private LineFrameSugar() {}

  /**
   * Expands the {@code lines} sugar in the header and footer sections of the given root node.
   *
   * @param root the raw configuration node, as loaded from disk; never {@code null}
   * @throws SerializationException if a {@code lines} list cannot be read as strings
   */
  static void expand(ConfigurationNode root) throws SerializationException {
    expandSection(root.node("header"));
    expandSection(root.node("footer"));
  }

  private static void expandSection(ConfigurationNode section) throws SerializationException {
    ConfigurationNode lines = section.node(LINES);
    if (lines.virtual() || lines.empty()) {
      return;
    }
    if (!section.node(FRAMES).virtual()) {
      section.removeChild(LINES);
      return;
    }
    section.node(FRAMES).set(List.of(String.join(NEWLINE, readLines(lines))));
    section.removeChild(LINES);
  }

  private static List<String> readLines(ConfigurationNode lines) throws SerializationException {
    if (lines.isList()) {
      List<String> values = lines.getList(String.class);
      return values == null ? List.of() : values;
    }
    return List.of(lines.getString(""));
  }
}
