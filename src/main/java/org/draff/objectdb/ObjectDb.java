package org.draff.objectdb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 1/7/16.
 */

public interface ObjectDb {
  void save(Model object);
  void saveAll(List<? extends Model> objects);
  <T extends Model> List<T> findChildren(Model parent, Class<T> childClazz);
  <T extends Model> void createOrUpdate(Class<T> clazz, long id, ObjectUpdater<T> updater);
  <T extends Model> void createOrUpdate(Class<T> clazz, List<Long> ids, ObjectUpdater<T> updater);
  <T extends Model> List<T> find(Class<T> clazz, int limit);
  <T extends Model> List<T> find(Class<T> clazz, Map<String, Object> fieldConstraints, int limit);
  <T extends Model> T findOne(Class<T> clazz);
  <T extends Model> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints);
  <T extends Model> List<T> findByIds(Class<T> clazz, Collection<Long> ids);
  <T extends Model> T findById(Class<T> clazz, long id);
  <T extends Model> List<T> findOrderedById(Class<T> clazz, int limit, long minId);
  <T extends Model> List<T> findOrderedById(Class<T> clazz, int limit, long minId, Map<String, Object> constraints);
  void delete(Model object);
  void deleteAll(List<? extends Model> object);
  void deleteAllByIds(Class clazz, Collection<Long> ids);
}
