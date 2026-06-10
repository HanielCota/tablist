package com.hanielfialho.tablist.core.model;

import java.util.List;
import java.util.Objects;

/**
 * A non-empty, ordered sequence of renderable frames used to animate a single text slot.
 *
 * <p>A static (non-animated) slot is simply a {@code Frames} with one frame. The {@link
 * #frameAt(long)} method cycles through the frames so callers can drive an animation from a tick
 * counter.
 *
 * @param values the frames, in display order; copied defensively, never empty
 */
public record Frames(List<Renderable> values) {

  /**
   * Canonical constructor: copies the frames and rejects empty or {@code null}-containing input.
   *
   * @throws NullPointerException if {@code values} or any frame is {@code null}
   * @throws IllegalArgumentException if {@code values} is empty
   */
  public Frames {
    Objects.requireNonNull(values, "values");
    values = List.copyOf(values);
    requireNonEmpty(values);
  }

  /**
   * Builds a single-frame (static) sequence.
   *
   * @param frame the only frame
   * @return a {@code Frames} containing just {@code frame}
   */
  public static Frames single(Renderable frame) {
    return new Frames(List.of(frame));
  }

  private static void requireNonEmpty(List<Renderable> frames) {
    if (frames.isEmpty()) {
      throw new IllegalArgumentException("frames must not be empty");
    }
  }

  /**
   * Returns the frame to display at the given tick, cycling from the start when the tick exceeds
   * the frame count.
   *
   * @param tick the monotonic tick counter; negative values are wrapped too
   * @return the frame for {@code tick}
   */
  public Renderable frameAt(long tick) {
    return values.get(Math.floorMod(tick, values.size()));
  }
}
