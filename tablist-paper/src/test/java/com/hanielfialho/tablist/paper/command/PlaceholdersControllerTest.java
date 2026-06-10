package com.hanielfialho.tablist.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.commandframework.domain.value.PlayerId;
import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.commandframework.paper.feedback.FeedbackAudience;
import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.text.PlaceholderCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

/** Verifies {@code /tablist placeholders} lists the catalogue and the PlaceholderAPI note. */
class PlaceholdersControllerTest {

  private static final MiniMessage MINI = MiniMessage.miniMessage();

  private static Feedback feedback() {
    return new Feedback(new ActiveConfig(TabConfig.defaults(), () -> {}));
  }

  @Test
  void listsHeaderAndEveryBuiltinPlaceholderWithoutPapiNote() {
    RecordingActor actor = new RecordingActor();
    new PlaceholdersController(feedback(), false).placeholders(actor);

    TabConfig config = TabConfig.defaults();
    List<Component> expected = new ArrayList<>();
    expected.add(MINI.deserialize(config.messages().placeholdersHeader()));
    for (PlaceholderCatalog.Entry entry : PlaceholderCatalog.builtins()) {
      expected.add(
          MINI.deserialize(
              config.messages().placeholdersEntry(),
              net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed(
                  "name", entry.token()),
              net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed(
                  "description", entry.description())));
    }

    assertEquals(expected, actor.messages, "header followed by one line per built-in placeholder");
  }

  @Test
  void appendsThePapiNoteWhenPlaceholderApiIsPresent() {
    RecordingActor actor = new RecordingActor();
    new PlaceholdersController(feedback(), true).placeholders(actor);

    int expectedCount = 1 + PlaceholderCatalog.builtins().size() + 1;
    assertEquals(expectedCount, actor.messages.size(), "header + entries + PAPI note");
    assertTrue(
        actor
            .messages
            .get(actor.messages.size() - 1)
            .equals(MINI.deserialize(TabConfig.defaults().messages().placeholdersPapi())),
        "the last line is the PlaceholderAPI note");
  }

  /** A command sender that records every component it is sent, in order. */
  static final class RecordingActor implements CommandActor, FeedbackAudience {

    final List<Component> messages = new ArrayList<>();
    private final UUID id = UUID.randomUUID();

    @Override
    public void sendInfo(Component message) {
      messages.add(message);
    }

    @Override
    public void sendError(Component message) {
      messages.add(message);
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
