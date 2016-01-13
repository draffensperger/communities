package org.draff;

import com.google.common.primitives.Longs;

import org.draff.models.FollowersGoal;
import org.draff.models.FollowersTracker;
import org.draff.models.UserDetail;
import org.draff.models.UserDetailRequest;
import org.draff.objectdb.ObjectDb;

import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/9/16.
 */
public class UserDetailRetriever {
  private ObjectDb db;
  private UsersResources twitterUsers;
  private static final int BATCH_SIZE = 100;

  public UserDetailRetriever(ObjectDb db, UsersResources users) {
    this.db = db;
    this.twitterUsers = users;
  }

  public void retrieveFollowersGoalDetails() throws TwitterException {
    List<FollowersGoal> trackers = db.find(FollowersGoal.class, BATCH_SIZE);
    if(!trackers.isEmpty()) {
      retrieveTrackerUserIds(trackers);
    }
  }

  public void retrieveUserIdsBatchDetails() {
    try {
      long[] neededIds = neededUserIdsBatch();
      List<User> users = twitterUsers.lookupUsers(neededIds);
      saveUserDetails(users);
      db.deleteAllByIds(UserDetailRequest.class, Longs.asList(neededIds));
    } catch(TwitterException e) {
      e.printStackTrace();
    }
  }

  private long[] neededUserIdsBatch() {
    List<Long> userIds = new ArrayList<>();

    Collection<Long> requestIds = requestIdsBatch(Long.MIN_VALUE);
    while(!requestIds.isEmpty() && userIds.size() < BATCH_SIZE) {
      fillUpToLimit(userIds, requestIds, BATCH_SIZE);
      if (userIds.size() < BATCH_SIZE) {
        requestIds = requestIdsBatch(Collections.max(userIds) + 1);
      }
    }

    return Longs.toArray(userIds);
  }

  private Collection<Long> requestIdsBatch(long minId) {
    List<Long> requestIdsList = db.findOrderedById(UserDetailRequest.class, BATCH_SIZE, minId).stream()
        .map(request -> request.id).collect(Collectors.toList());
    HashSet<Long> requestIds = new HashSet<>(requestIdsList);

    List<Long> existingIds = db.findByIds(UserDetail.class, requestIds).stream()
        .map(detail -> detail.id).collect(Collectors.toList());

    // Since there are already UserDetail records for those request ids, just delete the requests
    db.deleteAllByIds(UserDetailRequest.class, existingIds);
    requestIds.removeAll(existingIds);

    return requestIds;
  }

  private void fillUpToLimit(List<Long> dest, Collection<Long> source, int limit) {
    for (Long item : source) {
      if (dest.size() < limit) {
        dest.add(item);
      } else {
        return;
      }
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

  private void saveUserDetails(List<User> users) {
    List<UserDetail> details = users.stream()
        .map(user -> new UserDetail(user)).collect(Collectors.toList());
    db.saveAll(details);
  }

  private void updateFollowersTrackers(List<User> users, List<FollowersGoal> trackers) {
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
