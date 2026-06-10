package com.hanielfialho.tablist.paper.command;

import com.hanielfialho.commandframework.domain.value.PlayerId;
import com.hanielfialho.commandframework.execution.CommandActor;
import com.hanielfialho.tablist.core.config.ActiveConfig;
import com.hanielfialho.tablist.core.config.TabConfig;
import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.text.TextResolutionPipeline;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Application service behind {@code /tablist preview}: resolves the configured header and footer
 * for the sender and echoes them back, so an admin can see exactly what their templates render to —
 * including multi-line layouts and every animation frame — without needing a second player online
 * or a guess about how {@code <newline>} will look.
 *
 * <p>Each frame is resolved through the same {@link TextResolutionPipeline} the live tab list uses,
 * so placeholders evaluate identically to the real thing. The frames are chained with {@code
 * thenCompose} so they are echoed strictly in reading order even when a resolver completes
 * asynchronously (e.g. PlaceholderAPI).
 */
public final class PreviewController {

  private final ActiveConfig config;
  private final TextResolutionPipeline pipeline;
  private final Feedback feedback;

  /**
   * Creates the controller.
   *
   * @param config the active configuration supplying the templates; never {@code null}
   * @param pipeline resolves each template for the viewer; never {@code null}
   * @param feedback renders the labels and echoes the resolved frames; never {@code null}
   */
  public PreviewController(
      ActiveConfig config, TextResolutionPipeline pipeline, Feedback feedback) {
    this.config = Objects.requireNonNull(config, "config");
    this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    this.feedback = Objects.requireNonNull(feedback, "feedback");
  }

  /**
   * Resolves and echoes the header and footer for the sending player.
   *
   * @param actor the command sender; must be a player (enforced by {@code @PlayerOnly})
   */
  public void preview(CommandActor actor) {
    Optional<PlayerId> playerId = actor.playerId();
    if (playerId.isEmpty()) {
      return;
    }
    ViewerId viewer = new ViewerId(playerId.get().value());
    TabConfig current = config.current();

    CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
    chain = chain.thenRun(() -> feedback.previewHeaderLabel(actor));
    chain = echoFrames(chain, actor, viewer, current.header().frames());
    chain = chain.thenRun(() -> feedback.previewFooterLabel(actor));
    echoFrames(chain, actor, viewer, current.footer().frames());
  }

  private CompletableFuture<Void> echoFrames(
      CompletableFuture<Void> chain, CommandActor actor, ViewerId viewer, List<String> frames) {
    CompletableFuture<Void> result = chain;
    for (String frame : frames) {
      result =
          result.thenCompose(
              ignored ->
                  pipeline
                      .resolve(viewer, frame)
                      .thenAccept(component -> feedback.component(actor, component)));
    }
    return result;
  }
}
