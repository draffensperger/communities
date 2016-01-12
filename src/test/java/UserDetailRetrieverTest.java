import org.draff.UserDetailRetriever;
import org.draff.models.FollowersGoal;
import org.draff.models.FollowersTracker;
import org.draff.models.UserDetail;
import org.draff.models.UserDetailRequest;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dave on 1/9/16.
 */
public class UserDetailRetrieverTest {
  private DatastoreDb db;

  @Before
  public void setup() {
    db = new DatastoreDb(TestDatastore.get());
    TestDatastore.clean();
  }

  @Test
  public void testRetrieveFollowersGoalDetails() throws TwitterException {
    FollowersGoal goal1 = new FollowersGoal();
    goal1.id = "user1";
    goal1.depthGoal = 1;
    FollowersGoal goal2 = new FollowersGoal();
    goal2.id = "user2";
    goal2.depthGoal = 2;
    db.saveAll(Arrays.asList(goal1, goal2));
    waitForEventualSave(FollowersGoal.class);

    FollowersTracker existingFollowersTracker = new FollowersTracker();
    existingFollowersTracker.id = 10;
    existingFollowersTracker.friendsRetrieved = true;
    db.save(existingFollowersTracker);
    waitForEventualSave(FollowersTracker.class);

    UserDetailRetriever retriever = new UserDetailRetriever(db, mockUserResources());
    retriever.retrieveFollowersGoalDetails();

    waitForEventualDelete(FollowersGoal.class);
    assertNull(db.findOne(FollowersGoal.class));

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

  @Test
  public void testRetrieveUserIdsBatchDetails() throws TwitterException {
    UserDetailRequest request1 = new UserDetailRequest();
    request1.id = 10;
    UserDetailRequest request2 = new UserDetailRequest();
    request2.id = 20;
    UserDetailRequest request3 = new UserDetailRequest();
    request3.id = 30;
    db.saveAll(Arrays.asList(request1, request2, request3));
    waitForEventualSave(UserDetailRequest.class);

    UserDetail existingDetail = new UserDetail();
    existingDetail.id = 30;
    existingDetail.screenName = "user3";
    db.save(existingDetail);
    waitForEventualSave(UserDetail.class);

    UserDetailRetriever retriever = new UserDetailRetriever(db, mockUserResources());
    retriever.retrieveUserIdsBatchDetails();

    waitForEventualDelete(UserDetailRequest.class);
    assertNull(db.findOne(UserDetailRequest.class));

    UserDetail detail1 = db.findById(UserDetail.class, 10);
    assertNotNull(detail1);
    assertEquals("user1", detail1.screenName);

    UserDetail detail2 = db.findById(UserDetail.class, 20);
    assertNotNull(detail2);
    assertEquals("user2", detail2.screenName);

    UserDetail detail3 = db.findById(UserDetail.class, 30);
    assertNotNull(detail3);
    assertEquals("user3", detail3.screenName);
  }

  private UsersResources mockUserResources() throws TwitterException {
    List<User> users = new ArrayList<>();
    String user1JSON = "{\"id\":10,\"screen_name\":\"user1\"}";
    String user2JSON = "{\"id\":20,\"screen_name\":\"user2\"}";
    users.add(TwitterObjectFactory.createUser(user1JSON));
    users.add(TwitterObjectFactory.createUser(user2JSON));

    UsersResources userResources = mock(UsersResources.class);

    ResponseList<User> usersResponse = mock(ResponseList.class, delegatesTo(users));
    when(userResources.lookupUsers(new String[] {"user1", "user2"})).thenReturn(usersResponse);
    when(userResources.lookupUsers(new long[] {10, 20})).thenReturn(usersResponse);
    when(userResources.lookupUsers(new long[] {20, 10})).thenReturn(usersResponse);

    return userResources;
  }
}
