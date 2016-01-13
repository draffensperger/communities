package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Key;
import com.google.api.services.datastore.DatastoreV1.Value;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.getPropertyMap;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static com.google.api.services.datastore.client.DatastoreHelper.makeProperty;
import static com.google.api.services.datastore.client.DatastoreHelper.makeValue;
import static java.util.Arrays.asList;

/**
 * This class provides a simple way to map between a plain-old-Java-object and a Datastore Entity
 * instance. It's not full-featured by any means, but it's a simple way to get the job done without
 * adding a dependency on another library like objectify.
 *
 * Created by dave on 1/2/16.
 */
public class EntityMapper {
  private static final List<Class> DATASTORE_TYPES = asList(
      String.class, Date.class, Boolean.TYPE, Boolean.class, Long.TYPE, Long.class, Double.TYPE,
      Double.class
  );

  public static Entity toEntity(Object object) {
    if (object == null)  { return null; }
    Entity.Builder builder = Entity.newBuilder();
    setEntityKey(builder, object);

    propertyFields(object).forEach(field -> setPropertyFromObject(builder, field, object));
    return builder.build();
  }

  public static <T> T fromEntity(Entity entity, Class<T> clazz) {
    if (entity == null)  { return null; }
    Object object = newInstance(clazz);
    setObjectIdFromEntity(object, entity);

    propertyFields(object).forEach(field -> setFieldFromEntity(object, field, entity));
    return clazz.cast(object);
  }

  public static String entityKind(Class clazz) {
    return clazz.getSimpleName();
  }

  private static Object newInstance(Class clazz) {
    try {
      return clazz.newInstance();
    } catch (IllegalAccessException|InstantiationException e) {
      throw new ObjectDbException(e);
    }
  }

  private static void setEntityKey(Entity.Builder builder, Object object) {
    builder.setKey(makeKey(entityKind(object.getClass()), getObjectId(object)));
  }

  private static void setObjectIdFromEntity(Object object, Entity entity) {
    setObjectId(object, getEntityId(entity));
  }

  public static void setObjectId(Object object, Object id) {
    Field idField;
    try {
      idField = object.getClass().getDeclaredField("id");
      setField(object, idField, id);
    } catch(NoSuchFieldException e) {
      // It's possible that the object has an id method for an auto-generated id (e.g. Follower).
      // So just do nothing if we can't set the id field
    }
  }

  public static Object getObjectId(Object object) {
    try {
      Field idField = object.getClass().getDeclaredField("id");
      return idField.get(object);
    } catch(NoSuchFieldException|IllegalAccessException e) {}

    try {
      Method idMethod = object.getClass().getDeclaredMethod("id", null);
      return idMethod.invoke(object);
    } catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
      throw new ObjectDbException(e);
    }
  }

  private static Object getEntityId(Entity entity) {
    Key.PathElement pathElement = entity.getKey().getPathElement(0);
    if (pathElement.hasId()) {
      return pathElement.getId();
    } else {
      return pathElement.getName();
    }
  }

  private static List<Field> propertyFields(Object object) {
    return asList(object.getClass().getDeclaredFields()).stream().filter(
        f -> f.getName() != "id" && DATASTORE_TYPES.contains(f.getType())
    ).collect(Collectors.toList());
  }

  private static void setPropertyFromObject(Entity.Builder builder, Field field, Object object) {
    Object fieldVal = getField(object, field);
    if (fieldVal != null) {
      builder.addProperty(makeProperty(field.getName(), toValue(fieldVal)));
    }
  }

  private static void setFieldFromEntity(Object object, Field field, Entity entity) {
    setField(object, field, fromValue(getPropertyMap(entity).get(field.getName())));
  }

  private static void setField(Object object, Field field, Object value) {
    try {
      if (value != null) {
        field.set(object, value);
      }
    } catch (IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }

  private static Object getField(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }

  public static Value.Builder toValue(Object object) {
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
    if (value == null) {
      return null;
    } else if (value.hasIntegerValue()) {
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
