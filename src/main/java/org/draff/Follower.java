package org.draff;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/1/16.
 */
public class Follower implements Model {
  FollowersTracker parent;
  long id;

  // ObjectDb expects a default contructor.
  Follower() {}

  Follower(FollowersTracker parent, long id) {
    this.parent = parent;
    this.id = id;
  }
}
