package org.draff;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.google.api.services.datastore.client.DatastoreHelper.*;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.DatastoreV1.Key.PathElement;
import java.util.Map;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.api.services.datastore.client.DatastoreHelper.*;

/**
 * Created by dave on 1/3/16.
 */
public class DatastoreDbTest {
  private DatastoreDb db;

  @Before
  public void setup() {
    db = new DatastoreDb(TestDatastore.get());
  }

  @Test
  public void testSaveGetAndDeleteCursors() {
    TestDatastore.clean();

    FollowersCursor cursor = new FollowersCursor();
    cursor.id = 1;
    cursor.cursor = 10;

    assertNull(db.findOne(FollowersCursor.class));
    db.save(cursor);
    FollowersCursor retrievedCursor = db.findOne(FollowersCursor.class);

    // Check that they are different objects, i.e. not cached or something
    assertTrue(cursor != retrievedCursor);
    assertEquals(cursor.id, retrievedCursor.id);
    assertEquals(cursor.cursor, retrievedCursor.cursor);

    // Check that the cursor is still there
    assertNotNull(db.findOne(FollowersCursor.class));

    db.delete(cursor);
    assertNull(db.findOne(FollowersCursor.class));
  }
}
