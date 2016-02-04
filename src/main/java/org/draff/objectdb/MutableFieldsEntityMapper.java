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
import static org.draff.objectdb.EntityMapperHelper.fieldOrNull;
import static org.draff.objectdb.EntityMapperHelper.kindForClass;
import static org.draff.objectdb.EntityMapperHelper.invoke;
import static org.draff.objectdb.ValueHelper.fromValue;
import static org.draff.objectdb.ValueHelper.isDatastoreType;
import static org.draff.objectdb.ValueHelper.toValue;

/**
 * Created by dave on 1/26/16.
 */
class MutableFieldsEntityMapper implements EntityMapper {
  private final String kind;
  private final List<Field> propertyFields;
  private final Field idField;
  private final Field parentField;
  private final Method idMethod;
  private final Class modelClass;
  private final Constructor constructor;
  private final EntityMapper parentMapper;

  public MutableFieldsEntityMapper(Class clazz, EntityMapper parentMapper) {
    kind = kindForClass(clazz);
    propertyFields = classPropertyFields(clazz);
    modelClass = clazz;

    idField = fieldOrNull(modelClass, "id");
    if (idField == null) {
      try {
        idMethod = modelClass.getDeclaredMethod("id");
        idMethod.setAccessible(true);
      } catch(NoSuchMethodException e) {
        throw new ObjectDbException(e);
      }
    } else {
      idMethod = null;
    }

    try {
      constructor = clazz.getDeclaredConstructor(new Class[0]);
      constructor.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new ObjectDbException(e);
    }

    parentField = fieldOrNull(modelClass, "parent");
    this.parentMapper = parentMapper;
  }

  @Override
  public Entity toEntity(Model model) {
    Entity.Builder builder = Entity.newBuilder();
    setEntityKey(builder, model);

    propertyFields.forEach(field -> setPropertyFromObject(builder, field, model));
    return builder.build();
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    return clazz.cast(newFromEntity(entity, clazz));
  }

  @Override
  public Object getModelId(Model model) {
    if (idField != null) {
      return getField(model, idField);
    }
    return invoke(idMethod, model);
  }

  @Override
  public String entityKind(Class clazz) {
    return kind;
  }

  private <T extends Model> T newFromEntity(Entity entity, Class<T> clazz) {
    Object object = newInstance();
    setObjectIdFromEntity(object, entity);
    propertyFields.forEach(field -> setFieldFromEntity(object, field, entity));
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

  private Object newInstance() {
    try {
      return constructor.newInstance();
    } catch(InstantiationException|IllegalAccessException|InvocationTargetException e) {
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
    if (parentField == null) {
      builder.setKey(makeKey(kind, getModelId(model)));
    } else {
      try {
        Model parent = (Model) parentField.get(model);
        builder.setKey(makeKey(
            parentMapper.entityKind(parentField.getType()), parentMapper.getModelId(parent),
            kind, getModelId(model)));
      } catch(ClassCastException|IllegalAccessException e) {
        throw new ObjectDbException(e);
      }
    }
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
