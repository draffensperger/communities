package org.draff;

import org.draff.models.Follower;
import org.draff.models.UserTracker;
import org.draff.objectdb.*;
import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

/**
 * Created by dave on 1/7/16.
 */
public class GetFollowersWorker implements Runnable {
  private ObjectDb db;
  private Twitter twitter;
  private FriendsFollowersResources friendsFollowers;
  private boolean shouldStopAfterNextGet;

  public GetFollowersWorker(ObjectDb db, Twitter twitter) {
    this.db = db;
    this.twitter = twitter;
    this.friendsFollowers = twitter.friendsFollowers();
  }

  public void stopAfterNextGet() {
    this.shouldStopAfterNextGet = true;
  }

  public void run() {
    UserTracker user = nextUserToGetFollowers();
    while(!shouldStopAfterNextGet && user != null) {
      getFollowersBatch(user);
      try {
        Thread.sleep(60000);
      } catch(InterruptedException e) {
        e.printStackTrace();
      }
      user = nextUserToGetFollowers();
    }
  }

  private void getFollowersBatch(UserTracker user) {
    try {
      IDs ids = friendsFollowers.getFollowersIDs(user.id, user.followersCursor);
      saveFollowers(user, ids.getIDs());

      if (ids.hasNext()) {
        user.followersCursor = ids.getNextCursor();
      } else {
        user.followerDepth += 1;
      }
      db.save(user);
    } catch (TwitterException e) {
      e.printStackTrace();
    }
  }

  private UserTracker nextUserToGetFollowers() {
    UserTracker user = userForDepthAndGoal(1, 0);
    if (user != null)  {
      return user;
    }

    user = userForDepthAndGoal(2, 0);
    if (user != null) {
      return user;
    }

    return userForDepthAndGoal(2, 1);
  }

  private UserTracker userForDepthAndGoal(long followerDepthGoal, long followerDepth) {
    Map<String, Object> constraints = new ImmutableMap.Builder<String, Object>()
        .put("followerDepthGoal", followerDepthGoal)
        .put("followerDepth", followerDepth)
        .build();
    return db.findOne(UserTracker.class, constraints);
  }

  private void saveFollowers(UserTracker user, long[] followerIds) {
    List<Follower> followers = new ArrayList<>();
    for (long followerId : followerIds) {
      Follower follower = new Follower();
      follower.userId = user.id;
      follower.followerId = followerId;
      followers.add(follower);
    }
    db.save(followers);

    if (user.followerDepthGoal == 2) {
      saveUsersFromFollowers(followers);
    }
  }

  private void saveUsersFromFollowers(List<Follower> followers) {
    List<Object> userIds = followers.stream().map(
        follower -> follower.userId).collect(Collectors.toList());
    List<UserTracker> existingUsers = db.findByIds(UserTracker.class, userIds);
    // think about this and come back to it - enqueing users based on followers...
  }
}
