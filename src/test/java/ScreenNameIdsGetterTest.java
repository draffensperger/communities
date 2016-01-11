import org.draff.ScreenNameIdsGetter;
import org.draff.models.ScreenNameTracker;
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

import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
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
    tracker1.depthGoal = 2;
    ScreenNameTracker tracker2 = new ScreenNameTracker();
    tracker2.id = "user2";
    tracker2.depthGoal = 3;
    db.saveAll(Arrays.asList(tracker1, tracker2));
    waitForEventualSave(ScreenNameTracker.class);

    List<User> users = new ArrayList<>();
    String user1JSON = "{\"id\":1,\"screen_name\":\"user1\"}";
    String user2JSON = "{\"id\":2,\"screen_name\":\"user2\"}";
    users.add(TwitterObjectFactory.createUser(user1JSON));
    users.add(TwitterObjectFactory.createUser(user2JSON));

    UsersResources userResources = mock(UsersResources.class);
    String[] screenNames = {"user1", "user2"};

    ResponseList<User> usersResponse = mock(ResponseList.class, delegatesTo(users));

    when(userResources.lookupUsers(new String[] {"user1", "user2"}))
        .thenReturn(usersResponse);

    ScreenNameIdsGetter getter = new ScreenNameIdsGetter(db, userResources);
    getter.run();


  }
}
