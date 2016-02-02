package org.draff.twitfetch;

import org.draff.objectdb.ObjectDb;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Map;

/**
 * Created by dave on 1/14/16.
 */
public class TwitterGraphFetcher {
  private ObjectDb objectDb;
  private Twitter twitter;

  public TwitterGraphFetcher(ObjectDb objectDb, Twitter twitter) {
    this.objectDb = objectDb;
    this.twitter = twitter;
  }

  public void runFetch() {
    Map<String, RateLimitStatus> rateLimitStatusMap;
    try {
      rateLimitStatusMap = twitter.getRateLimitStatus();
    } catch(TwitterException e) {
      e.printStackTrace();
      return;
    }

    RateLimit followersRateLimit = new RateLimit(rateLimitStatusMap.get("/followers/ids"));

    FollowersBatchFetcher followersBatchFetcher =
        new FollowersBatchFetcher(objectDb, twitter.friendsFollowers());
    FollowersGoalUpdater followersGoalUpdater =
        new FollowersGoalUpdater(objectDb, twitter.users());
    FollowersFetcher followersFetcher =
        new FollowersFetcher(followersBatchFetcher, followersGoalUpdater,
            followersRateLimit);

    UserDetailBatchFetcher userDetailBatchFetcher =
        new UserDetailBatchFetcher(objectDb, twitter.users());
    UserDetailFetcher userDetailFetcher = new UserDetailFetcher(userDetailBatchFetcher);


    Thread followersFetcherThread = new Thread(followersFetcher);
    Thread userDetailFetcherThread = new Thread(userDetailFetcher);

    followersFetcherThread.start();
    userDetailFetcherThread.start();

    addShutdownHook(followersFetcherThread, userDetailFetcherThread);

    try {
      followersFetcherThread.join();
      userDetailFetcherThread.join();
    } catch(InterruptedException e) {}
  }

  private void addShutdownHook(Thread followersFetcherThread, Thread userDetailFetcherThread) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        followersFetcherThread.interrupt();
        userDetailFetcherThread.interrupt();
      }
    });
  }
}
