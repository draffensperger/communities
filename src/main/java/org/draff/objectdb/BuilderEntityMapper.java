package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Value;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.getPropertyMap;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static com.google.api.services.datastore.client.DatastoreHelper.makeProperty;
import static org.draff.objectdb.EntityMapperHelper.*;
import static org.draff.objectdb.ValueHelper.fromValue;
import static org.draff.objectdb.ValueHelper.toValue;

/**
 * Created by dave on 1/26/16.
 */
public class BuilderEntityMapper implements EntityMapper {
  private static final Logger log = Logger.getLogger(BuilderEntityMapper.class.getName());

  private final Class modelClass;
  private final Method buildMethod;
  private final Method builderMethod;
  private final Method builderIdMethod;
  private final List<Method> builderPropertyMethods;

  private final Method idMethod;
  private final List<Method> propertyMethods;
  private final String entityKind;

  public BuilderEntityMapper(Class clazz, String staticBuilderMethodName) {
    builderMethod = method(clazz, staticBuilderMethodName);
    idMethod = method(clazz, "id");

    Class builderClass = builderMethod.getReturnType();
    buildMethod = method(builderClass, "build");
    builderPropertyMethods = methods(builderClass).stream()
        .filter(m -> !m.getName().equals("id") &&
            m.getReturnType().equals(builderClass)).collect(Collectors.toList());
    builderIdMethod = method(builderClass, "id");

    // Assume that the property methods on the value type class itself have the same names as the
    // property methods on the builder.
    propertyMethods = builderPropertyMethods.stream().map(m -> method(clazz, m.getName()))
        .collect(Collectors.toList());

    entityKind = kindForClass(clazz);
    modelClass = clazz;
  }

  @Override
  public Entity toEntity(Model model) {
    Entity.Builder builder = Entity.newBuilder();
    builder.setKey(makeKey(entityKind(model.getClass()), getModelId(model)));
    for (Method method : propertyMethods) {
      Object value = invoke(method, model);
      if (value != null) {
        builder.addProperty(makeProperty(method.getName(), toValue(value)));
      }
    }
    return builder.build();
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    Map<String, Value> entityProperties = getPropertyMap(entity);
    try {
      Object builder = invoke(builderMethod, null);
      builderPropertyMethods.forEach(m -> {
        if (entityProperties.containsKey(m.getName())) {
          invoke(m, builder, fromValue(entityProperties.get(m.getName())));
        }
      });
      invoke(builderIdMethod, builder, entityId(entity));
      return clazz.cast(invoke(buildMethod, builder));
    } catch(RuntimeException e) {
      log.severe("Exception building entity (" + entity.getKey().toString() +
          ") with properties: " + entityProperties.toString());
      log.severe("This may be because the entity does not have the expected properties.");
      throw(e);
    }
  }

  @Override
  public Object getModelId(Model model) {
    return invoke(idMethod, model);
  }

  @Override
  public String entityKind(Class clazz) {
    return entityKind;
  }
}
