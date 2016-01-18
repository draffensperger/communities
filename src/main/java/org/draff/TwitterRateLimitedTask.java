package org.draff;

import twitter4j.RateLimitStatus;

/**
 * Created by dave on 1/18/16.
 */
public class TwitterRateLimitedTask {

  private Runnable task;


  public TwitterRateLimitedTask(RateLimitStatus initialRateLimitStatus, Runnable task) {
    this.task = task;
  }

  public void runIfPossible() {

  }

  public void sleepUntilCanRun() throws InterruptedException {

  }

  private int getRemaining() {
    if (System.currentTimeMillis() < initialResetTimeMs) {
      return initialRemaining;
    } else {

    }
  }
}
