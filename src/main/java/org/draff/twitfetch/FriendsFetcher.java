package org.draff.twitfetch;
import java.util.logging.*;

/**
 * Created by dave on 1/13/16.
 */
public class FriendsFetcher implements Runnable {
  private static Logger log = Logger.getLogger(FriendsFetcher.class.getName());

  private FriendsBatchFetcher batchFetcher;
  private RateLimit friendsRateLimit;

  public FriendsFetcher(FriendsBatchFetcher batchFetcher, RateLimit friendsRateLimit) {
    this.batchFetcher = batchFetcher;
    this.friendsRateLimit = friendsRateLimit;
  }

  public void run() {
    try {
      while(true) {
        while(friendsRateLimit.hasRemaining()) {
          fetchFriendsIfHasRemaining();
        }

        long msToSleep = friendsRateLimit.timeUntilNextReset();
        log.info("Sleeping " + Math.round((double)msToSleep / 60000.0) + " minutes for friends rate limit.");
        Thread.sleep(msToSleep);
      }
    } catch(InterruptedException e) {}
  }

  private void fetchFriendsIfHasRemaining() {
    try {
      friendsRateLimit.decrement();
      batchFetcher.fetchFriendsBatch();
    } catch(Exception e) {
      log.log(Level.SEVERE, "Error fetching friends: " + e.toString(), e);
    }
  }
}
