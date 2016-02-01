package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by dave on 1/26/16.
 */
class EntityMapperHelper {
  private static final String AUTO_VALUE_PREFIX = "AutoValue_";

  static Method methodOrNull(Class clazz, String publicOrDeclaredMethodName) {
    return lookupMethod(clazz, publicOrDeclaredMethodName, false);
  }

  static Method method(Class clazz, String publicOrDeclaredMethodName) {
    return lookupMethod(clazz, publicOrDeclaredMethodName, true);
  }

  static Field fieldOrNull(Class clazz, String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch(NoSuchFieldException e) {
      return null;
    }
  }

  private static Method lookupMethod(Class clazz, String name, boolean throwOnNotFound) {
    ArrayList<Method> methods = new ArrayList<>();
    methods.addAll(Arrays.asList(clazz.getMethods()));
    methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
    methods.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredMethods()));
    for (Method method : methods) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    if (throwOnNotFound) {
      throw new ObjectDbException(new NoSuchElementException("Method not found: " + name));
    } else {
      return null;
    }
  }

  static List<Method> methods(Class clazz) {
    List<Method> methods = new ArrayList<>();
    methods.addAll(Arrays.asList(clazz.getMethods()));
    List<Method> declaredMethods = Arrays.asList(clazz.getDeclaredMethods());
    declaredMethods.forEach(m -> m.setAccessible(true));
    methods.addAll(declaredMethods);
    return methods;
  }

  static Object invoke(Method method, Object thisObj, Object ... args) {
    try {
      return method.invoke(thisObj, args);
    } catch(InvocationTargetException|IllegalAccessException e) {
      throw new ObjectDbException(e);
    }
  }

  static String kindForClass(Class clazz) {
    String kind = clazz.getSimpleName();
    if (kind.startsWith(AUTO_VALUE_PREFIX)) {
      kind = kind.substring(AUTO_VALUE_PREFIX.length());
    }
    return kind;
  }

  static Object entityId(Entity entity) {
    Key key = entity.getKey();
    Key.PathElement pathElement = key.getPathElement(key.getPathElementCount() - 1);
    if (pathElement.hasId()) {
      return pathElement.getId();
    } else {
      return pathElement.getName();
    }
  }

  private static Method makePublic(Method method) {
    method.setAccessible(true);
    return method;
  }
}
