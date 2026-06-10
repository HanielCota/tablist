package com.hanielfialho.tablist.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies the {@code lines:} convenience syntax expands into newline-joined static frames. */
class LineFrameSugarTest {

  private static TabConfig loadYaml(Path dir, String yaml) throws IOException, ConfigException {
    Path file = dir.resolve("config.yml");
    Files.writeString(file, yaml);
    return new ConfigLoader(file).load();
  }

  @Test
  void joinsLinesIntoOneStaticFrameWithNewlineTags(@TempDir Path dir)
      throws IOException, ConfigException {
    TabConfig config =
        loadYaml(
            dir,
            """
            header:
              lines:
                - "MY SERVER"
                - "<gray>welcome</gray>"
            """);

    assertEquals(
        List.of("MY SERVER<newline><gray>welcome</gray>"),
        config.header().frames(),
        "each line becomes a rendered line of a single static frame");
  }

  @Test
  void blankLineEntryBecomesABlankRenderedLine(@TempDir Path dir)
      throws IOException, ConfigException {
    TabConfig config =
        loadYaml(
            dir,
            """
            footer:
              lines:
                - "top"
                - ""
                - "bottom"
            """);

    assertEquals(
        List.of("top<newline><newline>bottom"),
        config.footer().frames(),
        "an empty entry yields a blank line — the admin's 'skip a line'");
  }

  @Test
  void explicitFramesWinWhenBothAreGiven(@TempDir Path dir) throws IOException, ConfigException {
    TabConfig config =
        loadYaml(
            dir,
            """
            header:
              frames:
                - "animated-a"
                - "animated-b"
              lines:
                - "ignored"
            """);

    assertEquals(
        List.of("animated-a", "animated-b"),
        config.header().frames(),
        "frames take precedence; the lines sugar is dropped");
  }

  @Test
  void aSectionWithoutLinesKeepsItsDefaultFrames(@TempDir Path dir)
      throws IOException, ConfigException {
    TabConfig config = loadYaml(dir, "footer:\n  lines:\n    - \"only footer\"\n");

    assertEquals(
        TabConfig.defaults().header().frames(),
        config.header().frames(),
        "an untouched header still falls back to the default frames");
    assertEquals(List.of("only footer"), config.footer().frames());
  }

  @Test
  void aSingleScalarLineIsAcceptedAsOneLine(@TempDir Path dir) throws IOException, ConfigException {
    TabConfig config = loadYaml(dir, "header:\n  lines: \"just one\"\n");

    assertEquals(List.of("just one"), config.header().frames());
  }

  @Test
  void anEmptyLinesListLeavesTheDefaultFramesInPlace(@TempDir Path dir)
      throws IOException, ConfigException {
    TabConfig config = loadYaml(dir, "header:\n  lines: []\n");

    assertEquals(
        TabConfig.defaults().header().frames(),
        config.header().frames(),
        "an empty lines list is treated as absent, not as an invalid empty frame");
  }

  @Test
  void invalidYamlStillSurfacesAsConfigException(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("config.yml");
    Files.writeString(file, "header:\n  lines: [unterminated");

    assertThrows(ConfigException.class, () -> new ConfigLoader(file).load());
  }
}
