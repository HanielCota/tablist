package com.hanielfialho.tablist.core.config;

import com.hanielfialho.tablist.core.model.Frames;
import com.hanielfialho.tablist.core.model.Renderable;
import java.util.List;
import java.util.Objects;

/**
 * A first-class collection of animation frames that resolves which frame to show at a given tick.
 *
 * <p>It holds the frames and the per-frame interval, and nothing else: it spawns no thread and
 * keeps no mutable cursor. The {@link com.hanielfialho.tablist.core.state.TabFlusher TabFlusher}
 * owns the tick (advanced via the {@code TabScheduler}) and simply asks {@link #frameAt(long)} for
 * the current frame, so animation stays deterministic and testable.
 */
public final class AnimationFrameCycler {

  private final Frames frames;
  private final int intervalTicks;

  /**
   * Creates a cycler over the given frames.
   *
   * @param frames the frames to cycle; never {@code null}
   * @param intervalTicks how many ticks each frame is shown; at least {@code 1}
   * @throws IllegalArgumentException if {@code intervalTicks < 1}
   */
  public AnimationFrameCycler(Frames frames, int intervalTicks) {
    this.frames = Objects.requireNonNull(frames, "frames");
    this.intervalTicks = requirePositive(intervalTicks);
  }

  /**
   * Builds a cycler from raw MiniMessage frame strings.
   *
   * @param frames the MiniMessage frames; never empty
   * @param intervalTicks how many ticks each frame is shown; at least {@code 1}
   * @return a new cycler
   */
  public static AnimationFrameCycler of(List<String> frames, int intervalTicks) {
    Objects.requireNonNull(frames, "frames");
    return new AnimationFrameCycler(
        new Frames(frames.stream().map(Renderable::new).toList()), intervalTicks);
  }

  /**
   * Builds a cycler from a configuration {@link FrameSection}.
   *
   * @param section the header/footer section; never {@code null}
   * @return a new cycler
   */
  public static AnimationFrameCycler from(FrameSection section) {
    Objects.requireNonNull(section, "section");
    return of(section.frames(), section.intervalTicks());
  }

  /**
   * Returns the frame to display at the given tick. The frame advances once every {@code
   * intervalTicks} ticks and wraps around at the end of the sequence.
   *
   * @param tick the monotonic tick counter owned by the flusher
   * @return the frame for {@code tick}
   */
  public Renderable frameAt(long tick) {
    return frames.frameAt(Math.floorDiv(tick, intervalTicks));
  }

  private static int requirePositive(int intervalTicks) {
    if (intervalTicks < 1) {
      throw new IllegalArgumentException("interval-ticks must be >= 1");
    }
    return intervalTicks;
  }
}
