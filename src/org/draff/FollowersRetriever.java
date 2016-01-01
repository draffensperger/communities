package org.draff;

import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.protobuf.ByteString;

import static com.google.api.services.datastore.client.DatastoreHelper.*;

/**
 * Created by dave on 12/28/15.
 */
public class FollowersRetriever {
  private FriendsFollowersResources friendsFollowers;
  private Datastore datastore;
  public FollowersRetriever(FriendsFollowersResources friendsFollowers, Datastore datastore) {
    this.friendsFollowers = friendsFollowers;
    this.datastore = datastore;
  }

  public void retrieveFollowers(long userId) throws TwitterException {
    boolean hasNext = true;
    long cursor = -1;
    IDs ids;
    while(hasNext) {
      ids = friendsFollowers.getFollowersIDs(userId, cursor);
      saveFollowers(userId, ids.getIDs());
      hasNext = ids.hasNext();
      if (hasNext) {
        cursor = ids.getNextCursor();
      }
    }
  }

  private void saveUser(long userId) {

  }

  private void saveFollowers(long userId, long[] followerIds) {
    long retrievedAt = System.currentTimeMillis();

    Entity[] followers = new Entity[followerIds.length];
    for (int i = 0; i < followers.length; i++) {
      long followerId = followerIds[i];
      followers[i] = buildTwitterFollower(userId, followerId, retrievedAt);
    }
    saveUpserts(followers);
  }

  private Entity buildTwitterFollower(long userId, long followerId, long retrievedAt) {
    return Entity.newBuilder()
        .setKey(makeKey("TwitterFollower", userId + ":" + followerId))
        .addProperty(makeProperty("userId", makeValue(userId)))
        .addProperty(makeProperty("followerId", makeValue(followerId)))
        .addProperty(makeProperty("retrievedAt", makeValue(retrievedAt)))
        .build();
  }

  private void saveUpserts(Entity[] entities) {
    Mutation.Builder mutation = Mutation.newBuilder();
    for (Entity entity : entities) {
      mutation.addUpsert(entity);
    }

    CommitRequest request = CommitRequest.newBuilder()
        .setMode(CommitRequest.Mode.NON_TRANSACTIONAL)
        .setMutation(mutation)
        .build();

    try {
      datastore.commit(request);
    } catch (DatastoreException e) {
      e.printStackTrace();
    }
  }
}
