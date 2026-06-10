package com.hanielfialho.tablist.core.text;

import com.hanielfialho.tablist.core.model.ViewerId;
import com.hanielfialho.tablist.core.port.PlaceholderResolver;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A first-class collection of placeholder resolvers tried in order: the first one that changes the
 * input wins.
 *
 * <p>Each resolver is asked, in declaration order, to resolve the template; the first whose result
 * differs from the input is taken and the rest are skipped. If none changes the input, the original
 * template is returned unchanged.
 */
public final class CompositePlaceholderResolver implements PlaceholderResolver {

  private final List<PlaceholderResolver> resolvers;

  /**
   * Creates a composite over an ordered list of resolvers.
   *
   * @param resolvers the resolvers to try, in priority order; never {@code null}
   */
  public CompositePlaceholderResolver(List<PlaceholderResolver> resolvers) {
    this.resolvers = List.copyOf(resolvers);
  }

  /**
   * Creates a composite from an ordered array of resolvers.
   *
   * @param resolvers the resolvers to try, in priority order
   * @return a new composite
   */
  public static CompositePlaceholderResolver of(PlaceholderResolver... resolvers) {
    return new CompositePlaceholderResolver(List.of(resolvers));
  }

  @Override
  public CompletableFuture<String> resolve(ViewerId viewer, String raw) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(raw, "raw");
    return resolveFrom(0, viewer, raw);
  }

  private CompletableFuture<String> resolveFrom(int index, ViewerId viewer, String raw) {
    if (index >= resolvers.size()) {
      return CompletableFuture.completedFuture(raw);
    }
    return resolvers
        .get(index)
        .resolve(viewer, raw)
        .thenCompose(result -> pick(index, viewer, raw, result));
  }

  private CompletableFuture<String> pick(int index, ViewerId viewer, String raw, String result) {
    if (!result.equals(raw)) {
      return CompletableFuture.completedFuture(result);
    }
    return resolveFrom(index + 1, viewer, raw);
  }
}
