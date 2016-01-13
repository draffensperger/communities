package org.draff;

import com.google.common.collect.ImmutableMap;

import org.draff.models.Follower;
import org.draff.models.FollowersTracker;
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
public class FollowersRetriever {
  private ObjectDb db;
  private FriendsFollowersResources friendsFollowers;

  private final static long TWITTER_CURSOR_START = -1L;

  private final static Map<String, Object> NEEDS_FOLLOWERS =
      new ImmutableMap.Builder<String, Object>().put("shouldRetrieveFollowers", true)
          .put("followersRetrieved", false).build();

  private final static Map<String, Object> NEEDS_FRIENDS =
      new ImmutableMap.Builder<String, Object>().put("shouldRetrieveFriends", true)
          .put("friendsRetrieved", false).build();

  public FollowersRetriever(ObjectDb db, FriendsFollowersResources friendsFollowers) {
    this.db = db;
    this.friendsFollowers = friendsFollowers;
  }

  public void retrieveFollowersBatch() throws TwitterException {
    retrieveBatch(false, NEEDS_FOLLOWERS);
  }

  public void retrieveFriendsBatch() throws TwitterException {
    retrieveBatch(true, NEEDS_FRIENDS);
  }

  private void retrieveBatch(boolean isFriends, Map<String, Object> trackerConstraints)
      throws TwitterException {
    FollowersTracker tracker = db.findOne(FollowersTracker.class, trackerConstraints);
    if (tracker == null) {
      return;
    }

    long[] friendOrFollowerIds;
    if (isFriends) {
      friendOrFollowerIds = retrieveFriends(tracker);
    } else {
      friendOrFollowerIds = retrieveFollowers(tracker);
    }

    addLevel2TrackersIfNeeded(tracker, friendOrFollowerIds);
    db.save(tracker);
  }

  private long[] retrieveFollowers(FollowersTracker tracker) throws TwitterException {
    IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id, tracker.followersCursor);
    saveFollowers(tracker.id, followerIds.getIDs());
    updateFollowersCursor(tracker, followerIds);
    return followerIds.getIDs();
  }

  private long[] retrieveFriends(FollowersTracker tracker) throws TwitterException {
    IDs friendIds = friendsFollowers.getFriendsIDs(tracker.id, tracker.friendsCursor);
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
    }
  }

  private void updateFriendsCursor(FollowersTracker tracker, IDs friendsIds) {
    if (friendsIds.hasNext()) {
      tracker.friendsCursor = friendsIds.getNextCursor();
    } else {
      tracker.friendsRetrieved = true;
    }
  }

  private void addLevel2TrackersIfNeeded(FollowersTracker tracker, long[] friendOrFollowerIds) {
    if (tracker.shouldRetrieveLevel2Followers || tracker.shouldRetrieveLevel2Friends) {
      // implement a bulk create or update function
    }
  }
}
