package org.draff.analysis;

import org.draff.model.FollowersTracker;
import org.draff.objectdb.ObjectDb;

import java.util.logging.Logger;

/**
 * Created by dave on 7/4/16.
 */
public class FollowersRequester {
  private static final Logger log = Logger.getLogger(FollowersRequester.class.getName());

  private ObjectDb db;
  public FollowersRequester(ObjectDb db) {
    this.db = db;
  }

  public void requestFollowers(long userId, boolean requestSecondLevel) {
    FollowersTracker tracker = db.findById(FollowersTracker.class, userId);
    FollowersTracker.Builder builder;
    if (tracker == null) {
      builder = FollowersTracker.builder();
    } else {
      builder = tracker.toBuilder();
    }

    tracker = builder
        .id(userId)
        .shouldFetchFollowers(true)
        .shouldFetchLevel2Followers(requestSecondLevel)
        .build();

    log.info("Requesting followers for " + userId + ". Include 2nd level: " + requestSecondLevel);
    db.save(tracker);
  }
}
