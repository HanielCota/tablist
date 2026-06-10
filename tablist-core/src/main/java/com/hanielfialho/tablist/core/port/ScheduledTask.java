package com.hanielfialho.tablist.core.port;

/** A handle to a scheduled task, which can be cancelled before it runs. */
public interface ScheduledTask {

  /** Cancels the task if it has not run yet; a no-op once it has. */
  void cancel();

  /**
   * Tells whether the task was accepted by the platform scheduler.
   *
   * @return {@code true} when the task is pending or may still run
   */
  default boolean scheduled() {
    return true;
  }
}
