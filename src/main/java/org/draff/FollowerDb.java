package org.draff;

import twitter4j.Twitter;
import java.util.List;

/**
 * Created by dave on 1/1/16.
 */
public interface FollowerDb {
  void saveUser(User user);
  void saveUsers(List<User> users);
  User nextUpForGetFollowersBatch();

  void saveFollowers(List<Follower> followers);

  void saveCursor(FollowersCursor cursor);
  FollowersCursor getCursor();
  void deleteCursor(FollowersCursor cursor);
}
