package org.draff.twitfetch;

import com.google.common.collect.ImmutableMap;

import org.draff.model.FriendsTracker;
import org.draff.model.UserDetail;
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
        handleOrRethrow(exception);
        return new long[0];
      }
    }

    private void handleOrRethrow(TwitterException exception)
        throws TwitterException {
      if (exception.getStatusCode() == 401) {
        UserDetail userDetail = db.findById(UserDetail.class, tracker.id());
        if (userDetail == null) {
          // In this case it's most likely that the user is protected, but perhaps the UserDetail
          // record for it has not been retrieved yet. In this case, rather than making the explicit
          // claim that the Friends have been retrieved, just set that we won't retrieve them.
          tracker = tracker.withShouldFetchFriends(false);
        } else if (userDetail.isProtected()) {
          // The 401 error is a result of the protected status of the user and is totally normal
          // Just mark that we have retrieved the Friends for that users and move on.
          tracker = tracker.withFriendsFetched(true);
        } else {
          // The user does exist but was not marked as protected. This may be a genuinely exceptional
          // circumstance (or the user marked themselves as protected since the retrieval began).
          throw exception;
        }
      } else {
        throw exception;
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
