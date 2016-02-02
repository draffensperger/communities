package org.draff.mapper;

import com.google.api.services.datastore.DatastoreV1.*;

import org.draff.model.Follower;
import org.draff.model.FollowersTracker;
import org.draff.objectdb.EntityMapper;
import org.draff.objectdb.Model;

import static com.google.api.services.datastore.client.DatastoreHelper.*;

/**
 * Created by dave on 2/1/16.
 */
public enum FollowerMapper implements EntityMapper {
  INSTANCE;

  @Override
  public Entity toEntity(Model model) {
    Follower follower = (Follower) model;
    Entity.Builder builder = Entity.newBuilder();
    builder.setKey(makeKey("FollowersTracker", follower.parent().id, "Follower", follower.id()));
    return builder.build();
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    Key key = entity.getKey();
    FollowersTracker parent = new FollowersTracker();
    parent.id = key.getPathElement(0).getId();
    return clazz.cast(Follower.create(parent, key.getPathElement(1).getId()));
  }

  @Override
  public Object getModelId(Model model) {
    return ((Follower)model).id();
  }

  @Override
  public String entityKind(Class clazz) {
    return "Follower";
  }
}
