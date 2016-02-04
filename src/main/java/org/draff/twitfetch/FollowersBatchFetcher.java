package org.draff.twitfetch;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;

import org.draff.model.*;
import org.draff.objectdb.ObjectDb;

import twitter4j.IDs;
import twitter4j.TwitterException;
import twitter4j.api.FriendsFollowersResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 1/7/16.
 */
public class FollowersBatchFetcher {
  private ObjectDb db;
  private FriendsFollowersResources friendsFollowers;

  private final static Map<String, Object> NEEDS_FOLLOWERS =
      new ImmutableMap.Builder<String, Object>().put("shouldFetchFollowers", true)
          .put("followersFetched", false).build();

  public FollowersBatchFetcher(ObjectDb db, FriendsFollowersResources friendsFollowers) {
    this.db = db;
    this.friendsFollowers = friendsFollowers;
  }

  public void fetchFollowersBatch() throws TwitterException {
    fetchBatch(NEEDS_FOLLOWERS);
  }

  private void fetchBatch(Map<String, Object> trackerConstraints)
      throws TwitterException {
    FollowersTracker tracker = db.findOne(FollowersTracker.class, trackerConstraints);
    if (tracker != null) {
      new FollowersBatchFetch(tracker).fetch();
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
      System.out.println("Fetching followers batch for userid: " + tracker.id());
      try {
        IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id(), tracker.followersCursor());
        saveFollowers(followerIds.getIDs());
        updateFollowersCursor(followerIds);
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
          // claim that the followers have been retrieved, just set that we won't retrieve them.
          tracker = tracker.withShouldFetchFollowers(false);
        } else if (userDetail.isProtected()) {
          // The 401 error is a result of the protected status of the user and is totally normal
          // Just mark that we have retrieved the followers for that users and move on.
          tracker = tracker.withFollowersFetched(true);
        } else {
          // The user does exist but was not marked as protected. This may be a genuinely exceptional
          // circumstance (or the user marked themselves as protected since the retrieval began).
          throw exception;
        }
      } else {
        throw exception;
      }
    }

    private void saveFollowers(long[] followerIds) {
      List<Follower> followers = new ArrayList<>(followerIds.length);
      for (long followerId : followerIds) {
        followers.add(Follower.create(tracker.id(), followerId));
      }
      db.saveAll(followers);
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
            .transformer(level2Tracker -> ((FollowersTracker)level2Tracker).withShouldFetchFollowers(true))
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
