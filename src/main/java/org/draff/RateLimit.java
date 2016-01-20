package org.draff;

import twitter4j.RateLimitStatus;

/**
 * Created by dave on 1/18/16.
 */
public class RateLimit {
  // Twitter rate limits reset every 15 minutes
  private static final long PERIOD_LENGTH_MS = 15 * 60 * 1000;
  private static final long RATE_LIMIT_MARGIN = 5 * 1000;

  private int limitPerPeriod;
  private int remaining;
  private long initialResetTime;
  private long lastPerformedTime;

  public RateLimit(RateLimitStatus initialStatus) {
    this.limitPerPeriod = initialStatus.getLimit();
    this.remaining = initialStatus.getRemaining();
    this.lastPerformedTime = System.currentTimeMillis();

    long msSinceLastReset = PERIOD_LENGTH_MS - initialStatus.getSecondsUntilReset() * 1000L;
    this.initialResetTime = System.currentTimeMillis() - msSinceLastReset + RATE_LIMIT_MARGIN;
  }

  public boolean hasRemaining() {
    checkForLimitReset();
    return remaining > 0;
  }

  public void decrement() {
    if (hasRemaining()) {
      lastPerformedTime = System.currentTimeMillis();
      remaining--;
    } else {
      throw new IllegalStateException("Cannot have performed task as rate limit would be hit");
    }
  }

  public long timeUntilNextReset() {
    return PERIOD_LENGTH_MS - timeSinceLastReset(System.currentTimeMillis());
  }

  private void checkForLimitReset() {
    if (lastPerformedTime < lastResetTime()) {
      remaining = limitPerPeriod;
    }
  }

  private long lastResetTime() {
    long currentTime = System.currentTimeMillis();
    return currentTime - timeSinceLastReset(currentTime);
  }

  private long timeSinceLastReset(long currentTime) {
    return (currentTime - initialResetTime) % PERIOD_LENGTH_MS;
  }
}
