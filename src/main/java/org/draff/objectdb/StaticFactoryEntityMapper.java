package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.*;
import static org.draff.objectdb.EntityMapperHelper.*;
import static org.draff.objectdb.ValueHelper.*;

/**
 * Created by dave on 1/26/16.
 */
public class StaticFactoryEntityMapper implements EntityMapper {
  private final Class modelClass;
  private final Method factoryMethod;
  private final Method idMethod;
  private final List<String> propertyNames;
  private final List<Method> propertyMethods;

  public StaticFactoryEntityMapper(Class clazz, String staticFactoryMethod) {
    factoryMethod = method(clazz, staticFactoryMethod);

    Class[] factoryParamTypes = factoryMethod.getParameterTypes();
    if (factoryParamTypes.length != new HashSet<>(Arrays.asList(factoryParamTypes)).size()) {
      throw new IllegalArgumentException("Static factory entity mapper will not work for " + clazz +
          " because its factory method takes arguments of the same type and Java reflection cannot " +
          " give methods in declared order. User a builder or custom entity mapper instead.");
    }

    // For AutoValue types, the abstract method names will be the property list.
    // Allow this to work for either the abstract @AutoValue class or the implementation class.
    if (clazz.getSimpleName().startsWith("AutoValue_")) {
      propertyNames = abstractPropertyMethods(clazz.getSuperclass());
    } else {
      propertyNames = abstractPropertyMethods(clazz);
    }

    propertyMethods = propertyNames.stream()
        .filter(name -> !name.equals("id"))
        .map(name -> method(clazz, name)).collect(Collectors.toList());

    idMethod = method(clazz, "id");
    modelClass = clazz;
  }

  private List<String> abstractPropertyMethods(Class clazz) {
    List<Method> methods = Arrays.asList(clazz.getDeclaredMethods()).stream()
        .filter(m -> isDatastoreType(m.getReturnType()) && Modifier.isAbstract(m.getModifiers()))
        .collect(Collectors.toList());

    List<Class> factoryParamTypes = Arrays.asList(factoryMethod.getParameterTypes());
    for (Method method : methods) {
      if (!factoryParamTypes.contains(method.getReturnType())) {
        throw new IllegalStateException("Expected return type of property method " +
            method.getName() + " of " + clazz + " to be in the type list of factory method.");
      }
    }

    // Because the list of methods comes in in an undefined order, sort them to be in the factory
    // method parameter order.
    methods.sort((m1, m2) -> Integer.compare(
        factoryParamTypes.indexOf(m1.getReturnType()),
        factoryParamTypes.indexOf(m2.getReturnType())));

    return methods.stream().map(m -> m.getName()).collect(Collectors.toList());
  }

  private List<String> fieldNames(Class clazz) {
    return Arrays.asList(clazz.getDeclaredFields()).stream()
        .filter(m -> isDatastoreType(m.getType()))
        .map(f -> f.getName()).collect(Collectors.toList());
  }


  @Override
  public Entity toEntity(Model model) {
    if (model.getClass() != modelClass) {
      throw new IllegalArgumentException("Expected model of class " + modelClass + " got " + model);
    }

    Entity.Builder builder = Entity.newBuilder();
    builder.setKey(makeKey(entityKind(model.getClass()), getModelId(model)));
    propertyMethods.forEach(method ->
        builder.addProperty(makeProperty(method.getName(), toValue(invoke(method, model)))));
    return builder.build();
  }

  @Override
  public <T extends Model> T fromEntity(Entity entity, Class<T> clazz) {
    if (clazz != modelClass) {
      throw new IllegalArgumentException("Class " + modelClass + " got class " + clazz);
    }

    return clazz.cast(invoke(factoryMethod, null, factoryArgs(entity)));
  }

  private Object[] factoryArgs(Entity entity) {
    Map<String, Value> entityProperties = getPropertyMap(entity);
    return propertyNames.stream().map(fieldName -> {
      if (fieldName == "id") {
        return entityId(entity);
      } else {
        return fromValue(entityProperties.get(fieldName));
      }
    }).toArray();
  }

  @Override
  public Object getModelId(Model model) {
    return invoke(idMethod, model);
  }

  @Override
  public String entityKind(Class clazz) {
    return kindForClass(clazz);
  }
}
