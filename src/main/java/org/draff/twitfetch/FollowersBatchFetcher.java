package org.draff.twitfetch;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;

import org.draff.model.*;
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
public class FollowersBatchFetcher {
  private static final Logger log = Logger.getLogger(FollowersBatchFetcher.class.getName());

  private ObjectDb db;
  private FriendsFollowersResources friendsFollowers;
  private FollowersStorer followersStorer;

  private final static Map<String, Object> NEEDS_FOLLOWERS =
      new ImmutableMap.Builder<String, Object>().put("shouldFetchFollowers", true)
          .put("followersFetched", false).build();

  @Inject
  public FollowersBatchFetcher(ObjectDb db, FriendsFollowersResources friendsFollowers,
                               FollowersStorer followersStorer) {
    this.db = db;
    this.friendsFollowers = friendsFollowers;
    this.followersStorer = followersStorer;
  }

  public void fetchFollowersBatch() throws TwitterException {
    fetchBatch(NEEDS_FOLLOWERS);
  }

  private void fetchBatch(Map<String, Object> trackerConstraints)
      throws TwitterException {
    FollowersTracker tracker = db.findOne(FollowersTracker.class, trackerConstraints);
    if (tracker != null) {
      new FollowersBatchFetch(tracker).fetch();
    } else {
      log.fine("No followers to fetch.");
    }
  }

  private class FollowersBatchFetch {
    private FollowersTracker tracker;
    private FollowersBatchFetch(FollowersTracker tracker) {
      this.tracker = tracker;
    }

    private void fetch() throws TwitterException {
      addLevel2TrackersIfNeeded(fetchFollowers());
      db.save(tracker);
    }

    private long[] fetchFollowers() throws TwitterException {
      log.info("Fetching followers batch for userid: " + tracker.id());
      try {
        IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id(), tracker.followersCursor());
        saveFollowers(followerIds.getIDs());
        updateFollowersCursor(followerIds);
        return followerIds.getIDs();
      } catch(TwitterException exception) {
        if (exception.getStatusCode() == 401) {
          // Typically a 401 error at this point indicates that the user has their tweets protected
          // so just mark that we should not fetch info for this user.
          tracker = tracker.withShouldFetchFollowers(false);
        }
        return new long[0];
      }
    }

    private void saveFollowers(long[] followerIds) {
      followersStorer.storeFollowers(tracker.id(), "followers", followerIds);
      log.fine("Saved " + followerIds.length + " followers");
    }

    private void updateFollowersCursor(IDs followerIds) {
      if (followerIds.hasNext()) {
        tracker = tracker.withFollowersCursor(followerIds.getNextCursor());
      } else {
        tracker = tracker.withFollowersFetched(true).withFollowersCursor(-1L);
      }
    }

    private void addLevel2TrackersIfNeeded(long[] friendOrFollowerIds) {
      if (tracker.shouldFetchLevel2Followers()) {
        db.createOrTransform(FollowersTracker.class)
            .namesOrIds(Longs.asList(friendOrFollowerIds))
            .creator(id -> FollowersTracker.builder().id((Long)id).shouldFetchFollowers(true).build())
            .transformer(level2Tracker -> level2Tracker.withShouldFetchFollowers(true))
            .now();

        db.createOrTransform(FriendsTracker.class)
            .namesOrIds(Longs.asList(friendOrFollowerIds))
            .creator(id -> FriendsTracker.builder().id((Long)id).shouldFetchFriends(true).build())
            .transformer(level2Tracker -> level2Tracker.withShouldFetchFriends(true))
            .now();

        addUserDetailRequests(friendOrFollowerIds);
      }
    }

    private void addUserDetailRequests(long[] userIds) {
      // Leave existing user detail requests as is, but create new ones with the value of
      // false for the detailRetrieved field.
      db.createOrTransform(UserDetailRequestById.class).namesOrIds(Longs.asList(userIds))
          .transformer(existing -> existing)
          .creator(id -> UserDetailRequestById.builder().id((Long)id).detailRetrieved(false).build())
          .now();
    }
  }
}
