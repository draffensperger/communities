package org.draff.analysis;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.draff.model.FollowersGoal;
import org.draff.objectdb.ObjectDb;
import org.draff.twitfetch.TwitFetchModule;

/**
 * Created by dave on 2/6/16.
 */
public class Main {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new TwitFetchModule());
    ObjectDb db = injector.getInstance(ObjectDb.class);

    String command = "";
    if (args.length > 0) {
      command = args[0];
    }
    if (command.equals("retrieve-followers")) {
      long id = Long.valueOf(args[1]);
      new FollowersRequester(db).requestFollowers(id, false);
    } else if (command.equals("retrieve-followers-2nd-level")) {
      long id = Long.valueOf(args[1]);
      new FollowersRequester(db).requestFollowers(id, true);
    } else if (command.equals("retrieve-friends")) {
      long id = Long.valueOf(args[1]);
      new FriendsRequester(db).requestFriends(id);
    } else if (command.equals("sum")) {
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
