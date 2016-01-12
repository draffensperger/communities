package org.draff.models;

/**
 * Created by dave on 1/9/16.
 */
public class FollowersTracker {
  public long id;
  public boolean shouldRetrieveFollowers;
  public boolean shouldRetrieveFriends;
  public boolean shouldRetrieveLevel2Followers;
  public boolean shouldRetrieveLevel2Friends;

  public boolean followersRetrieved;
  public boolean friendsRetrieved;
  public boolean level2FollowersRetrieved;
  public boolean level2FriendsRetrieved;
}
