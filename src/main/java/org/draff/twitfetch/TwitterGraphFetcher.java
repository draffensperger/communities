package org.draff.twitfetch;

import org.draff.objectdb.ObjectDb;

import twitter4j.Twitter;

import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Created by dave on 1/14/16.
 */
public class TwitterGraphFetcher {
  private static final Logger log = Logger.getLogger(TwitterGraphFetcher.class.getName());

  private ObjectDb objectDb;
  private Twitter twitter;
  private FollowersStorer followersStorer;

  @Inject
  public TwitterGraphFetcher(ObjectDb objectDb, Twitter twitter, FollowersStorer followersStorer) {
    this.objectDb = objectDb;
    this.twitter = twitter;
    this.followersStorer = followersStorer;
  }

  public void runFetch() {
    RateLimit followersRateLimit = new RateLimit(twitter, "/followers/ids");

    FollowersBatchFetcher followersBatchFetcher =
        new FollowersBatchFetcher(objectDb, twitter.friendsFollowers(), followersStorer);
    FollowersGoalUpdater followersGoalUpdater =
        new FollowersGoalUpdater(objectDb, twitter.users());
    FollowersFetcher followersFetcher =
        new FollowersFetcher(followersBatchFetcher, followersGoalUpdater,
            followersRateLimit);


    RateLimit friendsRateLimit = new RateLimit(twitter, "/friends/ids");
    FriendsBatchFetcher friendsBatchFetcher =
        new FriendsBatchFetcher(objectDb, twitter.friendsFollowers(), followersStorer);
    FriendsFetcher friendsFetcher =
        new FriendsFetcher(friendsBatchFetcher, friendsRateLimit);

    UserDetailBatchFetcher userDetailBatchFetcher =
        new UserDetailBatchFetcher(objectDb, twitter.users());
    UserDetailFetcher userDetailFetcher = new UserDetailFetcher(userDetailBatchFetcher);

    Thread followersFetcherThread = new Thread(followersFetcher);
    Thread userDetailFetcherThread = new Thread(userDetailFetcher);
    Thread friendsFetcherThread = new Thread(friendsFetcher);

    log.info("Starting Twitter graph fetch ...");

    followersFetcherThread.start();
    userDetailFetcherThread.start();
    friendsFetcherThread.start();

    addShutdownHook(followersFetcherThread, userDetailFetcherThread, friendsFetcherThread);

    try {
      followersFetcherThread.join();
      userDetailFetcherThread.join();
      friendsFetcherThread.join();
    } catch(InterruptedException e) {}
  }

  private void addShutdownHook(Thread followersFetcherThread, Thread userDetailFetcherThread,
                               Thread friendsFetcherThread) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        followersFetcherThread.interrupt();
        userDetailFetcherThread.interrupt();
        friendsFetcherThread.interrupt();
      }
    });
  }
}
