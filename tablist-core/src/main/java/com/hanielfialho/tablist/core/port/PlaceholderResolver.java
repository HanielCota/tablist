package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.concurrent.CompletableFuture;

/**
 * Resolves placeholders inside a renderable template for a given viewer, asynchronously.
 *
 * <p>Resolution may hit slow sources (economy, permissions, remote services), so it returns a
 * {@link CompletableFuture} and must never block the caller. The core defines the contract only;
 * the adapter supplies the implementation.
 */
@FunctionalInterface
public interface PlaceholderResolver {

  /**
   * Resolves every placeholder in {@code raw} from the viewer's perspective.
   *
   * @param viewer the player the placeholders are evaluated for; never {@code null}
   * @param raw the template text to resolve; never {@code null}
   * @return a future completing with the fully resolved text; never {@code null}
   */
  CompletableFuture<String> resolve(ViewerId viewer, String raw);
}
