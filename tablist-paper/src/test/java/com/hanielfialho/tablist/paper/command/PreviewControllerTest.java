package com.hanielfialho.tablist.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.FrameSection;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import com.hanielfialho.tablist.core.text.ResolvedTextCache;
import com.hanielfialho.tablist.core.text.TextResolutionPipeline;
import com.hanielfialho.tablist.core.text.TextResolver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

/**
 * Verifies {@code /tablist preview} echoes the header and footer, in reading order, each label
 * followed by its resolved frames.
 */
class PreviewControllerTest {

  private static final MiniMessage MINI = MiniMessage.miniMessage();

  private static TextResolutionPipeline passthroughPipeline() {
    PlaceholderResolver passthrough =
        (viewer, raw) -> CompletableFuture.completedFuture(raw); // leave placeholders literal
    return new TextResolutionPipeline(
        ResolvedTextCache.create(Duration.ofSeconds(3)),
        new TextResolver(passthrough, MINI),
        new DirtyTracker());
  }

  // Gradient frames deserialize to lazy VirtualComponents that never compare equal across two
  // parses; plain frames give value-comparable TextComponents, so the assertion tests order, not
  // MiniMessage internals.
  private static TabConfig configWithPlainFrames() {
    TabConfig base = TabConfig.defaults();
    return new TabConfig(
        new FrameSection(List.of("<white>H1</white>", "<yellow>H2</yellow>"), 20),
        new FrameSection(List.of("<gray>Online: %online%</gray>"), 20),
        base.nameFormat(),
        base.sorting(),
        base.refresh(),
        base.messages());
  }

  @Test
  void echoesHeaderLabelThenHeaderFramesThenFooterLabelThenFooterFrames() {
    TabConfig config = configWithPlainFrames();
    ActiveConfig active = new ActiveConfig(config, () -> {});
    Feedback feedback = new Feedback(active);
    PreviewController controller = new PreviewController(active, passthroughPipeline(), feedback);
    RecordingActor actor = new RecordingActor();

    controller.preview(actor);

    List<Component> expected = new ArrayList<>();
    expected.add(MINI.deserialize(config.messages().previewHeader()));
    config.header().frames().forEach(frame -> expected.add(MINI.deserialize(frame)));
    expected.add(MINI.deserialize(config.messages().previewFooter()));
    config.footer().frames().forEach(frame -> expected.add(MINI.deserialize(frame)));

    assertEquals(expected.size(), actor.messages.size(), "message count");
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actor.messages.get(i), "message at index " + i);
    }
  }
}
