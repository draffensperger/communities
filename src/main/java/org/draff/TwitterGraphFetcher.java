package org.draff;

import com.google.api.services.datastore.client.Datastore;

import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;

import org.draff.objectdb.DatastoreDb;
import org.draff.objectdb.ObjectDb;

import twitter4j.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Created by dave on 1/14/16.
 */
public class TwitterGraphFetcher {
  private FollowersFetcher followersFetcher;
  private UserDetailFetcher userDetailFetcher;
  private Twitter twitter;

  public TwitterGraphFetcher(ObjectDb objectDb, Twitter twitter) {
    FollowersBatchFetcher followersBatchFetcher =
        new FollowersBatchFetcher(objectDb, twitter.friendsFollowers());
    FollowersGoalUpdater followersGoalUpdater =
        new FollowersGoalUpdater(objectDb, twitter.users());
    this.followersFetcher = new FollowersFetcher(twitter, followersBatchFetcher, followersGoalUpdater);
    this.twitter = twitter;

    UserDetailBatchFetcher userDetailBatchFetcher =
        new UserDetailBatchFetcher(objectDb, twitter.users());
    this.userDetailFetcher = new UserDetailFetcher(userDetailBatchFetcher);
  }

  public static TwitterGraphFetcher configureFromEnv() throws GeneralSecurityException, IOException {
    return new TwitterGraphFetcher(new DatastoreDb(datastoreFromEnv()), twitterFromEnv());
  }

  public void runFetch() {
    Map<String, RateLimitStatus> rateLimitStatusMap = new HashMap<>();
    try {
      rateLimitStatusMap = twitter.getRateLimitStatus();
    } catch(TwitterException e) {
      e.printStackTrace();
    }

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

  private static Datastore datastoreFromEnv() throws GeneralSecurityException, IOException {
    return DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv().build());
  }

  private static Twitter twitterFromEnv() {
    return new TwitterFactory().getInstance();
  }
}
