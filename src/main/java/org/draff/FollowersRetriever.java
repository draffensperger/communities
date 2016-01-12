package org.draff;

import org.draff.models.Follower;
import org.draff.models.FollowersTracker;
import org.draff.objectdb.ObjectDb;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.FriendsFollowersResources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 1/7/16.
 */
public class FollowersRetriever {
  private ObjectDb db;
  private FriendsFollowersResources friendsFollowers;

  public FollowersRetriever(ObjectDb db, Twitter twitter) {
    this.db = db;
    this.friendsFollowers = twitter.friendsFollowers();
  }

  public void retrieveBatch() throws TwitterException {
    FollowersTracker tracker = getNextTracker();
    if (tracker.shouldRetrieveFollowers && !tracker.followersRetrieved) {
      retrieveFollowersBatch(tracker);
    } else if (tracker.shouldRetrieveFriends && !tracker.friendsRetrieved) {
      retrieveFriendsBatch(tracker);
    }
  }

  private FollowersTracker getNextTracker() {
    return null;
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
    db.save(followers);
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
