package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Filter;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.PropertyFilter;
import com.google.api.services.datastore.client.Datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.makeFilter;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static org.draff.objectdb.EntityMapper.entityKind;
import static org.draff.objectdb.EntityMapper.fromEntity;
import static org.draff.objectdb.EntityMapper.getObjectId;
import static org.draff.objectdb.EntityMapper.toEntity;
import static org.draff.objectdb.EntityMapper.toValue;

/**
 * Created by dave on 1/1/16.
 */
public class DatastoreDb implements ObjectDb {
  private DatastoreUtil util;
  private Datastore datastore;

  public DatastoreDb(Datastore datastore) {
    this.util = new DatastoreUtil(datastore);
    this.datastore = datastore;
  }

  @Override
  public void save(Object object) {
    if (object instanceof List) {
      throw new ObjectDbException("Tried to call save with a List, did you mean saveAll?");
    }
    util.saveUpsert(toEntity(object));
  }

  @Override
  public void saveAll(List<? extends Object> objects) {
    if (objects.isEmpty()) {
      return;
    }
    long start = System.nanoTime();
    List<Entity> entities = objects.stream()
        .map(o -> toEntity(o)).collect(Collectors.toList());
    System.out.println("  converting " + objects.size() + " objects to entities took " +
        (System.nanoTime() - start)/1000000 + " ms");

    start = System.nanoTime();
    util.saveUpserts(entities);
    System.out.println("  saving " + objects.size() + " upserts took " +
        (System.nanoTime() - start)/1000000 + " ms");
  }

  @Override
  public <T> List<T> find(Class<T> clazz, int limit) {
    return findByFilter(clazz, null, limit);
  }

  @Override
  public <T> List<T> find(Class<T> clazz, Map<String, Object> fieldConstraints, int limit) {
    return findByConstraints(clazz, fieldConstraints, limit);
  }

  @Override
  public <T> T findOne(Class<T> clazz) {
    return firstOrNull(findByFilter(clazz, null, 1));
  }

  @Override
  public <T> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints) {
    return firstOrNull(findByConstraints(clazz, fieldConstraints, 1));
  }

  private <T> T firstOrNull(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
  }

  private <T> List<T> findByConstraints(Class<T> clazz, Map<String, Object> fieldConstraints, int limit) {
    return findByFilter(clazz, constraintsFilter(fieldConstraints), limit);
  }

  private Filter constraintsFilter(Map<String, Object> fieldConstraints) {
    if (fieldConstraints == null) {
      return null;
    }
    List<Filter> filters = new ArrayList<>();
    fieldConstraints.forEach((field, value) ->
        filters.add(makeFilter(field, PropertyFilter.Operator.EQUAL, toValue(value)).build())
    );
    return makeFilter(filters).build();
  }

  private <T> List<T> findByFilter(Class<T> clazz, Filter filter, int limit) {
    return fromEntities(clazz, util.find(entityKind(clazz), filter, limit));
  }

  @Override
  public <T> List<T> findByIds(Class<T> clazz, Collection<Long> ids) {
    List<Key> keys = ids.stream().map(id -> makeKey(entityKind(clazz), id).build())
        .collect(Collectors.toList());
    return fromEntities(clazz, util.findByIds(keys));
  }

  @Override
  public <T> List<T> findOrderedById(Class<T> clazz, int limit, long minId) {
    return fromEntities(clazz, util.findOrderedById(entityKind(clazz), limit, minId, null));
  }

  @Override
  public <T> List<T> findOrderedById(Class<T> clazz, int limit, long minId,
                                     Map<String, Object> constraints) {
    return fromEntities(clazz, util.findOrderedById(entityKind(clazz), limit, minId,
        constraintsFilter(constraints)));
  }

  private <T> List<T> fromEntities(Class<T> clazz, Collection<Entity> entities) {
    return entities.stream()
        .map(entity -> clazz.cast(fromEntity(entity, clazz))).collect(Collectors.toList());
  }

  @Override
  public <T> T findById(Class<T> clazz, long id) {
    return findByIdObject(clazz, id);
  }


  private <T> T findByIdObject(Class<T> clazz, Object id) {
    return fromEntity(util.findById(makeKey(entityKind(clazz), id).build()), clazz);
  }

  public void delete(Object object) {
    util.saveDelete(objectKey(object));
  }

  public void deleteAll(List<?> objects) {
    util.saveDeletes(objects.stream().map(o -> objectKey(o)).collect(Collectors.toList()));
  }

  public void deleteAllByIds(Class clazz, Collection<Long> ids) {
    String kind = entityKind(clazz);
    util.saveDeletes(ids.stream().map(id -> makeKey(kind, id)).collect(Collectors.toList()));
  }

  private Key.Builder objectKey(Object object) {
    return makeKey(entityKind(object.getClass()), getObjectId(object));
  }

  public <T> void createOrUpdate(Class<T> clazz, long id, ObjectUpdater<T> updater) {
    T object = clazz.cast(findById(clazz, id));
    object = newOrUpdatedObject(clazz, object, id, updater);
    save(object);
  }

  public <T> void createOrUpdate(Class<T> clazz, List<Long> ids, ObjectUpdater<T> updater) {
    List<T> foundObjects = findByIds(clazz, ids);
    Map<Long, T> idsToFound = new HashMap<>();
    foundObjects.forEach(o -> idsToFound.put((Long)getObjectId(o), o));

    List<T> objectsToSave = ids.stream().map(
        id -> newOrUpdatedObject(clazz, idsToFound.get(id), id, updater)
    ).collect(Collectors.toList());
    saveAll(objectsToSave);
  }

  private <T> T newOrUpdatedObject(Class<T> clazz, T object, long id, ObjectUpdater<T> updater) {
    if (object == null) {
      try {
        object = clazz.newInstance();
      } catch (InstantiationException|IllegalAccessException e) {
        throw new ObjectDbException(e);
      }
      EntityMapper.setObjectId(object, id);
    }

    if (updater != null) {
      updater.update(object);
    }
    return object;
  }
}
