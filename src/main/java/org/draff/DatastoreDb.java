package org.draff;

import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.DatastoreV1.*;
import static com.google.api.services.datastore.client.DatastoreHelper.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.draff.EntityMapper.*;

/**
 * Created by dave on 1/1/16.
 */
public class DatastoreDb {
  private DatastoreUtil util;
  private Datastore datastore;

  public DatastoreDb(Datastore datastore) {
    this.util = new DatastoreUtil(datastore);
    this.datastore = datastore;
  }

  public void save(Object object) {
    util.saveUpsert(toEntity(object));
  }

  public void save(List<Object> objects) {
    List<Entity> entities = objects.stream().map(o -> toEntity(o)).collect(Collectors.toList());
    util.saveUpserts(entities);
  }

  public <T> T findOne(Class<T> clazz) {
    return clazz.cast(fromEntity(util.findOne(entityKind(clazz), null), clazz));
  }

  public <T> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints) {
    return null;
  }

  public <T> List<T> findByIds(Class<T> clazz, List<Object> ids) {
    List<Key> keys = ids.stream().map(id -> makeKey(entityKind(clazz), id).build())
        .collect(Collectors.toList());

    return util.findByIds(keys).stream()
        .map(entityResult -> fromEntity(entityResult.getEntity(), clazz))
        .collect(Collectors.toList());
  }

  public void delete(Object object) {
    util.saveDelete(makeKey(entityKind(object.getClass()), getObjectId(object)));
  }
}
