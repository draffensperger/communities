package org.draff;

/**
 * Created by dave on 1/13/16.
 */
public class FollowersFetcher implements Runnable {
  private FollowersBatchFetcher batchFetcher;
  private FollowersGoalUpdater goalUpdater;
  private RateLimit followersRateLimit;
  private RateLimit friendsRateLimit;

  public FollowersFetcher(FollowersBatchFetcher batchFetcher, FollowersGoalUpdater goalUpdater,
                          RateLimit followersRateLimit, RateLimit friendsRateLimit) {
    this.batchFetcher = batchFetcher;
    this.goalUpdater = goalUpdater;
    this.friendsRateLimit = friendsRateLimit;
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

        while(followersRateLimit.hasRemaining() || friendsRateLimit.hasRemaining()) {
          fetchFollowersIfHasRemaining();
          fetchFriendsIfHasRemaining();
        }

        long msToSleep = Math.min(followersRateLimit.timeUntilNextReset(),
            friendsRateLimit.timeUntilNextReset());
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

  private void fetchFriendsIfHasRemaining() {
    if (friendsRateLimit.hasRemaining()) {
      try {
        friendsRateLimit.decrement();
        batchFetcher.fetchFriendsBatch();
      } catch(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
}
