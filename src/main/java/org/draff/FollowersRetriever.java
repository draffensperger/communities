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

  private final static Map<String, Object> NEEDS_FOLLOWERS =
      new ImmutableMap.Builder<String, Object>().put("shouldRetrieveFollowers", true)
          .put("followersRetrieved", false).build();

  public FollowersRetriever(ObjectDb db, FriendsFollowersResources friendsFollowers) {
    this.db = db;
    this.friendsFollowers = friendsFollowers;
  }

  public void retrieveBatch() throws TwitterException {
    FollowersTracker tracker = db.findOne(FollowersTracker.class, NEEDS_FOLLOWERS);
    if (tracker != null) {
      retrieveFollowersBatch(tracker);
    }
  }

  private void retrieveFollowersBatch(FollowersTracker tracker) throws TwitterException {
    if (tracker.followersCursor == 0) {
      tracker.followersCursor = -1;
    }
    IDs followerIds = friendsFollowers.getFollowersIDs(tracker.id, tracker.followersCursor);
    List<Follower> followers = new ArrayList<>();
    for (long followerId : followerIds.getIDs()) {
      Follower follower = new Follower();
      follower.userId = tracker.id;
      follower.followerId = followerId;
      follower.retrievedAt = System.currentTimeMillis();
      followers.add(follower);
    }
    db.saveAll(followers);
    if (followerIds.hasNext()) {
      tracker.followersCursor = followerIds.getNextCursor();
    } else {
      tracker.followersRetrieved = true;
    }
    addLevel2TrackersIfNeeded(tracker, followerIds.getIDs());
    db.save(tracker);
  }

  private void retrieveFriendsBatch(FollowersTracker tracker) throws TwitterException {
    if (tracker.friendsCursor == 0) {
      tracker.friendsCursor = -1;
    }
    IDs friends = friendsFollowers.getFriendsIDs(tracker.id, tracker.friendsCursor);
    List<Follower> followers = new ArrayList<>();
    long[] friendIds = friends.getIDs();
    for (long friendId : friendIds) {
      Follower follower = new Follower();
      follower.userId = friendId;
      follower.followerId = tracker.id;
      follower.retrievedAt = System.currentTimeMillis();
      followers.add(follower);
    }
    db.save(followers);
    if (friends.hasNext()) {
      tracker.friendsCursor = friends.getNextCursor();
    } else {
      tracker.friendsRetrieved = true;
    }
    addLevel2TrackersIfNeeded(tracker, friendIds);
    db.save(tracker);
  }

  private void addLevel2TrackersIfNeeded(FollowersTracker tracker, long[] friendOrFollowerIds) {
    if (tracker.shouldRetrieveLevel2Followers || tracker.shouldRetrieveLevel2Friends) {
      // implement a bulk create or update function
    }
  }
}
