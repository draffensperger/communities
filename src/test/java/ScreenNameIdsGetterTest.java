import org.draff.ScreenNameIdsGetter;
import org.draff.models.FollowersTracker;
import org.draff.models.ScreenNameTracker;
import org.draff.models.UserDetail;
import org.draff.objectdb.DatastoreDb;
import org.draff.support.TestDatastore;
import org.junit.Before;
import org.junit.Test;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.draff.support.EventualConsistencyHelper.waitForEventualDelete;
import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dave on 1/9/16.
 */
public class ScreenNameIdsGetterTest {
  private DatastoreDb db;

  @Before
  public void setup() {
    db = new DatastoreDb(TestDatastore.get());
    TestDatastore.clean();
  }

  @Test
  public void testRun() throws TwitterException {
    ScreenNameTracker tracker1 = new ScreenNameTracker();
    tracker1.id = "user1";
    tracker1.depthGoal = 1;
    ScreenNameTracker tracker2 = new ScreenNameTracker();
    tracker2.id = "user2";
    tracker2.depthGoal = 2;
    db.saveAll(Arrays.asList(tracker1, tracker2));
    waitForEventualSave(ScreenNameTracker.class);

    FollowersTracker existingFollowersTracker = new FollowersTracker();
    existingFollowersTracker.id = 10;
    existingFollowersTracker.friendsRetrieved = true;
    db.save(existingFollowersTracker);
    waitForEventualSave(FollowersTracker.class);

    ScreenNameIdsGetter getter = new ScreenNameIdsGetter(db, mockUserResources());
    getter.runBatch();

    waitForEventualDelete(ScreenNameTracker.class);
    assertNull(db.findOne(ScreenNameTracker.class));

    UserDetail userDetail1 = db.findById(UserDetail.class, 10);
    assertNotNull(userDetail1);
    assertEquals("user1", userDetail1.screenName);

    UserDetail userDetail2 = db.findById(UserDetail.class, 20);
    assertNotNull(userDetail2);
    assertEquals("user2", userDetail2.screenName);

    FollowersTracker followersTracker1 = db.findById(FollowersTracker.class, 10);
    assertNotNull(followersTracker1);
    assertTrue(followersTracker1.shouldRetrieveFriends);
    assertTrue(followersTracker1.shouldRetrieveFriends);
    assertTrue(followersTracker1.friendsRetrieved);
    assertFalse(followersTracker1.shouldRetrieveLevel2Followers);
    assertFalse(followersTracker1.shouldRetrieveLevel2Friends);

    FollowersTracker followersTracker2 = db.findById(FollowersTracker.class, 20);
    assertNotNull(followersTracker2);
    assertTrue(followersTracker2.shouldRetrieveFriends);
    assertTrue(followersTracker2.shouldRetrieveFriends);
    assertTrue(followersTracker2.shouldRetrieveLevel2Followers);
    assertTrue(followersTracker2.shouldRetrieveLevel2Friends);
  }

  private UsersResources mockUserResources() throws TwitterException {
    List<User> users = new ArrayList<>();
    String user1JSON = "{\"id\":10,\"screen_name\":\"user1\"}";
    String user2JSON = "{\"id\":20,\"screen_name\":\"user2\"}";
    users.add(TwitterObjectFactory.createUser(user1JSON));
    users.add(TwitterObjectFactory.createUser(user2JSON));

    UsersResources userResources = mock(UsersResources.class);
    String[] screenNames = {"user1", "user2"};

    ResponseList<User> usersResponse = mock(ResponseList.class, delegatesTo(users));

    when(userResources.lookupUsers(new String[] {"user1", "user2"}))
        .thenReturn(usersResponse);
    return userResources;
  }
}
