package org.draff.model;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/1/16.
 */
public class Follower implements Model {
  public FollowersTracker parent;
  public long id;

  // ObjectDb expects a default contructor.
  public Follower() {}

  public Follower(FollowersTracker parent, long id) {
    this.parent = parent;
    this.id = id;
  }
}
