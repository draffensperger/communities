package org.draff;

import java.util.List;
import java.util.Arrays;
import com.google.api.services.datastore.client.*;

/**
 * Created by dave on 1/3/16.
 */
public class DatastoreCleaner {
  private DatastoreDb db;

  public DatastoreCleaner(Datastore datastore) {
    db = new DatastoreDb(datastore);
  }

  public void clean() {
    List<Class> modelClasses = Arrays.asList(Follower.class, FollowersCursor.class, User.class);
    modelClasses.forEach(clazz -> deleteObjects(clazz));
  }

  private void deleteObjects(Class clazz) {
    Object object = db.findOne(clazz);
    while(object != null) {
      db.delete(object);
      object = db.findOne(clazz);
    }
  }
}
