package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;

/**
 * This class provides a simple way to map between a plain-old-Java-object and a Datastore Entity
 * instance. It's not full-featured by any means, but it's a simple way to get the job done without
 * adding a dependency on another library like objectify.
 *
 * Created by dave on 1/2/16.
 */
class CachingEntityMapper implements EntityMapper {
  private static final ConcurrentHashMap<Class, EntityMapper> mappers = new ConcurrentHashMap<>();

  public CachingEntityMapper() {
  }

  public CachingEntityMapper(Map<Class, EntityMapper> customMappers) {
    mappers.putAll(customMappers);
  }

  @Override
  public Entity toEntity(Model model) {
    return mapperFor(model.getClass()).toEntity(model);
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
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
      EntityMapper defaultMapper = defaultForClass(clazz);
      mappers.put(clazz, defaultMapper);
      return defaultMapper;
    } else {
      return mapper;
    }
  }

  private EntityMapper defaultForClass(Class clazz) {
    return new ReflectionEntityMapper(clazz);
  }
}
