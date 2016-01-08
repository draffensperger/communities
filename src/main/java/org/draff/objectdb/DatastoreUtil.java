package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import java.util.List;

/**
 * Created by dave on 1/3/16.
 */
public class DatastoreUtil {
  private Datastore datastore;
  public DatastoreUtil(Datastore datastore) {
    this.datastore = datastore;
  }

  public void saveUpsert(Entity entity) {
    saveMutation(Mutation.newBuilder().addUpsert(entity));
  }

  public void saveUpserts(List<Entity> entities) {
    Mutation.Builder mutation = Mutation.newBuilder();
    for (Entity entity : entities) {
      mutation.addUpsert(entity);
    }
    saveMutation(mutation);
  }

  public void saveDelete(Key.Builder key) {
    Mutation.Builder mutation = Mutation.newBuilder();
    mutation.addDelete(key);
    saveMutation(mutation);
  }

  public void saveMutation(Mutation.Builder mutation) {
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

  public Entity findOne(String kind, Filter filter) {
    Query.Builder q = Query.newBuilder();
    q.addKindBuilder().setName(kind);
    if (filter != null) {
      q.setFilter(filter);
    }
    q.setLimit(1);
    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(q.build()).build();
    return firstResult(request);
  }

  public Entity firstResult(RunQueryRequest request) {
    try {
      RunQueryResponse response = datastore.runQuery(request);
      List<EntityResult> results = response.getBatch().getEntityResultList();
      return results.isEmpty() ? null : results.get(0).getEntity();
    } catch (DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<EntityResult> findByIds(Iterable<Key> keys) {
    LookupRequest request = LookupRequest.newBuilder().addAllKey(keys).build();
    try {
      LookupResponse response = datastore.lookup(request);
      return response.getFoundList();
    } catch(DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }

/*  private Datastore datastore;

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
*/
}
