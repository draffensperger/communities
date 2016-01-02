package org.draff;

import com.google.api.services.datastore.DatastoreV1.*;

import static com.google.api.services.datastore.client.DatastoreHelper.*;
import java.lang.reflect.*;
import static java.util.Arrays.*;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides a simple way to map between a plain-old-Java-object and a Datastore Entity
 * instance. It's not full-featured by any means, but it's a simple way to get the job done without
 * adding a dependency on another library like objectify.
 *
 * Created by dave on 1/2/16.
 */
public class EntityMapper {
  private static String modelsPrefix;

  public static final List<Class> DATASTORE_TYPES = asList(
      String.class, Date.class, Boolean.TYPE, Boolean.class, Long.TYPE, Long.class, Double.TYPE,
      Double.class
  );

  public static void setModelClassesPackage(String packageName) {
    modelsPrefix = packageName + ".";
  }

  public static Entity toEntity(Object object) {
    Entity.Builder builder = Entity.newBuilder();
    propertyFields(object).forEach(f -> addProperty(builder, object, f));
    addKey(builder, object);
    return builder.build();
  }

  public static Object fromEntity(Entity entity) {
    Key.PathElement pathElement = entity.getKey().getPathElement(0);
    Object object = instanceFromEntity(entity);

    setObjectFields(object, entity);

    Field idField;
    try {
      idField = object.getClass().getDeclaredField("id");
    } catch(NoSuchFieldException e) {
      return object;
    }

    Class idFieldType = idField.getType();
    Object idValue = null;

    if (idFieldType == Long.TYPE || idFieldType == Long.class) {
      idValue = pathElement.getId();
    } else if (idFieldType == String.class) {
      idValue = pathElement.getName();
    }

    try {
      idField.set(object, idValue);
    } catch(IllegalAccessException e) {
      e.printStackTrace();
    }

    return object;
  }

  private static Object instanceFromEntity(Entity entity) {
    try {
      return entityModelClass(entity).newInstance();
    } catch (IllegalAccessException|InstantiationException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Class entityModelClass(Entity entity) {
    try {
      return Class.forName(modelsPrefix + entity.getKey().getPathElement(0).getKind());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static List<Field> propertyFields(Object object) {
    return asList(object.getClass().getDeclaredFields()).stream().filter(
        f -> f.getName() != "id" && DATASTORE_TYPES.contains(f.getType())
    ).collect(Collectors.toList());
  }

  private static void setObjectFields(Object object, Entity entity) {
    Map<String, Value> props = getPropertyMap(entity);
    propertyFields(object).forEach(f -> setFieldValue(object, f, props.get(f.getName())));
  }

  private static void setFieldValue(Object object, Field field, Value value) {
    try {
      field.set(object, fromValue(value));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static void addProperty(Entity.Builder builder, Object object, Field field) {
    try {
      builder.addProperty(makeProperty(field.getName(), toValue(field.get(object))));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static void addKey(Entity.Builder builder, Object object) {
    builder.setKey(makeKey(
        object.getClass().getSimpleName(), fieldOrMethodValue(object, "id")
    ));
  }

  private static Object fieldOrMethodValue(Object object, String fieldOrMethodName) {
    try {
      Field idField = object.getClass().getDeclaredField(fieldOrMethodName);
      return idField.get(object);
    } catch(NoSuchFieldException|IllegalAccessException e) {}

    try {
      Method idMethod = object.getClass().getDeclaredMethod("id", null);
      return idMethod.invoke(object);
    } catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Value.Builder toValue(Object object) {
    if (object instanceof Long) {
      return makeValue((Long)object);
    } else if (object instanceof String) {
      return makeValue((String)object);
    } else if (object instanceof Double) {
      return makeValue((Double)object);
    } else if (object instanceof Boolean) {
      return makeValue((Boolean)object);
    } else if (object instanceof Date) {
      return makeValue((Date)object);
    } else {
      throw new IllegalArgumentException(
          "Can't make Datastore value for " + object.getClass() + ": " + object);
    }
  }

  private static Object fromValue(Value value) {
    if (value.hasIntegerValue()) {
      return value.getIntegerValue();
    } else if (value.hasStringValue()) {
      return value.getStringValue();
    } else if (value.hasDoubleValue()) {
      return value.getDoubleValue();
    } else if (value.hasBooleanValue()) {
      return value.getBooleanValue();
    } else if (value.hasTimestampMicrosecondsValue()) {
      return value.getTimestampMicrosecondsValue();
    } else {
      throw new IllegalArgumentException(
          "Not configured to convert Datastore value " + value);
    }
  }
}
