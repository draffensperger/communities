package org.draff.twitfetch;

import com.google.common.collect.ImmutableMap;

import org.draff.model.FriendsTracker;
import org.draff.objectdb.ObjectDb;

import twitter4j.IDs;
import twitter4j.TwitterException;
import twitter4j.api.FriendsFollowersResources;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Created by dave on 1/7/16.
 */
public class FriendsBatchFetcher {
  private static final Logger log = Logger.getLogger(FriendsBatchFetcher.class.getName());

  private ObjectDb db;
  private FriendsFollowersResources friendsFriends;
  private FollowersStorer storer;

  private final static Map<String, Object> NEEDS_FRIENDS =
      new ImmutableMap.Builder<String, Object>().put("shouldFetchFriends", true)
          .put("friendsFetched", false).build();

  @Inject
  public FriendsBatchFetcher(ObjectDb db, FriendsFollowersResources friendsFriends,
                             FollowersStorer storer) {
    this.db = db;
    this.friendsFriends = friendsFriends;
    this.storer = storer;
  }

  public void fetchFriendsBatch() throws TwitterException {
    fetchBatch(NEEDS_FRIENDS);
  }

  private void fetchBatch(Map<String, Object> trackerConstraints)
      throws TwitterException {
    FriendsTracker tracker = db.findOne(FriendsTracker.class, trackerConstraints);
    if (tracker != null) {
      new FriendsBatchFetch(tracker).fetch();
    } else {
      log.fine("No friends to fetch.");
    }
  }

  private class FriendsBatchFetch {
    private FriendsTracker tracker;
    private FriendsBatchFetch(FriendsTracker tracker) {
      this.tracker = tracker;
    }

    private void fetch() throws TwitterException {
      fetchFriends();
      db.save(tracker);
    }

    private long[] fetchFriends() throws TwitterException {
      log.info("Fetching friends batch for userid: " + tracker.id());
      try {
        IDs followerIds = friendsFriends.getFriendsIDs(tracker.id(), tracker.friendsCursor());
        saveFriends(followerIds.getIDs());
        updateFriendsCursor(followerIds);
        return followerIds.getIDs();
      } catch(TwitterException exception) {
        if (exception.getStatusCode() == 401) {
          // Typically a 401 error at this point indicates that the user has their tweets protected
          // so just mark that we should not fetch info for this user.
          tracker = tracker.withShouldFetchFriends(false);
          return new long[0];
        } else {
          throw exception;
        }
      }
    }

    private void saveFriends(long[] followerIds) {
      storer.storeFollowers(tracker.id(), "friends", followerIds);
      log.fine("Saved " + followerIds.length + " friends");
    }

    private void updateFriendsCursor(IDs followerIds) {
      if (followerIds.hasNext()) {
        tracker = tracker.withFriendsCursor(followerIds.getNextCursor());
      } else {
        tracker = tracker.withFriendsFetched(true).withFriendsCursor(-1L);
      }
    }
  }
}
