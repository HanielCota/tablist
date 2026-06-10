package com.hanielfialho.tablist.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.commandframework.domain.value.PlayerId;
import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.commandframework.paper.feedback.FeedbackAudience;
import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.ConfigLoader;
import com.hanielfialho.tablist.core.config.ConfigReloader;
import com.hanielfialho.tablist.core.config.DirtyAllViewers;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.ViewerDirectory;
import com.hanielfialho.tablist.core.state.DirtyTracker;
import com.hanielfialho.tablist.core.state.TablistToggle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the command controllers — the services the thin {@code /tablist} facade delegates to —
 * drive the core and render the configured MiniMessage feedback. The framework's own dispatch is
 * covered by its {@code CommandTestHarness}, which needs a server source stack; here we test the
 * behaviour Tablist owns, with no server.
 */
class TablistCommandsTest {

  private static final MiniMessage MINI = MiniMessage.miniMessage();

  @Test
  void toggleFlipsTheViewerMarksThemDirtyAndConfirms() {
    ActiveConfig config = new ActiveConfig(TabConfig.defaults(), () -> {});
    TablistToggle toggle = new TablistToggle();
    DirtyTracker dirty = new DirtyTracker();
    ToggleController controller = new ToggleController(toggle, dirty, new Feedback(config));
    UUID id = UUID.randomUUID();
    FakeActor actor = new FakeActor(id);

    controller.toggle(actor);

    ViewerId viewer = new ViewerId(id);
    assertFalse(toggle.isEnabled(viewer), "the first toggle switches the custom tab off");
    assertTrue(dirty.drainDirty().contains(viewer), "the viewer must be marked dirty");
    assertEquals(
        MINI.deserialize(config.current().messages().toggleOff()),
        actor.lastMessage,
        "the toggle-off line must be sent");
  }

  @Test
  void reloadReportsSuccessAndAppliesTheNewConfig(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("config.yml");
    Files.writeString(file, "refresh:\n  placeholder-refresh-seconds: 5\n");
    ActiveConfig config = new ActiveConfig(TabConfig.defaults(), () -> {});
    ReloadController controller =
        new ReloadController(reloader(file, config), new Feedback(config));
    FakeActor actor = new FakeActor(UUID.randomUUID());

    controller.reload(actor);

    assertEquals(5, config.current().refresh().placeholderRefreshSeconds(), "new value must apply");
    assertEquals(
        MINI.deserialize(config.current().messages().reloadSuccess()),
        actor.lastMessage,
        "the reload-success line must be sent");
  }

  @Test
  void reloadKeepsConfigAndReportsErrorOnInvalidYaml(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("config.yml");
    Files.writeString(file, "header:\n  frames: [unterminated");
    ActiveConfig config = new ActiveConfig(TabConfig.defaults(), () -> {});
    TabConfig before = config.current();
    ReloadController controller =
        new ReloadController(reloader(file, config), new Feedback(config));
    FakeActor actor = new FakeActor(UUID.randomUUID());

    controller.reload(actor);

    assertSame(before, config.current(), "an invalid reload must keep the previous config");
    assertNotNull(actor.lastMessage, "an error line must be sent");
    assertNotEquals(
        MINI.deserialize(config.current().messages().reloadSuccess()),
        actor.lastMessage,
        "the success line must not be sent on failure");
  }

  private static ConfigReloader reloader(Path file, ActiveConfig config) {
    ViewerDirectory directory = List::of;
    return new ConfigReloader(
        new ConfigLoader(file), config, new DirtyAllViewers(new DirtyTracker(), directory));
  }

  /** A command sender that captures the Component feedback it is sent. */
  private static final class FakeActor implements CommandActor, FeedbackAudience {

    private final UUID id;
    private Component lastMessage;

    FakeActor(UUID id) {
      this.id = id;
    }

    @Override
    public void sendInfo(Component message) {
      this.lastMessage = message;
    }

    @Override
    public void sendError(Component message) {
      this.lastMessage = message;
    }

    @Override
    public Optional<PlayerId> playerId() {
      return Optional.of(PlayerId.of(id));
    }

    @Override
    public boolean hasPermission(String permission) {
      return true;
    }

    @Override
    public String name() {
      return "Tester";
    }

    @Override
    public Locale locale() {
      return Locale.ROOT;
    }

    @Override
    public void sendSuccess(String message) {}

    @Override
    public void sendError(String message) {}

    @Override
    public void sendInfo(String message) {}
  }
}
