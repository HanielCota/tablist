package com.hanielfialho.tablist.paper.scheduler;

import com.hanielfialho.tablist.core.port.ScheduledTask;
import java.util.Objects;
import org.bukkit.scheduler.BukkitTask;

/** A {@link ScheduledTask} backed by a Bukkit task (Paper). */
public final class BukkitScheduledTask implements ScheduledTask {

  private final BukkitTask task;

  /**
   * Wraps a Bukkit task.
   *
   * @param task the scheduled Bukkit task; never {@code null}
   */
  public BukkitScheduledTask(BukkitTask task) {
    this.task = Objects.requireNonNull(task, "task");
  }

  @Override
  public void cancel() {
    task.cancel();
  }
}
