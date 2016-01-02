package org.draff;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;
import static com.google.api.services.datastore.client.DatastoreHelper.*;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.DatastoreV1.Key.PathElement;
import java.util.Map;

/**
 * Created by dave on 1/2/16.
 */
public class EntityMapperTest {
  class TestModel {
    long id;
    String stringProp;
    long longProp;
  }

  class TestModelWithIdMethod {
    String stringProp;
    long longProp;
    String id() {
      return stringProp + ":" + longProp;
    }
  }

  @Before
  public void setup() {
    System.out.println("Settting up test");
  }

  @Test
  public void testObjectToEntityWithIdField() {
    TestModel model = new TestModel();
    model.id = 5;
    model.stringProp = "hi";
    model.longProp = -3;

    Entity entity = EntityMapper.objectToEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals(5, pathElement.getId());
    assertEquals("TestModel", pathElement.getKind());

    assertEquals(2, props.keySet().size());
    assertEquals(-3, getLong(props.get("longProp")));
    assertEquals("hi", getString(props.get("stringProp")));
  }

  @Test
  public void testObjectToEntityWithIdMethod() {
    TestModelWithIdMethod model = new TestModelWithIdMethod();
    model.stringProp = "h";
    model.longProp = 1;

    Entity entity = EntityMapper.objectToEntity(model);
    Map<String, Value> props = getPropertyMap(entity);
    PathElement pathElement = entity.getKey().getPathElement(0);
    assertEquals("h:1", pathElement.getName());
    assertEquals("TestModelWithIdMethod", pathElement.getKind());

    assertEquals(1, getLong(props.get("longProp")));
    assertEquals("h", getString(props.get("stringProp")));
  }

  @Test
  public void testEntityToObject() {
  }
}