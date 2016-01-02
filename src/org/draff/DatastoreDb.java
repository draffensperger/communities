package org.draff;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.*;

/**
 * Created by dave on 1/1/16.
 */
public class DatastoreDb implements FollowerDb {
  private Datastore datastore;
  public DatastoreDb(Datastore datastore) {
    this.datastore = datastore;
  }

  public void saveUsers(Iterable<TwitterUser> users) {
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

  public void saveFollowers(List<TwitterFollower> followers) {
    long retrievedAt = System.currentTimeMillis();

    saveUpserts(followers.stream().map(f -> entityFromFollower(f)).collect(Collectors.toList()));

    Entity[] followers = new Entity[followerIds.length];
    for (int i = 0; i < followers.length; i++) {
      long followerId = followerIds[i];
      followers[i] = buildTwitterFollower(userId, followerId, retrievedAt);
    }
    saveUpserts(followers);
  }

  public TwitterUser nextUpForGetFollowersBatch() {
    Filter notStarted = makeFilter("followerRetrievalStarted",
        PropertyFilter.Operator.EQUAL, makeValue(false)).build();
    Entity entity = findOne("TwitterUser", notStarted);
    return entity == null ? null: userFromEntity(entity);
  }

  private TwitterUser userFromEntity(Entity entity) {
    TwitterUser user = new TwitterUser();
    user.id = entity.getKey().getPathElement(0).getId();
    user.followersCursor = getLong(props.get("followersCursor"));
    user.followerRetrievalStartedAt = getLong(props.get("followerRetrievalStartedAt"));
    user.followerRetrievalFinishedAt = getLong(props.get("followerRetrievalFinishedAt"));
    user.followersCursor = getLong(props.get("followersCursor"));
    user.updatedAt = getLong(props.get("updatedAt"));
    return user;
  }

  private Entity entityFromFollower(TwitterFollower follower) {
    return Entity.newBuilder()
        .setKey(makeKey("TwitterFollower", follower.userId + ":" + follower.followerId))
        .addProperty(makeProperty("userId", makeValue(follower.userId)))
        .addProperty(makeProperty("followerId", makeValue(follower.followerId)))
        .addProperty(makeProperty("retrievedAt", makeValue(follower.retrievedAt)))
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

  private Entity findOne(String kind, Filter filter) {
    Query.Builder q = Query.newBuilder();
    q.addKindBuilder().setName(kind);
    q.setFilter(filter);
    q.setLimit(1);
    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(q.build()).build();
    return firstResult(request);
  }

  private Entity firstResult(RunQueryRequest request) {
    try {
      RunQueryResponse response = datastore.runQuery(request);
      List<EntityResult> results = response.getBatch().getEntityResultList();
      return results.isEmpty() ? null : results.get(0).getEntity();
    } catch (DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }
}
