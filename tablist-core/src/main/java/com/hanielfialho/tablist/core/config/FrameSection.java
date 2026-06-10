package com.hanielfialho.tablist.core.config;

import java.util.List;
import java.util.Objects;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * An animated text slot (header or footer): the ordered MiniMessage frames and how long each stays
 * on screen.
 *
 * @param frames the MiniMessage frames, cycled in order; never empty
 * @param intervalTicks the number of server ticks each frame is shown; at least {@code 1}
 */
@ConfigSerializable
public record FrameSection(List<String> frames, @Setting("interval-ticks") int intervalTicks) {

  /**
   * Canonical constructor: copies the frames and validates the slot.
   *
   * @throws NullPointerException if {@code frames} or any frame is {@code null}
   * @throws IllegalArgumentException if {@code frames} is empty or {@code intervalTicks < 1}
   */
  public FrameSection {
    Objects.requireNonNull(frames, "frames");
    frames = List.copyOf(frames);
    requireNonEmpty(frames);
    requirePositive(intervalTicks);
  }

  private static void requireNonEmpty(List<String> frames) {
    if (frames.isEmpty()) {
      throw new IllegalArgumentException("frames must not be empty");
    }
  }

  private static void requirePositive(int intervalTicks) {
    if (intervalTicks < 1) {
      throw new IllegalArgumentException("interval-ticks must be >= 1");
    }
  }
}
