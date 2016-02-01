package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.getPropertyMap;
import static com.google.api.services.datastore.client.DatastoreHelper.makeKey;
import static com.google.api.services.datastore.client.DatastoreHelper.makeProperty;
import static org.draff.objectdb.EntityMapperHelper.entityId;
import static org.draff.objectdb.EntityMapperHelper.kindForClass;
import static org.draff.objectdb.ValueHelper.fromValue;
import static org.draff.objectdb.ValueHelper.isDatastoreType;
import static org.draff.objectdb.ValueHelper.toValue;

/**
 * Created by dave on 1/26/16.
 */
public class MutableFieldsEntityMapper implements EntityMapper {
  private final String kind;

  public MutableFieldsEntityMapper(Class clazz) {
    kind = kindForClass(clazz);
  }

  @Override
  public Entity toEntity(Model model) {
    Entity.Builder builder = Entity.newBuilder();
    setEntityKey(builder, model);

    propertyFields(model).forEach(field -> setPropertyFromObject(builder, field, model));
    return builder.build();
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    return clazz.cast(newFromEntity(entity, clazz));
  }

  @Override
  public Object getModelId(Model model) {
    try {
      Field idField = model.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      return idField.get(model);
    } catch(NoSuchFieldException|IllegalAccessException e) {}
    try {
      Method idMethod = model.getClass().getDeclaredMethod("id", null);
      idMethod.setAccessible(true);
      return idMethod.invoke(model);
    } catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
      throw new ObjectDbException(e);
    }
  }

  @Override
  public String entityKind(Class clazz) {
    return kind;
  }

  private <T extends Model> T newFromEntity(Entity entity, Class<T> clazz) {
    Object object = newInstance(clazz);
    setObjectIdFromEntity(object, entity);

    propertyFields(object).forEach(field -> setFieldFromEntity(object, field, entity));
    return clazz.cast(object);
  }

  private void setObjectIdFromEntity(Object object, Entity entity) {
    setObjectId(object, entityId(entity));
  }

  public void setObjectId(Object object, Object id) {
    Field idField;
    try {
      idField = object.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      setField(object, idField, id);
    } catch(NoSuchFieldException e) {
      // It's possible that the object has an id method for an auto-generated derived id
      // So just do nothing if we can't set the id field
    }
  }

  private static Object newInstance(Class clazz) {
    try {
      Constructor constructor = clazz.getDeclaredConstructor(new Class[0]);
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (IllegalAccessException|InstantiationException|InvocationTargetException|
        NoSuchMethodException e) {
      throw new ObjectDbException(e);
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

  private void setEntityKey(Entity.Builder builder, Model model) {
    try {
      Field parentField = model.getClass().getDeclaredField("parent");
      parentField.setAccessible(true);
      try {
        Model parent = (Model) parentField.get(model);
        builder.setKey(makeKey(kind, getModelId(parent), kind, getModelId(model)));
      } catch(ClassCastException|IllegalAccessException e) {
        throw new ObjectDbException(e);
      }
    } catch(NoSuchFieldException e) {
      // For entities that don't have a parent set, just use their direct id as a key
      builder.setKey(makeKey(kind, getModelId(model)));
    }
  }

  private static List<Field> propertyFields(Object object) {
    return classPropertyFields(object.getClass());
  }

  private static List<Field> classPropertyFields(Class clazz) {
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields()).stream().filter(
        f -> f.getName() != "id" && f.getName() != "parent"
            && isDatastoreType(f.getType())
    ).collect(Collectors.toList());
    fields.forEach(f -> f.setAccessible(true));
    return fields;
  }

  private static void setPropertyFromObject(Entity.Builder builder, Field field, Object object) {
    Object fieldVal = getField(object, field);
    if (fieldVal != null) {
      builder.addProperty(makeProperty(field.getName(), toValue(fieldVal)));
    }
  }

  private static Object getField(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }
}
