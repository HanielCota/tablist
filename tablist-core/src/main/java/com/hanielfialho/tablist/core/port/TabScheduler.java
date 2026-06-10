package com.hanielfialho.tablist.core.port;

import com.hanielfialho.tablist.core.model.ViewerId;
import java.util.concurrent.Executor;

/**
 * The scheduling strategy a platform provides to drive coalesced re-renders.
 *
 * <p>The core defines this contract and knows nothing of Bukkit or Folia. Paper and Folia each
 * supply an implementation; the chosen one is injected at boot so the rest of the framework stays
 * platform-agnostic. This mirrors the {@code MenuScheduler} pattern from MenuFramework.
 */
public interface TabScheduler {

  /**
   * Returns a scheduler bound to the given viewer's context.
   *
   * @param viewer the owner of the tab list to re-render; never {@code null}
   * @return a viewer-scoped scheduler
   */
  ViewerScheduler forViewer(ViewerId viewer);

  /**
   * Returns an executor that runs tasks on the platform's global tick context — the main thread on
   * Paper, the global region thread on Folia. Used for work that is not tied to a single viewer.
   *
   * @return the global executor; never {@code null}
   */
  Executor global();

  /**
   * Schedules a task to run repeatedly on the platform's global tick context, every {@code period}
   * ticks. Used to drive the periodic flush loop — the global thread only coalesces and dispatches;
   * the actual per-player work is routed back to each player's context.
   *
   * @param task the work to run each period; never {@code null}
   * @param period the period in ticks; must be {@code >= 1}
   * @return a handle to cancel the loop
   */
  ScheduledTask scheduleGlobalRepeating(Runnable task, long period);
}
