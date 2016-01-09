package org.draff.objectdb;

import java.util.Map;
import java.util.List;

/**
 * Created by dave on 1/7/16.
 */
public interface ObjectDb {
  void save(Object object);
  void save(List<Object> objects);
  void saveFields(Object object, String... fields);
  void saveFields(List<Object> objects, String... fields);
  <T> T findOne(Class<T> clazz);
  <T> T findOne(Class<T> clazz, Map<String, Object> fieldConstraints);
  <T> List<T> findByIds(Class<T> clazz, List<Object> ids);
  <T> T findById(Class<T> clazz, long id);
  <T> T findById(Class<T> clazz, String id);
  void delete(Object object);
}
