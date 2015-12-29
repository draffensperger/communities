package org.draff;

import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;

/**
 * Created by dave on 12/28/15.
 */
public class FollowersRetriever {
  private FriendsFollowersResources friendsFollowers;
  public FollowersRetriever(FriendsFollowersResources friendsFollowers) {
    this.friendsFollowers = friendsFollowers;
  }

  public void retrieveFollowers(String screenName) throws TwitterException {
    // The Twitter4J docs say for the cursor to start at -1
    boolean hasNext = true;
    long cursor = -1;
    IDs ids;
    while(hasNext) {
      ids = friendsFollowers.getFollowersIDs(screenName, cursor);
      saveIds(screenName, ids.getIDs());
      hasNext = ids.hasNext();
      if (hasNext) {
        cursor = ids.getNextCursor();
      }
    }
  }

  private void saveIds(String screenName, long[] ids) {
    for(long id: ids) {
      System.out.println("Follower of " + screenName + ": " + id);
    }
  }
}
