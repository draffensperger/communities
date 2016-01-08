package org.draff;

import org.junit.Before;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import com.google.common.collect.ImmutableMap;
import static org.junit.Assert.*;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.DatastoreV1.Key.PathElement;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
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
    TestDatastore.clean();
  }

  @Test
  public void testSaveFindOneAndDelete() {
    FollowersCursor cursor = new FollowersCursor();
    cursor.id = 1;
    cursor.cursor = 10;

    assertNull(db.findOne(FollowersCursor.class));

    db.save(cursor);
    waitForEventualSave(FollowersCursor.class);

    FollowersCursor retrievedCursor = db.findOne(FollowersCursor.class);
    assertNotNull(retrievedCursor);

    // Check that they are different objects, i.e. not cached or something
    assertTrue(cursor != retrievedCursor);
    assertEquals(cursor.id, retrievedCursor.id);
    assertEquals(cursor.cursor, retrievedCursor.cursor);

    // Check that the cursor is still there
    assertNotNull(db.findOne(FollowersCursor.class));

    db.delete(cursor);
    waitOnEventualConsistency(() -> db.findOne(Follower.class) == null);

    assertNull(db.findOne(FollowersCursor.class));
  }

  @Test
  public void testSaveMultipleAndConstrainedFind() {
    Follower follower1 = new Follower();
    follower1.userId = 3;
    follower1.followerId = 4;

    Follower follower2 = new Follower();
    follower2.userId = 5;
    follower2.followerId = 6;

    db.save(Arrays.asList(follower1, follower2));
    waitForEventualSave(Follower.class);

    Map<String, Object> constraints1 =
        new ImmutableMap.Builder<String, Object>()
            .put("userId", 3L).build();
    Follower found = db.findOne(Follower.class, constraints1);
    assertNotNull(found);
    assertEquals(3, found.userId);
    assertEquals(4, found.followerId);

    Map<String, Object> constraints2 =
        new ImmutableMap.Builder<String, Object>()
            .put("userId", 5L).put("followerId", 6L).build();
    assertNotNull(db.findOne(Follower.class, constraints2));

    Map<String, Object> constraints3 =
        new ImmutableMap.Builder<String, Object>()
            .put("userId", -5L).put("followerId", 6L).build();
    assertNull(db.findOne(Follower.class, constraints3));
  }

  @Test
  public void testFindByIds() {
    User user1 = new User();
    user1.id = 1;
    user1.depthGoal = 2;
    User user2 = new User();
    user2.id = 2;
    user2.depthGoal = 1;

    db.save(Arrays.asList(user1, user2));
    waitForEventualSave(User.class);

    List<User> users = db.findByIds(User.class, Arrays.asList(1, 2));
    assertEquals(2, users.size());
    User found1 = users.get(0);
    User found2 = users.get(1);
    if (found1.id > found2.id) {
      User temp = found1;
      found2 = found1;
      found1 = temp;
    }

    assertEquals(1, found1.id);
    assertEquals(2, found1.depthGoal);
    assertEquals(2, found2.id);
    assertEquals(1, found2.depthGoal);
  }

  private void waitForEventualSave(Class clazz) {
    waitOnEventualConsistency(() -> db.findOne(clazz) != null);
  }

  private void waitOnEventualConsistency(Callable<Boolean> condition) {
    Awaitility.await().atMost(1, TimeUnit.SECONDS).until(condition);
  }
}
