package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.draff.objectdb.EntityMapperHelper.fieldOrNull;
import static org.draff.objectdb.EntityMapperHelper.isAutoValueImpl;
import static org.draff.objectdb.EntityMapperHelper.methodOrNull;

/**
 * This class provides a simple way to map between a plain-old-Java-object and a Datastore Entity
 * instance. It's not full-featured by any means, but it's a simple way to get the job done without
 * adding a dependency on another library like objectify.
 *
 * Created by dave on 1/2/16.
 */
class ManagingEntityMapper implements EntityMapper {
  private static final ConcurrentHashMap<Class, EntityMapper> mappers = new ConcurrentHashMap<>();

  public ManagingEntityMapper() {}

  public ManagingEntityMapper(Map<Class, EntityMapper> customMappers) {
    mappers.putAll(customMappers);
  }

  @Override
  public Entity toEntity(Model model) {
    if (model == null) {
      return null;
    }
    return mapperFor(model.getClass()).toEntity(model);
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    if (entity == null) {
      return null;
    }
    return mapperFor(clazz).fromEntity(entity, clazz);
  }

  @Override
  public Object getModelId(Model model) {
    return mapperFor(model.getClass()).getModelId(model);
  }

  @Override
  public String entityKind(Class clazz) {
    return mapperFor(clazz).entityKind(clazz);
  }

  private EntityMapper mapperFor(Class clazz) {
    EntityMapper mapper = mappers.get(clazz);
    if (mapper == null) {
      EntityMapper defaultMapper = defaultMapperForClass(clazz);
      mappers.put(clazz, defaultMapper);
      return defaultMapper;
    } else {
      return mapper;
    }
  }

  public EntityMapper defaultMapperForClass(Class clazz) {
    if (isAutoValueImpl(clazz)) {
      // The constructors below are designed to work on the abstract @AutoValue class, not the
      // auto-generated implementation class. This will also make it work better with custom entity
      // mapper classes.
      return mapperFor(clazz.getSuperclass());
    }

    if (methodOrNull(clazz, "create") != null) {
      return new StaticFactoryEntityMapper(clazz, "create");
    } else if (methodOrNull(clazz, "builder") != null) {
      return new BuilderEntityMapper(clazz, "builder");
    } else {
      EntityMapper parentMapper;
      Field parentField = fieldOrNull(clazz, "parent");
      if (parentField == null) {
        parentMapper = null;
      } else {
        parentMapper = defaultMapperForClass(parentField.getType());
      }
      return new MutableFieldsEntityMapper(clazz, parentMapper);
    }
  }
}
