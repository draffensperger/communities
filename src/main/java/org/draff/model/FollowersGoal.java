package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/9/16.
 */
@AutoValue
public abstract class FollowersGoal implements Model {
  public abstract String id();
  public abstract long depthGoal();

  public static FollowersGoal create(String id, long depthGoal) {
    return new AutoValue_FollowersGoal(id, depthGoal);
  }
}
