package org.draff;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.*;

/**
 * Created by dave on 1/13/16.
 */
public class FollowersFetcher implements Runnable {
  private FollowersBatchFetcher batchFetcher;
  private FollowersGoalUpdater goalUpdater;
  private Twitter twitter;

  // Both the get friends ids and the get followers ids are rate limited to an average of 1/minute
  private final long MIN_MS_BETWEEN_BATCHES = 60 * 1000;

  private long friendsBatchStartedAt = System.currentTimeMillis();
  private long followersBatchStartedAt = System.currentTimeMillis();

  public FollowersFetcher(Twitter twitter, FollowersBatchFetcher batchFetcher, FollowersGoalUpdater goalUpdater) {
    this.twitter = twitter;
    this.batchFetcher = batchFetcher;
    this.goalUpdater = goalUpdater;
  }

  public void run() {
    try {
      while(true) {
        Map<String, RateLimitStatus> rateLimitStatusMap = new HashMap<>();
        try {
          rateLimitStatusMap = twitter.getRateLimitStatus();
        } catch(TwitterException e) {
          e.printStackTrace();
        }

        try {
          goalUpdater.retrieveFollowersGoalDetails();
        } catch(Exception e) {
          e.printStackTrace();
        }

        sleepUtilReadyForFollowers();
        try {
          batchFetcher.fetchFollowersBatch();
        } catch(Exception e) {
          e.printStackTrace();
        }

        sleepUtilReadyForFriends();
        try {
          batchFetcher.fetchFriendsBatch();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    } catch(InterruptedException e) {}
  }

  private void sleepUtilReadyForFollowers() throws InterruptedException {
    long msSinceLastBatch = System.currentTimeMillis() - friendsBatchStartedAt;
    long sleepTime = Math.max(0L, MIN_MS_BETWEEN_BATCHES - msSinceLastBatch);
    System.out.printf("Sleeping for %d ms for followers%n", sleepTime);
    Thread.sleep(sleepTime);
    friendsBatchStartedAt = System.currentTimeMillis();
  }

  private void sleepUtilReadyForFriends() throws InterruptedException {
    long msSinceLastBatch = System.currentTimeMillis() - followersBatchStartedAt;
    long sleepTime = Math.max(0L, MIN_MS_BETWEEN_BATCHES - msSinceLastBatch);
    System.out.printf("Sleeping for %d ms for friends%n", sleepTime);
    Thread.sleep(sleepTime);
    followersBatchStartedAt = System.currentTimeMillis();
  }
}
