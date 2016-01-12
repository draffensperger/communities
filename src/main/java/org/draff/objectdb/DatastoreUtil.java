package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Filter;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.LookupRequest;
import com.google.api.services.datastore.DatastoreV1.LookupResponse;
import com.google.api.services.datastore.DatastoreV1.Mutation;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.DatastoreV1.PropertyOrder;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.RunQueryResponse;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.makeFilter;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static com.google.api.services.datastore.client.DatastoreHelper.makeOrder;
import static com.google.api.services.datastore.client.DatastoreHelper.makeValue;

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

  public void saveDeletes(List<Key.Builder> keys) {
    Mutation.Builder mutation = Mutation.newBuilder();
    keys.forEach(key -> mutation.addDelete(key));
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

  public List<Entity> find(String kind, Filter filter, int limit) {
    Query.Builder q = Query.newBuilder();
    q.addKindBuilder().setName(kind);
    if (filter != null) {
      q.setFilter(filter);
    }
    q.setLimit(limit);
    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(q.build()).build();
    return results(request);
  }

  private List<Entity> results(RunQueryRequest request) {
    try {
      RunQueryResponse response = datastore.runQuery(request);
      return response.getBatch().getEntityResultList().stream()
          .map(r -> r.getEntity()).collect(Collectors.toList());
    } catch (DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<Entity> findByIds(Iterable<Key> keys) {
    LookupRequest request = LookupRequest.newBuilder().addAllKey(keys).build();
    try {
      LookupResponse response = datastore.lookup(request);
      return response.getFoundList().stream()
          .map(result -> result.getEntity()).collect(Collectors.toList());
    } catch(DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Entity findById(Key key) {
    LookupRequest request = LookupRequest.newBuilder().addKey(key).build();
    try {
      LookupResponse response = datastore.lookup(request);
      if (response.getFoundCount() == 0) {
        return null;
      }
      return response.getFound(0).getEntity();
    } catch(DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<Entity> findOrderedById(String kind, int limit, long minId) {
    Query.Builder query = Query.newBuilder();
    query.addKindBuilder().setName(kind);
    Filter minKeyFilter = makeFilter("__key__", PropertyFilter.Operator.GREATER_THAN_OR_EQUAL,
        makeValue(makeKey(kind, minId))).build();
    query.addOrder(makeOrder("__key__", PropertyOrder.Direction.ASCENDING));
    query.setFilter(minKeyFilter);
    query.setLimit(limit);

    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(query).build();

    try {
      return datastore.runQuery(request).getBatch().getEntityResultList().stream()
          .map(result -> result.getEntity()).collect(Collectors.toList());
    } catch(DatastoreException e) {
      e.printStackTrace();
      return null;
    }
  }
}
