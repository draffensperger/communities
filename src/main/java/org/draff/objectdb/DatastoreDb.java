package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.makeFilter;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static org.draff.objectdb.ValueHelper.*;

/**
 * Created by dave on 1/1/16.
 */
public class DatastoreDb implements ObjectDb {
  private final DatastoreUtil util;
  private final EntityMapper mapper;

  public DatastoreDb(Datastore datastore, Map<Class, EntityMapper> customEntityMappers) {
    this.util = new DatastoreUtil(datastore);
    this.mapper = new ManagingEntityMapper(customEntityMappers);
  }

  public DatastoreDb(Datastore datastore) {
    this.util = new DatastoreUtil(datastore);
    this.mapper = new ManagingEntityMapper();
  }

  // Package-private accessor to entity mapper for use by helper classes
  EntityMapper mapper() {
    return mapper;
  }

  @Override
  public void save(Model object) {
    util.saveUpsert(mapper.toEntity(object));
  }

  @Override
  public void saveAll(List<? extends Model> models) {
    if (models.isEmpty()) {
      return;
    }
    long start = System.nanoTime();
    List<Entity> entities = toEntities(models);
    System.out.println("  converting " + models.size() + " objects to entities took " +
        (System.nanoTime() - start)/1000000 + " ms");

    start = System.nanoTime();
    util.saveUpserts(entities);
    System.out.println("  saving " + models.size() + " upserts took " +
        (System.nanoTime() - start)/1000000 + " ms");
  }

  @Override
  public <T extends Model> List<T> findChildren(Model parent, Class<T> clazz, int limit, long minId) {
    List<Entity> entities = util.findChildren(entityKind(parent.getClass()),
        mapper.getModelId(parent), entityKind(clazz), limit, minId);
    List<T> models = fromEntities(clazz, entities);
    //setParents(models, parent);
    return models;
  }


  private void setParents(List<? extends Model> children, Model parent) {
    if (children.isEmpty()) {
      return;
    }
    Class childClass = children.get(0).getClass();
    try {
      Field parentField = childClass.getDeclaredField("parent");
      parentField.setAccessible(true);
      for (Model child : children) {
        parentField.set(child, parent);
      }
    } catch(NoSuchFieldException|IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }

  @Override
  public <T extends Model> List<T> find(Class<T> clazz, int limit) {
    return findByFilter(clazz, null, limit);
  }

  @Override
  public <T extends Model> List<T> find(Class<T> clazz, Map<String, Object> fieldConstraints, int limit) {
    return findByConstraints(clazz, fieldConstraints, limit);
  }

  @Override
  public <T extends Model> T findOne(Class<T> clazz) {
    return firstOrNull(findByFilter(clazz, null, 1));
  }

  @Override
  public <T extends Model> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints) {
    return firstOrNull(findByConstraints(clazz, fieldConstraints, 1));
  }

  private <T extends Model> T firstOrNull(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
  }

