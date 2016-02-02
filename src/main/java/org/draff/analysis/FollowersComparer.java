package org.draff.analysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.draff.model.Follower;
import org.draff.model.FollowersTracker;
import org.draff.model.UserDetail;
import org.draff.objectdb.ObjectDb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dave on 1/20/16.
 */
public class FollowersComparer {
  private String screenName1;
  private String screenName2;
  private ObjectDb db;
  private static final int BATCH_SIZE = 8000;

  public FollowersComparer(ObjectDb db, String screenName1, String screenName2) {
    this.screenName1 = screenName1;
    this.screenName2 = screenName2;
    this.db = db;
  }

  public void printCompareResults() {
    Set<Long> followersIds1 = followerIds(screenName1);
    Set<Long> followersIds2 = followerIds(screenName2);
    Set<Long> followersInCommon = Sets.intersection(followersIds1, followersIds2);
    System.out.printf("%s and %s have %d followers in common", screenName1, screenName2,
        followersInCommon.size());
  }

  private Set<Long> followerIds(String screenName) {
    UserDetail detail = userbyScreenName(screenName);
    FollowersTracker tracker = db.findById(FollowersTracker.class, detail.id);
    HashSet<Long> followerIds = new HashSet<>();

    long nextMinId = Long.MIN_VALUE;
    List<Follower> followersBatch = db.findChildren(tracker, Follower.class, BATCH_SIZE, nextMinId);
    while(!followersBatch.isEmpty()) {
      for (Follower follower : followersBatch) {
        if (follower.id > nextMinId) {
          nextMinId = follower.id;
        }
        followerIds.add(follower.id);
      }
      followersBatch = db.findChildren(tracker, Follower.class, BATCH_SIZE, nextMinId + 1);
    }
    return followerIds;
  }

  private UserDetail userbyScreenName(String screenName) {
    return db.findOne(UserDetail.class,
        ImmutableMap.<String, Object>builder().put("screenName", screenName).build());
  }
}
