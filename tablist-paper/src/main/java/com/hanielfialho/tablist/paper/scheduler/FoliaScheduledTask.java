package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.port.ScheduledTask;
import java.util.Objects;

/** A {@link ScheduledTask} backed by a Folia region task. */
public final class FoliaScheduledTask implements ScheduledTask {

  private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;

  /**
   * Wraps a Folia scheduled task.
   *
   * @param task the Folia task; never {@code null}
   */
  public FoliaScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
    this.task = Objects.requireNonNull(task, "task");
  }

  @Override
  public void cancel() {
    task.cancel();
  }
}
