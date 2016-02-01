package org.draff.objectdb;

import com.google.api.services.datastore.DatastoreV1.Entity;
import com.google.api.services.datastore.DatastoreV1.Key.PathElement;
import com.google.api.services.datastore.DatastoreV1.Value;
import com.google.auto.value.AutoValue;

import org.junit.Test;

import java.util.Map;

import static com.google.api.services.datastore.client.DatastoreHelper.*;
import static org.junit.Assert.*;

@AutoValue
abstract class TestModel implements Model {
  abstract long id();
  abstract String stringProp();
  abstract long longProp();

  static Builder builder() {
    return new AutoValue_TestModel.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder id(long id);
    abstract Builder stringProp(String s);
    abstract Builder longProp(long l);
    abstract TestModel build();
  }
}

@AutoValue
abstract class TestModelWithIdMethod implements Model {
  abstract String stringProp();
  abstract long longProp();

  static TestModelWithIdMethod create(String stringProp, long longProp) {
    return new AutoValue_TestModelWithIdMethod(stringProp, longProp);
  }

  public String id() {
    return stringProp() + ":" + longProp();
  }
}

/**
 * Created by dave on 1/2/16.
 */
public class EntityMapperTest {
  @Test
  public void testToEntityWithIdField() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();

    TestModel model = TestModel.builder().id(5).stringProp("hi").longProp(-3).build();

    Entity entity = mapper.toEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals(5, pathElement.getId());
    assertEquals("TestModel", pathElement.getKind());

    assertEquals(2, props.keySet().size());
    assertEquals(-3, getLong(props.get("longProp")));
    assertEquals("hi", getString(props.get("stringProp")));
  }

  @Test
  public void testToEntityWithIdMethod() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();

    TestModelWithIdMethod model = TestModelWithIdMethod.create("h", 1);

    Entity entity = mapper.toEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals("h:1", pathElement.getName());
    assertEquals("TestModelWithIdMethod", pathElement.getKind());

    assertEquals(1, getLong(props.get("longProp")));
    assertEquals("h", getString(props.get("stringProp")));
  }

  @Test
  public void testFromEntity() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey("TestModel", 5))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();

    TestModel model = new ManagingEntityMapper().fromEntity(entity, TestModel.class);
    assertEquals("str", model.stringProp());
    assertEquals(-6, model.longProp());
    assertEquals(5, model.id());
  }

  @Test
  public void testFromEntityWithIdMethod() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey("TestModelWithIdMethod", "str:-6"))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();

    TestModelWithIdMethod model =
        new ManagingEntityMapper().fromEntity(entity, TestModelWithIdMethod.class);
    assertEquals("str", model.stringProp());
    assertEquals(-6, model.longProp());
    assertEquals("str:-6", model.id());
  }

  @Test
  public void testGivesAndReceivesNull() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();
    assertNull(mapper.toEntity(null));
    assertNull(mapper.fromEntity(null, TestModel.class));
  }
}