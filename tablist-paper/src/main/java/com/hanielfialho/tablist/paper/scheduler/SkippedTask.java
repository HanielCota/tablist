package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.port.ScheduledTask;

/**
 * A {@link ScheduledTask} that was never accepted by the platform — for example because the target
 * player had already logged out when the task was scheduled.
 */
public final class SkippedTask implements ScheduledTask {

  @Override
  public void cancel() {
    // Nothing was scheduled, so there is nothing to cancel.
  }

  @Override
  public boolean scheduled() {
    return false;
  }
}
