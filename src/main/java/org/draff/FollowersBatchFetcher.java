package org.draff;

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
      new ImmutableMap.Builder<String, Object>().put("shouldRetrieveFollowers", true)
          .put("followersRetrieved", false).build();

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
    if (tracker == null) {
      return;
    }

    addLevel2TrackersIfNeeded(tracker, fetchFollowers(tracker));
    db.save(tracker);
  }

  private long[] fetchFollowers(FollowersTracker tracker) throws TwitterException {
    System.out.println("Fetching followers batch for userid: " + tracker.id);
    try {
      IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id, tracker.followersCursor);
      saveFollowers(tracker, followerIds.getIDs());
      updateFollowersCursor(tracker, followerIds);
      return followerIds.getIDs();
    } catch(TwitterException exception) {
      handleOrRethrow(tracker, exception);
      return new long[0];
    }
  }

  private void handleOrRethrow(FollowersTracker tracker, TwitterException exception)
      throws TwitterException {
    if (exception.getStatusCode() == 401) {
      UserDetail userDetail = db.findById(UserDetail.class, tracker.id);
      if (userDetail == null) {
        // In this case it's most likely that the user is protected, but perhaps the UserDetail
        // record for it has not been retrieved yet. In this case, rather than making the explicit
        // claim that the followers have been retrieved, just set that we won't retrieve them.
        tracker.shouldRetrieveFollowers = false;
      } else if (userDetail.isProtected) {
        // The 401 error is a result of the protected status of the user and is totally normal
        // Just mark that we have retrieved the followers for that users and move on.
        tracker.followersRetrieved = true;
      } else {
        throw exception;
      }
    } else {
      throw exception;
    }
  }

  private void saveFollowers(FollowersTracker tracker, long[] followerIds) {
    List<Follower> followers = new ArrayList<>(followerIds.length);
    for (long followerId : followerIds) {
      followers.add(new Follower(tracker, followerId));
    }
    db.saveAll(followers);
  }

  private void updateFollowersCursor(FollowersTracker tracker, IDs followerIds) {
    if (followerIds.hasNext()) {
      tracker.followersCursor = followerIds.getNextCursor();
    } else {
      tracker.followersRetrieved = true;
      tracker.followersCursor = -1L;
    }
  }

  private void addLevel2TrackersIfNeeded(FollowersTracker tracker, long[] friendOrFollowerIds) {
    if (tracker.shouldRetrieveLevel2Followers) {
      db.createOrUpdateByIds(FollowersTracker.class, Longs.asList(friendOrFollowerIds), level2Tracker -> {
        level2Tracker.shouldRetrieveFollowers = tracker.shouldRetrieveLevel2Followers;
        return level2Tracker;
      });

      addUserDetailRequests(friendOrFollowerIds);
    }
  }

  private void addUserDetailRequests(long[] userIds) {
    // Leave existing user detail requests as is, but create new ones with the detault value of
    // false for the detailRetrieved field.
    db.createOrUpdateByIds(UserDetailRequestById.class, Longs.asList(userIds), null);
  }
}
