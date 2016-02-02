package org.draff.twitfetch;

/**
 * Created by dave on 1/13/16.
 */
public class FollowersFetcher implements Runnable {
  private FollowersBatchFetcher batchFetcher;
  private FollowersGoalUpdater goalUpdater;
  private RateLimit followersRateLimit;

  public FollowersFetcher(FollowersBatchFetcher batchFetcher, FollowersGoalUpdater goalUpdater,
                          RateLimit followersRateLimit) {
    this.batchFetcher = batchFetcher;
    this.goalUpdater = goalUpdater;
    this.followersRateLimit = followersRateLimit;
  }

  public void run() {
    try {
      while(true) {
        try {
          goalUpdater.retrieveFollowersGoalDetails();
        } catch(Exception e) {
          e.printStackTrace();
        }

        while(followersRateLimit.hasRemaining()) {
          fetchFollowersIfHasRemaining();
        }

        long msToSleep = followersRateLimit.timeUntilNextReset();
        System.out.println("Sleeping " + msToSleep + " for rate limit.");
        Thread.sleep(msToSleep);
      }
    } catch(InterruptedException e) {}
  }

  private void fetchFollowersIfHasRemaining() {
    if (followersRateLimit.hasRemaining()) {
      try {
        followersRateLimit.decrement();
        batchFetcher.fetchFollowersBatch();
      } catch(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
}
