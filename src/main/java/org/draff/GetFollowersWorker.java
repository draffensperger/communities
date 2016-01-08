package org.draff;

import org.draff.objectdb.*;
import twitter4j.Twitter;
import twitter4j.api.FriendsFollowersResources;

/**
 * Created by dave on 1/7/16.
 */
public class GetFollowersWorker {
  private ObjectDb db;
  private Twitter twitter;
  private FriendsFollowersResources friendsFollowers;

  public GetFollowersWorker(ObjectDb db, Twitter twitter) {
    this.db = db;
    this.twitter = twitter;
    this.friendsFollowers = twitter.friendsFollowers();
  }

  public void run() {
    twitter4j.User u;
    u.
  }
}
