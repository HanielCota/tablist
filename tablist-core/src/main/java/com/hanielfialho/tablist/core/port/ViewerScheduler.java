package com.hanielfialho.tablist.core.port;

/**
 * Schedules tasks on a single viewer's context.
 *
 * <p>On Paper this is the main thread; on Folia it is the player's region thread. Either way, the
 * task runs on the thread that is allowed to touch that player's connection, which is the threading
 * invariant the coalesced re-render relies on.
 */
public interface ViewerScheduler {

  /**
   * Schedules the task to run once on the viewer's context next tick.
   *
   * @param task the work to run; never {@code null}
   * @return a handle to cancel it before it runs
   */
  ScheduledTask schedule(Runnable task);

  /**
   * Schedules the task to run repeatedly on the viewer's context, every {@code period} ticks, with
   * the first run after the same delay. Used to drive animations and the periodic flush; the
   * returned handle must be cancelled when the viewer leaves.
   *
   * @param task the work to run each period; never {@code null}
   * @param period the period in ticks; must be {@code >= 1}
   * @return a handle to cancel the repeating task
   */
  ScheduledTask scheduleRepeating(Runnable task, long period);
}
