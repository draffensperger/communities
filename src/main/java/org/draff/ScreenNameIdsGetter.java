package org.draff;

import org.draff.models.FollowersTracker;
import org.draff.models.ScreenNameTracker;
import org.draff.models.UserDetail;
import org.draff.objectdb.ObjectDb;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/9/16.
 */
public class ScreenNameIdsGetter {
  private ObjectDb db;
  private UsersResources twitterUsers;
  private static final int BATCH_SIZE = 100;

  public ScreenNameIdsGetter(ObjectDb db, UsersResources users) {
    this.db = db;
    this.twitterUsers = users;
  }

  public void runBatch() {
    List<ScreenNameTracker> trackers = db.find(ScreenNameTracker.class, BATCH_SIZE);
    if(!trackers.isEmpty()) {
      getUserIds(trackers);
    }
  }

  private void getUserIds(List<ScreenNameTracker> trackers) {
    try {
      ResponseList<User> users = twitterUsers.lookupUsers(screenNames(trackers));
      saveUsers(users, trackers);
    } catch(TwitterException e) {
      e.printStackTrace();
    }
  }

  private String[] screenNames(List<ScreenNameTracker> trackers) {
    String[] screenNames = new String[trackers.size()];
    for (int i = 0; i < trackers.size(); i++) {
      screenNames[i] = trackers.get(i).id;
    }
    return screenNames;
  }

  private void saveUsers(List<User> users, List<ScreenNameTracker> trackers) {
    saveUserDetails(users);
    updateFollowersTrackers(users, trackers);
    db.deleteAll(trackers);
  }

  private void saveUserDetails(List<User> users) {
    List<UserDetail> details = users.stream()
        .map(user -> new UserDetail(user)).collect(Collectors.toList());
    db.saveAll(details);
  }

  private void updateFollowersTrackers(List<User> users, List<ScreenNameTracker> trackers) {
    Map<String, Long> screenNamesToIds = new HashMap<>();
    users.forEach(u -> screenNamesToIds.put(u.getScreenName(), u.getId()));
    trackers.forEach(tracker ->
      updateFollowersTracker(screenNamesToIds.get(tracker.id), tracker.depthGoal)
    );
  }

  private void updateFollowersTracker(long userId, long depthGoal) {
    db.createOrUpdate(FollowersTracker.class, userId, tracker -> {
      if (depthGoal >= 1) {
        tracker.shouldRetrieveFollowers = true;
        tracker.shouldRetrieveFriends = true;
      }
      if (depthGoal >= 2) {
        tracker.shouldRetrieveLevel2Followers = true;
        tracker.shouldRetrieveLevel2Friends = true;
      }
    });
  }
}
