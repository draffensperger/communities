package org.draff.twitfetch;

import org.draff.model.*;
import org.draff.objectdb.ObjectDb;

import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/13/16.
 */
public class FollowersGoalUpdater {
  private ObjectDb db;
  private UsersResources twitterUsers;
  private static final int BATCH_SIZE = 100;

  public FollowersGoalUpdater(ObjectDb db, UsersResources twitterUsers) {
    this.db = db;
    this.twitterUsers = twitterUsers;
  }

  public void retrieveFollowersGoalDetails() throws TwitterException {
    List<FollowersGoal> trackers = db.find(FollowersGoal.class, BATCH_SIZE);
    if(!trackers.isEmpty()) {
      retrieveTrackerUserIds(trackers);
    }
  }

  private void retrieveTrackerUserIds(List<FollowersGoal> trackers) throws TwitterException {
    List<User> users = twitterUsers.lookupUsers(screenNames(trackers));
    saveUsers(users, trackers);
  }

  private String[] screenNames(List<FollowersGoal> trackers) {
    String[] screenNames = new String[trackers.size()];
    for (int i = 0; i < trackers.size(); i++) {
      screenNames[i] = trackers.get(i).id;
    }
    return screenNames;
  }

  private void saveUsers(List<User> users, List<FollowersGoal> trackers) {
    saveUserDetails(users);
    updateFollowersTrackers(users, trackers);
    db.deleteAll(trackers);
  }

  private void updateFollowersTrackers(List<User> users, List<FollowersGoal> trackers) {
    Map<String, Long> screenNamesToIds = new HashMap<>();
    users.forEach(u -> screenNamesToIds.put(u.getScreenName().toLowerCase(), u.getId()));
    trackers.forEach(tracker ->
        updateFollowersTracker(screenNamesToIds.get(tracker.id.toLowerCase()), tracker.depthGoal)
    );
  }

  private void updateFollowersTracker(long userId, long depthGoal) {
    db.createOrUpdateById(FollowersTracker.class, userId, tracker -> {
      if (depthGoal >= 1) {
        tracker.shouldRetrieveFollowers = true;
      }
      if (depthGoal >= 2) {
        tracker.shouldRetrieveLevel2Followers = true;
      }
      return tracker;
    });
  }

  private void saveUserDetails(List<User> users) {
    db.saveAll(users.stream().map(u -> new UserDetail(u)).collect(Collectors.toList()));

    // Mark these users as retrieved in the UserDetailRequestById table so they won't be re-retrieved
    // later on.
    List<Long> ids = users.stream().map(u -> u.getId()).collect(Collectors.toList());
    db.createOrUpdateByIds(UserDetailRequestById.class, ids,
        request -> { request.detailRetrieved = true; return request; });
  }
}
