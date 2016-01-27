package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;

/**
 * Created by dave on 1/25/16.
 */
public interface EntityMapper {
  Entity toEntity(Model model);
  <T extends Model> T fromEntity(Entity entity, Class<T> clazz);
  Object getModelId(Model model);

  String entityKind(Class clazz);
}
