package org.draff.models;

/**
 * Created by dave on 1/9/16.
 */
public class FollowersTracker {
  public long id;
  public boolean shouldRetrieveFollowers;
  public boolean shouldRetrieveLevel2Followers;

  public boolean followersRetrieved;
  public boolean level2FollowersRetrieved;

  // These should default to -1 for a new instance as that is the starting Twitter retrieval cursor.
  public long followersCursor = -1L;
}
