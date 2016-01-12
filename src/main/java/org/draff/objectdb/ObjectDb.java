package org.draff.objectdb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 1/7/16.
 */

public interface ObjectDb {
  void save(Object object);
  void saveAll(List<?> objects);
  <T> List<T> find(Class<T> clazz, int limit);
  <T> List<T> find(Class<T> clazz, Map<String, Object> fieldConstraints, int limit);

  <T> List<T> findOrderedById(Class<T> clazz, int limit, long minId);

  <T> T findOne(Class<T> clazz);
  <T> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints);
  <T> List<T> findByIds(Class<T> clazz, Collection<Long> ids);
  <T> T findById(Class<T> clazz, long id);
  <T> T findById(Class<T> clazz, String id);
  void delete(Object object);
  void deleteAll(List<?> object);
  void deleteAllByIds(Class clazz, Collection<Long> ids);
  <T> void createOrUpdate(Class<T> clazz, long id, ObjectUpdater<T> updater);
}
