package org.draff.models;

/**
 * Created by dave on 1/1/16.
 */
public class Follower {
  public long userId;
  public long followerId;
  public long retrievedAt;
  public String id() {
    // Having the key tied to userId and followerId ensures that there won't be duplicates.
    return userId + ":" + followerId;
  }
}
