package com.hanielfialho.tablist.core.model;

import java.util.Objects;

/**
 * The header and footer of a viewer's tab list, each a sequence of animation {@link Frames}.
 *
 * <p>Equality is structural (over both frame sequences), so a changed footer makes the whole {@code
 * HeaderFooter} unequal to the previous one — which is exactly how the flusher notices that the
 * header/footer needs to be re-sent while the entries do not.
 *
 * @param header the header frames; never {@code null}
 * @param footer the footer frames; never {@code null}
 */
public record HeaderFooter(Frames header, Frames footer) {

  /**
   * Canonical constructor.
   *
   * @throws NullPointerException if {@code header} or {@code footer} is {@code null}
   */
  public HeaderFooter {
    Objects.requireNonNull(header, "header");
    Objects.requireNonNull(footer, "footer");
  }

  /**
   * Returns an empty (single blank frame) header and footer.
   *
   * @return a blank {@code HeaderFooter}
   */
  public static HeaderFooter empty() {
    return new HeaderFooter(Frames.single(Renderable.empty()), Frames.single(Renderable.empty()));
  }

  /**
   * Builds a static header/footer from two raw templates.
   *
   * @param header the header template
   * @param footer the footer template
   * @return a single-frame {@code HeaderFooter}
   */
  public static HeaderFooter of(String header, String footer) {
    return new HeaderFooter(
        Frames.single(new Renderable(header)), Frames.single(new Renderable(footer)));
  }
}
