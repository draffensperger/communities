package org.draff.objectdb;

/**
 * Created by dave on 1/25/16.
 */
public class OrigEntityMapper {
/*  private static final String AUTO_VALUE_PREFIX = "AutoValue_";

  private static final ImmutableSet<Class> DATASTORE_TYPES =
      new ImmutableSet.Builder<Class>().add(String.class, Date.class, Boolean.TYPE, Boolean.class,
          Long.TYPE, Long.class, Double.TYPE, Double.class).build();

  public static Entity toEntity(Model object) {
    if (object == null)  {
      return null;
    }
    Entity.Builder builder = Entity.newBuilder();
    setEntityKey(builder, object);

    propertyFields(object).forEach(field -> setPropertyFromObject(builder, field, object));
    return builder.build();
  }

  public static <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    if (entity == null)  { return null; }
    try {
      return clazz.cast(newFromEntity(entity, clazz));
    } catch (NoSuchMethodException e) {
      throw new ObjectDbException(e);
    }
  }

  public static String entityKind(Class clazz) {
    String kind = clazz.getSimpleName();
    if (kind.startsWith(AUTO_VALUE_PREFIX)) {
      kind = kind.substring(AUTO_VALUE_PREFIX.length());
    }
    return kind;
  }

  private static Object newFromEntity(Entity entity, Class clazz) throws NoSuchMethodException {
    List<Field> fields = classPropertyFields(clazz);
    Class[] fieldTypes = fields.stream().map(f -> f.getType()).toArray(Class[]::new);
    try {
      Method createMethod = publicOrDeclaredMethod(clazz, "create", fieldTypes);
      Object[] values = new Object[fields.size()];
      try {
        createMethod.invoke(null, values);
      } catch(IllegalAccessException|InvocationTargetException e) {
        throw new ObjectDbException(e);
      }
    } catch(NoSuchMethodException noCreateMethod) {
      try {
        Method builderMethod = publicOrDeclaredMethod(clazz, "builder");

        try {
          Object builder =builderMethod.invoke(null);
        } catch(IllegalAccessException|InvocationTargetException e) {
          throw new ObjectDbException(e);
        }

      } catch(NoSuchMethodException noBuilderMethod) {
        return entityToMutableObject(entity, clazz);
      }
    }

    return null;
  }

  private static <T extends Model>  T entityToMutableObject(Entity entity, Class<T> clazz) {
    Object object = newInstance(clazz);
    setObjectIdFromEntity(object, entity);

    propertyFields(object).forEach(field -> setFieldFromEntity(object, field, entity));
    return clazz.cast(object);
  }

  private static void setObjectIdFromEntity(Object object, Entity entity) {
    setObjectId(object, getEntityId(entity));
  }

  public static void setObjectId(Object object, Object id) {
    Field idField;
    try {
      idField = object.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      setField(object, idField, id);
    } catch(NoSuchFieldException e) {
      // It's possible that the object has an id method for an auto-generated id (e.g. Follower).
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

  private static void setEntityKey(Entity.Builder builder, Object object) {
    try {
      Field parentField = object.getClass().getDeclaredField("parent");
      parentField.setAccessible(true);
      try {
        Model parent = (Model) parentField.get(object);
        builder.setKey(makeKey(entityKind(parent.getClass()), getObjectId(parent),
            entityKind(object.getClass()), getObjectId(object)));
      } catch(ClassCastException|IllegalAccessException e) {
        throw new ObjectDbException(e);
      }
    } catch(NoSuchFieldException e) {
      // For entities that don't have a parent set, just use their direct id as a key
      builder.setKey(makeKey(entityKind(object.getClass()), getObjectId(object)));
    }
  }

  public static Object getObjectId(Object object) {
    try {
      return getIdMethod(object).invoke(object);
    } catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
      throw new ObjectDbException(e);
    }
  }

  private static Method getIdMethod(Object object) throws NoSuchMethodException {
    return publicOrDeclaredMethod(object.getClass(), "id");
  }

  private static Method publicOrDeclaredMethod(Class clazz, String methodName, Class ... fieldtypes)
      throws NoSuchMethodException {
    try {
      return clazz.getMethod(methodName, fieldtypes);
    } catch(NoSuchMethodException noDeclaredMethod) {
      // If there is no public method with that name, try getting a declared one on the class itself.
      Method method = clazz.getDeclaredMethod(methodName, fieldtypes);
      method.setAccessible(true);
      return method;
    }
  }

  private static Object getEntityId(Entity entity) {
    Key key = entity.getKey();
    Key.PathElement pathElement = key.getPathElement(key.getPathElementCount() - 1);
    if (pathElement.hasId()) {
      return pathElement.getId();
    } else {
      return pathElement.getName();
    }
  }

  private static List<Field> propertyFields(Object object) {
    return classPropertyFields(object.getClass());
  }

  private static List<Field> classPropertyFields(Class clazz) {
    List<Field> fields = asList(clazz.getDeclaredFields()).stream().filter(
        f -> f.getName() != "id" && f.getName() != "parent"
            && DATASTORE_TYPES.contains(f.getType())
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

  private static Object entityValue(Entity entity, String fieldName) {
    return fromValue(getPropertyMap(entity).get(fieldName));
  }

  private static Object getField(Object object, Field field) {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }*/
}
