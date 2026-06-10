/**
 * The status feature: a snapshot of runtime health for the {@code /tablist status} command.
 *
 * <p>{@link com.hanielfialho.tablist.core.status.StatusReporter StatusReporter} gathers the active
 * viewer count, the cache hit-rate, the rolling updates-per-minute and the current animation frame
 * into an immutable {@link com.hanielfialho.tablist.core.status.TablistStatus TablistStatus}. It is
 * the service the command delegates to; the command itself only renders the result.
 */
package com.hanielfialho.tablist.core.status;
