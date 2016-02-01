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
abstract class TestBuilderModel implements Model {
  abstract long id();
  abstract String stringProp();
  abstract long longProp();

  static Builder builder() {
    return new AutoValue_TestBuilderModel.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder id(long id);
    abstract Builder stringProp(String s);
    abstract Builder longProp(long l);
    abstract TestBuilderModel build();
  }
}

@AutoValue
abstract class TestFactoryModel implements Model {
  abstract String stringProp();
  abstract long longProp();

  static TestFactoryModel create(String stringProp, long longProp) {
    return new AutoValue_TestFactoryModel(stringProp, longProp);
  }

  public String id() {
    return stringProp() + ":" + longProp();
  }
}

@AutoValue
abstract class TestAmbiguousFactoryModel implements Model {
  abstract long id();
  abstract long longProp();

  static TestAmbiguousFactoryModel create(long id, long longProp) {
    return new AutoValue_TestAmbiguousFactoryModel(id, longProp);
  }
}

class TestMutableModel implements Model {
  long id;
  String screenName;
}

/**
 * Created by dave on 1/2/16.
 */
public class EntityMapperTest {
  @Test
  public void testBuilderModelToEntity() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();

    TestBuilderModel model = TestBuilderModel.builder().id(5).stringProp("hi").longProp(-3).build();

    Entity entity = mapper.toEntity(model);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals(5, pathElement.getId());
    assertEquals("TestBuilderModel", pathElement.getKind());

    Map<String, Value> props = getPropertyMap(entity);
    assertEquals(2, props.keySet().size());
    assertEquals(-3, getLong(props.get("longProp")));
    assertEquals("hi", getString(props.get("stringProp")));
  }

  @Test
  public void testBuilderModelFromEntity() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey("TestBuilderModel", 5))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();
    TestBuilderModel model = new ManagingEntityMapper().fromEntity(entity, TestBuilderModel.class);
    assertEquals("str", model.stringProp());
    assertEquals(-6, model.longProp());
    assertEquals(5, model.id());
  }

  @Test
  public void testFactoryModelToEntity() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();

    TestFactoryModel model = TestFactoryModel.create("h", 1);

    Entity entity = mapper.toEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals("h:1", pathElement.getName());
    assertEquals("TestFactoryModel", pathElement.getKind());

    assertEquals(1, getLong(props.get("longProp")));
    assertEquals("h", getString(props.get("stringProp")));
  }

  @Test
  public void testFactoryModelFromEntity() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey("TestFactoryModel", "str:-6"))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();

    TestFactoryModel model =
        new ManagingEntityMapper().fromEntity(entity, TestFactoryModel.class);
    assertEquals("str", model.stringProp());
    assertEquals(-6, model.longProp());
    assertEquals("str:-6", model.id());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAmbiguousFactoryGivesException() {
    // Because Java reflection does not return methods in declared order, the entity mapper can't
    // determine which property is which to pass into the "create" factory method, so expect that it
    // will return an IllegalArgumentException.
    TestAmbiguousFactoryModel model = TestAmbiguousFactoryModel.create(1L, 2L);
    new ManagingEntityMapper().toEntity(model);
  }

  @Test
  public void testMutableModelToEntity() {
    TestMutableModel model = new TestMutableModel();
    model.id = 1L;
    model.screenName = "user";

    Entity entity = new ManagingEntityMapper().toEntity(model);

    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals(1L, pathElement.getId());
    assertEquals("TestMutableModel", pathElement.getKind());

    Map<String, Value> props = getPropertyMap(entity);
    assertEquals(1, props.keySet().size());
    assertEquals("user", getString(props.get("screenName")));
  }

  @Test
  public void testMutableModelFromEntity() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey("TestMutableModel", 5L))
        .addProperty(makeProperty("screenName", makeValue("user")))
        .build();
    TestMutableModel model = new ManagingEntityMapper().fromEntity(entity, TestMutableModel.class);
    assertEquals("user", model.screenName) ;
    assertEquals(5L, model.id);
  }

  @Test
  public void testGivesAndReceivesNull() {
    ManagingEntityMapper mapper = new ManagingEntityMapper();
    assertNull(mapper.toEntity(null));
    assertNull(mapper.fromEntity(null, TestBuilderModel.class));
  }
}