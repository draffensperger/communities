package org.draff;

import java.util.List;
import java.util.Map;

/**
 * Created by dave on 1/1/16.
 */
public interface ObjectDb {
  void save(Object object);
  void save(List<Object> objects);

  <T> T findOne(Class<T> clazz);
  <T> T findOne(Class<T> clazz, Map<String, Object> where);

  <T> List<T> find(Class<T> clazz, long[] ids);

  void delete(Object object);
}
