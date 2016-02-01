package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1;

/**
 * Created by dave on 1/26/16.
 */
public class MutableFieldsEntityMapper implements EntityMapper {
  public MutableFieldsEntityMapper(Class clazz) {
  }

  @Override
  public DatastoreV1.Entity toEntity(Model model) {
    return null;
  }

  @Override
  public <T extends Model> T fromEntity(DatastoreV1.Entity entity, Class<T> clazz) {
    return null;
  }

  @Override
  public Object getModelId(Model model) {
    return null;
  }

  @Override
  public String entityKind(Class clazz) {
    return null;
  }
}
