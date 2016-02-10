package org.draff.analysis;

import org.draff.model.FollowersGoal;
import org.draff.objectdb.ObjectDb;

/**
 * Created by dave on 2/6/16.
 */
public class Main {
  public static void main(String[] args) {
    ObjectDb db = null;

    String command = "";
    if (args.length > 0) {
      command = args[0];
    }
    if (command.equals("sum")) {
      FollowersCounter counter = new FollowersCounter(db);
      FollowersCounter.Aggregates aggregates = counter.calcFollowerAggregates();
      System.out.println("Total followers: " + aggregates.followersTotal());
      System.out.println("Total friends: " + aggregates.friendsTotal());
      System.out.println("Total users: " + aggregates.usersTotal());
      System.out.println("Avg followers: " + aggregates.avgFollowers());
      System.out.println("Avg friends: " + aggregates.avgFriends());
    } else if (command.equals("goal")) {
      FollowersGoal goal = FollowersGoal.create(args[1], Long.valueOf(args[2]));
      db.save(goal);
    } else if (command.equals("compare")) {
      FollowersComparer comparer = new FollowersComparer(db, args[1], args[2]);
      comparer.printCompareResults();
    } else if (command.equals("load-communities")) {
      new EmbeddedCommunityLoader(db, args[1]).loadEmbeddedCommunities();
      new EmbeddedCommunityDetailRequester(db).requestCommunityUserDetails();
    } else if (command.equals("save-community-follower-counts")) {
      new EmbeddedCommunityFollowersCounts(db, args[1]).saveFollowersCounts();
    }
  }
}
