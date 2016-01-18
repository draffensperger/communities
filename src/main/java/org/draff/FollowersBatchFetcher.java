package org.draff;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;

import org.draff.models.Follower;
import org.draff.models.FollowersTracker;
import org.draff.models.UserDetailRequest;
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

  private final static Map<String, Object> NEEDS_FRIENDS =
      new ImmutableMap.Builder<String, Object>().put("shouldRetrieveFriends", true)
          .put("friendsRetrieved", false).build();

  public FollowersBatchFetcher(ObjectDb db, FriendsFollowersResources friendsFollowers) {
    this.db = db;
    this.friendsFollowers = friendsFollowers;
  }

  public void fetchFollowersBatch() throws TwitterException {
    fetchBatch(false, NEEDS_FOLLOWERS);
  }

  public void fetchFriendsBatch() throws TwitterException {
    fetchBatch(true, NEEDS_FRIENDS);
  }

  private void fetchBatch(boolean isFriends, Map<String, Object> trackerConstraints)
      throws TwitterException {
    FollowersTracker tracker = db.findOne(FollowersTracker.class, trackerConstraints);
    if (tracker == null) {
      return;
    }

    long[] friendOrFollowerIds;
    if (isFriends) {
      friendOrFollowerIds = fetchFriends(tracker);
    } else {
      friendOrFollowerIds = fetchFollowers(tracker);
    }

    addLevel2TrackersIfNeeded(tracker, friendOrFollowerIds);
    db.save(tracker);
  }

  private long[] fetchFollowers(FollowersTracker tracker) throws TwitterException {
    System.out.println("Fetching followers batch for userid: " + tracker.id);
    IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id, tracker.followersCursor);
    System.out.println("  saving " + followerIds.getIDs().length + " followers");
    saveFollowers(tracker.id, followerIds.getIDs());
    updateFollowersCursor(tracker, followerIds);
    return followerIds.getIDs();
  }

  private long[] fetchFriends(FollowersTracker tracker) throws TwitterException {
    System.out.println("Fetching friends batch for userid: " + tracker.id);
    IDs friendIds = friendsFollowers.getFriendsIDs(tracker.id, tracker.friendsCursor);
    System.out.println("  saving " + friendIds.getIDs().length + " friends");
    saveFriends(tracker.id, friendIds.getIDs());
    updateFriendsCursor(tracker, friendIds);
    return friendIds.getIDs();
  }

  private void saveFollowers(long userId, long[] followerIds) {
    List<Follower> followers = new ArrayList<>(followerIds.length);
    for (long followerId : followerIds) {
      followers.add(new Follower(userId, followerId));
    }
    db.saveAll(followers);
  }

  private void saveFriends(long userId, long[] friendIds) {
    List<Follower> followers = new ArrayList<>(friendIds.length);
    for (long friendId : friendIds) {
      followers.add(new Follower(friendId, userId));
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

  private void updateFriendsCursor(FollowersTracker tracker, IDs friendsIds) {
    if (friendsIds.hasNext()) {
      tracker.friendsCursor = friendsIds.getNextCursor();
    } else {
      tracker.friendsRetrieved = true;
      tracker.friendsCursor = -1L;
    }
  }

  private void addLevel2TrackersIfNeeded(FollowersTracker tracker, long[] friendOrFollowerIds) {
    if (tracker.shouldRetrieveLevel2Followers || tracker.shouldRetrieveLevel2Friends) {
      db.createOrUpdate(FollowersTracker.class, Longs.asList(friendOrFollowerIds), level2Tracker -> {
        level2Tracker.shouldRetrieveFriends = tracker.shouldRetrieveLevel2Friends;
        level2Tracker.shouldRetrieveFollowers = tracker.shouldRetrieveLevel2Followers;
      });

      addUserDetailRequests(friendOrFollowerIds);
    }
  }

  private void addUserDetailRequests(long[] userIds) {
    // Leave existing user detail requests as is, but create new ones with the detault value of
    // false for the detailRetrieved field.
    db.createOrUpdate(UserDetailRequest.class, Longs.asList(userIds), null);
  }
}
