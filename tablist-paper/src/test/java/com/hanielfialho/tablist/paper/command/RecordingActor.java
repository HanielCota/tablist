package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.domain.value.PlayerId;
import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.commandframework.paper.feedback.FeedbackAudience;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;

/**
 * A test command sender that records, in order, every {@link Component} it is sent through the
 * {@link FeedbackAudience} (rich-feedback) path — the only path Tablist's controllers use.
 */
final class RecordingActor implements CommandActor, FeedbackAudience {

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

  // The String-based CommandActor sinks are intentionally unused: Tablist's controllers send rich
  // Components through FeedbackAudience, so these tests never exercise the plain-text path.
  @Override
  public void sendSuccess(String message) {
    // unused — see class note above
  }

  @Override
  public void sendError(String message) {
    // unused — see class note above
  }

  @Override
  public void sendInfo(String message) {
    // unused — see class note above
  }
}
