package org.draff;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/1/16.
 */
public class Follower implements Model {
  long userId;
  long followerId;

  public Follower() {}
  public Follower(long userId, long followerId) {
    this.userId = userId;
    this.followerId = followerId;
  }

  String id() {
    // Having the key tied to userId and followerId ensures that there won't be duplicates.
    return userId + ":" + followerId;
  }
}
