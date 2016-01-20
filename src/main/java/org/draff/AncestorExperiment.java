package org.draff;

import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.client.Datastore;

import org.draff.objectdb.DatastoreDb;
import org.draff.objectdb.DatastoreUtil;
import org.draff.objectdb.ObjectDb;

import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;

/**
 * Created by dave on 1/19/16.
 */
public class AncestorExperiment {
  private Datastore datastore;
  private ObjectDb db;
  private DatastoreUtil util;
  public AncestorExperiment(Datastore datastore) {
    this.datastore = datastore;
    this.db = new DatastoreDb(datastore);
    this.util = new DatastoreUtil(datastore);
  }

  public void saveAncestorFollowers() {
    Key.Builder key = makeKey("FollowerRoot", 2L, "FollowerChild", 5L);
    Entity entity = Entity.newBuilder().setKey(key).build();
    util.saveUpsert(entity);

    Key.Builder key2 = makeKey("FollowerRoot", 2L);
    Entity entity2 = Entity.newBuilder().setKey(key2).build();
    util.saveUpsert(entity2);
  }
}
