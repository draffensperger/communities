package org.draff.mapper;

import com.google.api.services.datastore.client.Datastore;
import com.google.common.collect.ImmutableMap;

import org.draff.model.Follower;
import org.draff.objectdb.DatastoreDb;
import org.draff.objectdb.EntityMapper;

import java.util.Map;

/**
 * Created by dave on 2/1/16.
 */
public class DatastoreDbFactory {
  private static final Map<Class, EntityMapper> CUSTOM_ENTITY_MAPPERS =
      new ImmutableMap.Builder<Class, EntityMapper>()
          .put(Follower.class, FollowerMapper.INSTANCE)
          .build();

  private DatastoreDbFactory() {}

  public static DatastoreDb create(Datastore datastore) {
    return new DatastoreDb(datastore, CUSTOM_ENTITY_MAPPERS);
  }
}
