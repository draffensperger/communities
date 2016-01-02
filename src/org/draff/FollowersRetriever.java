package org.draff;

import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;

import com.google.api.services.datastore.DatastoreV1;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import java.util.List;
import java.util.Map;

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
    try {
      saveUser(userId, 2, false, -1);
      doFollowersRetrieval();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void doFollowersRetrieval() throws TwitterException, DatastoreException {
    Filter notStarted = makeFilter("followerRetrievalStarted", PropertyFilter.Operator.EQUAL, makeValue(false)).build();
    DatastoreV1.Query.Builder q = DatastoreV1.Query.newBuilder();
    q.addKindBuilder().setName("TwitterUser");
    q.setFilter(notStarted);
    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(q.build()).build();
    RunQueryResponse response = datastore.runQuery(request);

    List<EntityResult> results = response.getBatch().getEntityResultList();

    for (EntityResult result : results) {
      Entity entity = result.getEntity();
      Map<String, Value> props = getPropertyMap(entity);
      long userId = entity.getKey().getPathElement(0).getId();
      long depthGoal = getLong(props.get("followerDepthGoal"));
      long cursor = getLong(props.get("followersCursor"));
    }

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

  private void saveUser(long userId, int followerDepthGoal, boolean followerRetrievalStarted, long followersCursor) {
    long updatedAt = System.currentTimeMillis();
    Entity user = Entity.newBuilder()
        .setKey(makeKey("TwitterUser", userId))
        .addProperty(makeProperty("followerDepthGoal", makeValue(followerDepthGoal)))
        .addProperty(makeProperty("followerRetrievalStarted", makeValue(followerRetrievalStarted)))
        .addProperty(makeProperty("followersCursor", makeValue(followersCursor)))
        .addProperty(makeProperty("updatedAt", makeValue(updatedAt)))
        .build();
    saveUpsert(user);
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

  private void saveUpsert(Entity entity) {
    saveMutation(Mutation.newBuilder().addUpsert(entity));
  }

  private void saveUpserts(Entity[] entities) {
    Mutation.Builder mutation = Mutation.newBuilder();
    for (Entity entity : entities) {
      mutation.addUpsert(entity);
    }
    saveMutation(mutation);
  }

  private void saveMutation(Mutation.Builder mutation) {
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
