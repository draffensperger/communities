package org.draff;

import org.draff.model.UserDetail;
import org.draff.objectdb.ObjectDb;
import java.util.*;

/**
 * Created by dave on 1/18/16.
 */
public class FollowersCounter {
  private ObjectDb db;
  private static final int BATCH_SIZE = 100;

  public FollowersCounter(ObjectDb db) {
    this.db = db;
  }

  public Aggregates calcFollowerAggregates() {
    long lastUserId = Long.MIN_VALUE;
    List<UserDetail> users = db.findOrderedById(UserDetail.class, BATCH_SIZE, lastUserId);
    Aggregates aggregates = new Aggregates();
    while(!users.isEmpty()) {
      for (UserDetail user: users) {
        aggregates.totalFollowers += user.followersCount;
        aggregates.totalFriends += user.friendsCount;
        aggregates.totalUsers++;
        if (user.id > lastUserId) {
          lastUserId = user.id;
        }
      }
      users = db.findOrderedById(UserDetail.class, BATCH_SIZE, lastUserId + 1L);
    }
    return aggregates;
  }

  public class Aggregates {
    private Aggregates() {}
    private long totalFollowers;
    private long totalFriends;
    private long totalUsers;

    public long followersTotal() {
      return totalFollowers;
    }
    public double avgFollowers() {
      return totalFollowers / totalUsers;
    }
    public long friendsTotal() {
      return totalFriends;
    }
    public double avgFriends() {
      return totalFriends / totalUsers;
    }
    public long usersTotal() {
      return totalUsers;
    }
  }

}
