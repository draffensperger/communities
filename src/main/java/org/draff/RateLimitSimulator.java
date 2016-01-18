package org.draff;

import twitter4j.RateLimitStatus;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by dave on 1/18/16.
 */
public class RateLimitSimulator {
  // Twitter rate limits reset every 15 minutes
  private static final long PERIOD_LENGTH_MS = 15 * 60 * 1000;

  private int limitPerPeriod;
  private long initialResetTime;

  private Queue<Long> taskPerformedTimes;

  public RateLimitSimulator(RateLimitStatus initialStatus) {
    this.limitPerPeriod = initialStatus.getLimit();
    this.taskPerformedTimes = new ArrayDeque<>(limitPerPeriod);
    this.initialResetTime = initialStatus.getResetTimeInSeconds() * 1000L - PERIOD_LENGTH_MS;

    // Simulate the queue of performed times by filling it with the current time based on the
    // number of items performed since the last reset.
    Long initialTime = System.currentTimeMillis();
    int initialUsed = initialStatus.getLimit() - initialStatus.getRemaining();
    for (int i = 0; i < initialUsed; i++) {
      taskPerformedTimes.add(initialTime);
    }
  }

  public void sleepIfNeededThenTaskPerformed() throws InterruptedException {
    if (!canPerformTask()) {
      Thread.sleep(timeUntilNextReset());
    }
    taskPerformed();
  }

  private void taskPerformed() {
    if (canPerformTask()) {
      taskPerformedTimes.add(System.currentTimeMillis());
    } else {
      throw new IllegalStateException("Cannot have performed task as rate limit would be hit");
    }
  }

  public boolean canPerformTask() {
    removeOldPerformedTimes();
    return taskPerformedTimes.size() < limitPerPeriod;
  }

  private void removeOldPerformedTimes() {
    long lastResetTime = lastResetTime();
    while(!taskPerformedTimes.isEmpty() && taskPerformedTimes.peek() < lastResetTime) {
      taskPerformedTimes.remove();
    }
  }

  private long lastResetTime() {
    long currentTime = System.currentTimeMillis();
    return currentTime - timeSinceLastReset(currentTime);
  }

  private long timeUntilNextReset() {
    return PERIOD_LENGTH_MS - timeSinceLastReset(System.currentTimeMillis());
  }

  private long timeSinceLastReset(long currentTime) {
    return (currentTime - initialResetTime) % PERIOD_LENGTH_MS;
  }
}
