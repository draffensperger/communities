package org.draff;

import twitter4j.Twitter;

/**
 * Created by dave on 1/1/16.
 */
public interface FollowerDb {
  void saveUsers(Iterable<TwitterUser> users);
  TwitterUser nextUpForGetFollowersBatch();
  void saveFollowers(Iterable<TwitterFollower> followers);
}
