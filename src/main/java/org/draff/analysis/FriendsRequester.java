package org.draff.analysis;

import org.draff.model.FriendsTracker;
import org.draff.objectdb.ObjectDb;

import java.util.logging.Logger;

/**
 * Created by dave on 7/4/16.
 */
public class FriendsRequester {
  private static final Logger log = Logger.getLogger(FriendsRequester.class.getName());

  private ObjectDb db;
  public FriendsRequester(ObjectDb db) {
    this.db = db;
  }

  public void requestFriends(long userId) {
    FriendsTracker tracker = db.findById(FriendsTracker.class, userId);
    FriendsTracker.Builder builder;
    if (tracker == null) {
      builder = FriendsTracker.builder();
    } else {
      builder = tracker.toBuilder();
    }

    tracker = builder
        .id(userId)
        .shouldFetchFriends(true)
        .build();

    log.info("Requesting friends for " + userId);
    db.save(tracker);
  }
}
