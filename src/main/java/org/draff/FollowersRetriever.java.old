package org.draff;

import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;


/**
 * Created by dave on 12/28/15.
 */
public class FollowersRetriever {
  private FriendsFollowersResources friendsFollowers;
  private FollowerDb db;

  public FollowersRetriever(FriendsFollowersResources friendsFollowers, FollowerDb db) {
    this.friendsFollowers = friendsFollowers;
    this.db = db;
  }

  public void retrieveFollowers(long userId) throws TwitterException {
    try {
      saveUser(userId, 2, false, -1);
      doFollowersRetrieval();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void doFollowersRetrieval() throws TwitterException {

/*    boolean hasNext = true;
    long cursor = -1;
    IDs ids;
    while(hasNext) {
      ids = friendsFollowers.getFollowersIDs(userId, cursor);
      saveFollowers(userId, ids.getIDs());
      hasNext = ids.hasNext();
      if (hasNext) {
        cursor = ids.getNextCursor();
      }
    }*/
  }

}
