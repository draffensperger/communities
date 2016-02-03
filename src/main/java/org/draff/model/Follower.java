package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/1/16.
 */
@AutoValue
public abstract class Follower implements Model {
  public abstract FollowersTracker parent();
  public abstract long id();

  Follower() {}

  public static Follower create(FollowersTracker parent, long id) {
    return new AutoValue_Follower(parent, id);
  }
}
