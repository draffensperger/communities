package org.draff;

import com.google.api.services.datastore.client.Datastore;
import static com.google.api.services.datastore.client.DatastoreHelper.*;

import java.util.List;
import java.util.Map;

import static org.draff.EntityMapper.*;

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
    util.saveUpsert(toEntity(object));
  }

  @Override
  public void save(List<Object> objects) {
  }

  @Override
  public <T> T findOne(Class<T> clazz) {
    return clazz.cast(fromEntity(util.findOne(entityKind(clazz), null), clazz));
  }

  @Override
  public <T> T findOne(Class<T> clazz, Map<String, Object> where) {
    return null;
  }

  @Override
  public <T> List<T> find(Class<T> clazz, long[] ids) {
    return null;
  }

  @Override
  public void delete(Object object) {
    util.saveDelete(makeKey(entityKind(object.getClass()), getObjectId(object)));
  }
}
