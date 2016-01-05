package org.draff;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.google.api.services.datastore.client.DatastoreHelper.*;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.DatastoreV1.Key.PathElement;
import java.util.Map;

/**
 * Created by dave on 1/2/16.
 */
public class EntityMapperTest {
  private static final String kindPrefix = EntityMapperTest.class.getPackage().getName() + ".";

  @Before
  public void setup() {
    TestDatastore.clean();
  }

  @Test
  public void testToEntityWithIdField() {
    TestModel model = new TestModel();
    model.id = 5;
    model.stringProp = "hi";
    model.longProp = -3;

    Entity entity = EntityMapper.toEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals(5, pathElement.getId());
    assertEquals(kindPrefix + "TestModel", pathElement.getKind());

    assertEquals(2, props.keySet().size());
    assertEquals(-3, getLong(props.get("longProp")));
    assertEquals("hi", getString(props.get("stringProp")));
  }

  @Test
  public void testToEntityWithIdMethod() {
    TestModelWithIdMethod model = new TestModelWithIdMethod();
    model.stringProp = "h";
    model.longProp = 1;

    Entity entity = EntityMapper.toEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals("h:1", pathElement.getName());
    assertEquals(kindPrefix + "TestModelWithIdMethod", pathElement.getKind());

    assertEquals(1, getLong(props.get("longProp")));
    assertEquals("h", getString(props.get("stringProp")));
  }

  @Test
  public void testFromEntity() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey(kindPrefix + "TestModel", 5))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();

    TestModel model = (TestModel) EntityMapper.fromEntity(entity);
    assertEquals("str", model.stringProp);
    assertEquals(-6, model.longProp);
    assertEquals(5, model.id);
  }

  @Test
  public void testFromEntityWithIdMethod() {
    Entity entity = Entity.newBuilder()
        .setKey(makeKey(kindPrefix + "TestModelWithIdMethod", "str:-6"))
        .addProperty(makeProperty("stringProp", makeValue("str")))
        .addProperty(makeProperty("longProp", makeValue(-6)))
        .build();

    TestModelWithIdMethod model = (TestModelWithIdMethod) EntityMapper.fromEntity(entity);
    assertEquals("str", model.stringProp);
    assertEquals(-6, model.longProp);
    assertEquals("str:-6", model.id());
  }

  @Test
  public void testGivesAndReceivesNull() {
    assertNull(EntityMapper.toEntity(null));
    assertNull(EntityMapper.fromEntity(null));
  }
}