package com.hanielfialho.tablist.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.text.PlaceholderCatalog;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
              Placeholder.unparsed("name", entry.token()),
              Placeholder.unparsed("description", entry.description())));
    }

    assertEquals(expected, actor.messages, "header followed by one line per built-in placeholder");
  }

  @Test
  void appendsThePapiNoteWhenPlaceholderApiIsPresent() {
    RecordingActor actor = new RecordingActor();
    new PlaceholdersController(feedback(), true).placeholders(actor);

    int expectedCount = 1 + PlaceholderCatalog.builtins().size() + 1;
    assertEquals(expectedCount, actor.messages.size(), "header + entries + PAPI note");
    assertEquals(
        MINI.deserialize(TabConfig.defaults().messages().placeholdersPapi()),
        actor.messages.get(actor.messages.size() - 1),
        "the last line is the PlaceholderAPI note");
  }
}