  private <T extends Model> List<T> findByConstraints(Class<T> clazz, Map<String, Object> fieldConstraints, int limit) {
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

  private <T extends Model> List<T> findByFilter(Class<T> clazz, Filter filter, int limit) {
    return fromEntities(clazz, util.find(entityKind(clazz), filter, limit));
  }

  @Override
  public <T extends Model> List<T> findByIds(Class<T> clazz, Collection<Long> ids) {
    return findByNamesOrIds(clazz, ids);
  }

  @Override
  public <T extends Model> List<T> findByNames(Class<T> clazz, Collection<String> names) {
    return findByNamesOrIds(clazz, names);
  }

  @Override
  public <T extends Model> List<T> findByNamesOrIds(Class<T> clazz, Collection<?> namesOrIds) {
    List<Key> keys = namesOrIds.stream()
        .map(nameOrId -> makeKey(entityKind(clazz), nameOrId).build()).collect(Collectors.toList());
    return fromEntities(clazz, util.findByIds(keys));
  }

  @Override
  public <T extends Model> List<T> findOrderedById(Class<T> clazz, int limit, long minId) {
    return fromEntities(clazz, util.findOrderedById(entityKind(clazz), limit, minId, null));
  }

  @Override
  public <T extends Model> List<T> findOrderedById(Class<T> clazz, int limit, long minId,
                                     Map<String, Object> constraints) {
    return fromEntities(clazz, util.findOrderedById(entityKind(clazz), limit, minId,
        constraintsFilter(constraints)));
  }

  private <T extends Model> List<T> fromEntities(Class<T> clazz, Collection<Entity> entities) {
    return entities.stream()
        .map(entity -> clazz.cast(mapper.fromEntity(entity, clazz))).collect(Collectors.toList());
  }

  private List<Entity> toEntities(List<? extends Model> models) {
    return models.stream().map(o -> mapper.toEntity(o)).collect(Collectors.toList());
  }

  @Override
  public <T extends Model> T findById(Class<T> clazz, long id) {
    return findByIdObject(clazz, id);
  }


  private <T extends Model> T findByIdObject(Class<T> clazz, Object id) {
    return mapper.fromEntity(util.findById(makeKey(entityKind(clazz), id).build()), clazz);
  }

  @Override
  public void delete(Model object) {
    util.saveDelete(objectKey(object));
  }

  @Override
  public void deleteAll(List<? extends Model> objects) {
    util.saveDeletes(objects.stream().map(o -> objectKey(o)).collect(Collectors.toList()));
  }

  public void deleteAllByIds(Class clazz, Collection<Long> ids) {
    String kind = entityKind(clazz);
    util.saveDeletes(ids.stream().map(id -> makeKey(kind, id)).collect(Collectors.toList()));
  }

  private Key.Builder objectKey(Model model) {
    return makeKey(entityKind(model.getClass()), mapper.getModelId(model));
  }
//
//  public <T extends Model> void createOrUpdateById(Class<T> clazz, long id,
//                                                   ObjectTransformer<T> updater,
//                                                   ObjectFromIdCreator<T> generator) {
//    T object = clazz.cast(findById(clazz, id));
//    object = newOrUpdatedObject(object, id, updater, generator);
//    save(object);
//  }
//
//  @Override
//  public <T extends Model> void createOrUpdateByIds(Class<T> clazz, List<Long> ids,
//                                                    ObjectTransformer<T> updater,
//                                                    ObjectFromIdCreator<T> generator) {
//    createOrUpdateByNamesOrIds(clazz, ids, updater, generator);
//  }
//
//  @Override
//  public <T extends Model> void createOrUpdateByNames(Class<T> clazz, List<String> names,
//                                                      ObjectTransformer<T> updater,
//                                                      ObjectFromIdCreator<T> generator) {
//    createOrUpdateByNamesOrIds(clazz, names, updater, generator);
//  }
//
//  public <T extends Model> void createOrUpdateByNamesOrIds(Class<T> clazz, List<?> namesOrIds,
//                                                           ObjectTransformer<T> updater,
//                                                           ObjectFromIdCreator<T> generator) {
//    List<T> foundObjects = findByNamesOrIds(clazz, namesOrIds);
//    Map<Object, T> idsToFound = new HashMap<>();
//    foundObjects.forEach(o -> idsToFound.put(mapper.getModelId(o), o));
//
//    List<T> objectsToSave = namesOrIds.stream().map(
//        id -> newOrUpdatedObject(idsToFound.get(id), id, updater, generator)
//    ).collect(Collectors.toList());
//    saveAll(objectsToSave);
//  }

  @Override
  public <T extends Model> void createOrUpdateById(Class<T> clazz, long id, ObjectTransformer<T> updater) {
    T object = clazz.cast(findById(clazz, id));
    object = newOrUpdatedObject(clazz, object, id, updater);
    save(object);
  }

  @Override
  public <T extends Model> void createOrUpdateByIds(Class<T> clazz, List<Long> ids, ObjectTransformer<T> updater) {
    createOrUpdateByNamesOrIds(clazz, ids, updater);
  }

  @Override
  public <T extends Model> void createOrUpdateByNames(Class<T> clazz, List<String> names,
                                                      ObjectTransformer<T> updater) {
    createOrUpdateByNamesOrIds(clazz, names, updater);
  }

  public <T extends Model> void createOrUpdateByNamesOrIds(Class<T> clazz, List<?> namesOrIds,
                                                           ObjectTransformer<T> updater) {
    List<T> foundObjects = findByNamesOrIds(clazz, namesOrIds);
    Map<Object, T> idsToFound = new HashMap<>();
    foundObjects.forEach(o -> idsToFound.put(mapper.getModelId(o), o));

    List<T> objectsToSave = namesOrIds.stream().map(
        id -> newOrUpdatedObject(clazz, idsToFound.get(id), id, updater)
    ).collect(Collectors.toList());
    saveAll(objectsToSave);
  }

  private <T extends Model> T newOrUpdatedObject(Class<T> clazz, T object, Object nameOrId,
                                                 ObjectTransformer<T> updater) {
    if (object == null) {
      try {
        object = clazz.newInstance();
      } catch (InstantiationException|IllegalAccessException e) {
        throw new ObjectDbException(e);
      }
      setObjectId(object, nameOrId);
    }

    if (updater != null) {
      return updater.transform(object);
    } else {
      return object;
    }
  }

  private void setObjectId(Object object, Object nameOrId) {
    try {
      Field idField = object.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(object, nameOrId);
    } catch(NoSuchFieldException|IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }

  private String entityKind(Class clazz) {
    return mapper.entityKind(clazz);
  }

  @Override
  public <T extends Model> CreateOrTransformOp.Builder createOrTransform(Class<T> clazz) {
    return CreateOrTransformOp.builder().db(this).modelClass(clazz);
  }
}
