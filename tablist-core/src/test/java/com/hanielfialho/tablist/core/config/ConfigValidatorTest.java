package com.hanielfialho.tablist.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies the validator flags unclosed MiniMessage tags and placeholder-free name formats, while
 * staying quiet on the valid defaults and on Tablist's own self-closing tags.
 */
class ConfigValidatorTest {

  private final ConfigValidator validator = new ConfigValidator();

  private static TabConfig withHeaderFrames(List<String> frames) {
    TabConfig base = TabConfig.defaults();
    return new TabConfig(
        new FrameSection(frames, base.header().intervalTicks()),
        base.footer(),
        base.nameFormat(),
        base.sorting(),
        base.refresh(),
        base.messages());
  }

  private static TabConfig withNameFormat(NameFormat format) {
    TabConfig base = TabConfig.defaults();
    return new TabConfig(
        base.header(), base.footer(), format, base.sorting(), base.refresh(), base.messages());
  }

  @Test
  void defaultsProduceNoWarnings() {
    assertEquals(List.of(), validator.warningsFor(TabConfig.defaults()));
  }

  @Test
  void selfClosingTagsAndPlaceholdersAreNotWarnings() {
    // <error>/<viewers> are Tablist's own tags and %papi% is a placeholder: none are unclosed tags.
    TabConfig config = withHeaderFrames(List.of("<white><viewers> <error></white> %some_papi%"));

    assertEquals(List.of(), validator.warningsFor(config));
  }

  @Test
  void anUnclosedTagIsReportedWithItsLocation() {
    TabConfig config = withHeaderFrames(List.of("ok", "<gradient:#00c6ff:#0072ff>Tablist"));

    List<String> warnings = validator.warningsFor(config);

    assertEquals(1, warnings.size(), "only the broken frame warns");
    assertTrue(
        warnings.get(0).startsWith("header.frames[1]: "),
        "the warning names the exact offending frame, got: " + warnings.get(0));
  }

  @Test
  void anUnclosedTagInAMessageTemplateIsAlsoReported() {
    TabConfig base = TabConfig.defaults();
    Messages defaults = base.messages();
    Messages broken =
        new Messages(
            "<green>ok</green>",
            defaults.reloadError(),
            defaults.status(),
            defaults.toggleOn(),
            "<bold>oops",
            defaults.previewHeader(),
            defaults.previewFooter(),
            defaults.placeholdersHeader(),
            defaults.placeholdersEntry(),
            defaults.placeholdersPapi());
    TabConfig config =
        new TabConfig(
            base.header(),
            base.footer(),
            base.nameFormat(),
            base.sorting(),
            base.refresh(),
            broken);

    List<String> warnings = validator.warningsFor(config);

    assertEquals(1, warnings.size());
    assertTrue(warnings.get(0).startsWith("messages.toggle-off: "), warnings.get(0));
  }

  @Test
  void aNameFormatWithoutAnyPlaceholderWarns() {
    TabConfig config = withNameFormat(new NameFormat("", "<white>Player</white>", ""));

    List<String> warnings = validator.warningsFor(config);

    assertEquals(1, warnings.size());
    assertTrue(warnings.get(0).startsWith("name-format: "), warnings.get(0));
  }

  @Test
  void aPlaceholderInThePrefixSatisfiesTheNameFormatCheck() {
    TabConfig config = withNameFormat(new NameFormat("%player_name% ", "<white>guest</white>", ""));

    assertEquals(List.of(), validator.warningsFor(config));
  }
}
