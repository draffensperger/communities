package org.draff;

/**
 * Created by dave on 1/9/16.
 */
public class FollowersTracker {
  long id;
  boolean shouldRetrieveFollowers;
  boolean shouldRetrieveLevel2Followers;

  boolean followersRetrieved;
  boolean level2FollowersRetrieved;

  // These should default to -1 for a new instance as that is the starting Twitter retrieval cursor.
  long followersCursor = -1L;
}
