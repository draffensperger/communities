package org.draff.objectdb;

import com.google.common.collect.ImmutableMap;

import org.draff.support.TestDatastore;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
import static org.draff.support.EventualConsistencyHelper.waitForEventualDelete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by dave on 1/3/16.
 */

class User implements Model {
  long id;
  long depthGoal;
  boolean followersRetrieved;
}

class Follower implements Model {
  long userId;
  long followerId;
  public String id() {
    return userId + ":" + followerId;
  }
}

class FollowersCursor implements Model {
  long id;
  long cursor;
}

class Friend implements Model {
  User parent;
  long id;
}

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
    waitForEventualDelete(Follower.class);

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

    db.saveAll(Arrays.asList(follower1, follower2));
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

    db.saveAll(Arrays.asList(user1, user2));
    waitForEventualSave(User.class);

    List<User> users = db.findByIds(User.class, Arrays.asList(1L, 2L));
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

  @Test
  public void testFindById() {
    assertNull(db.findById(User.class, 8L));

    User user = new User();
    user.id = 8;
    user.depthGoal = 5;
    db.save(user);
    waitForEventualSave(User.class);

    User found = db.findById(User.class, 8L);
    assertNotNull(found);
    assertEquals(8, found.id);
    assertEquals(5, found.depthGoal);
  }

  @Test
  public void testSaveAndFindChildren() {
    User user = new User();
    user.id = 1L;
    db.save(user);
    waitForEventualSave(User.class);

    assertTrue(db.findChildren(user, Friend.class, 1, Long.MIN_VALUE).isEmpty());
    Friend friend1 = new Friend();
    friend1.parent = user;
    friend1.id = 4L;

    Friend friend2 = new Friend();
    friend2.parent = user;
    friend2.id = 5L;

    db.saveAll(Arrays.asList(friend1, friend2));
    waitForEventualSave(Friend.class);

    try {
      Thread.sleep(500);
    } catch(InterruptedException e) {}
    List<Friend> friends = db.findChildren(user, Friend.class, 3, Long.MIN_VALUE);
    friends.sort((f1, f2) -> Long.compare(f1.id, f2.id));
    assertEquals(2, friends.size());

    assertEquals(4L, friends.get(0).id);
    assertEquals(1L, friends.get(0).parent.id);

    assertEquals(5L, friends.get(1).id);
    assertEquals(1L, friends.get(1).parent.id);
  }
}
