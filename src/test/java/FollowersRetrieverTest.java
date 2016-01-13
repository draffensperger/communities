import org.draff.FollowersRetriever;
import org.draff.models.Follower;
import org.draff.models.FollowersTracker;
import org.draff.objectdb.DatastoreDb;
import org.draff.support.TestDatastore;
import org.junit.Before;
import org.junit.Test;

import twitter4j.IDs;
import twitter4j.TwitterException;
import twitter4j.api.FriendsFollowersResources;

import java.util.List;

import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by dave on 1/12/16.
 */
public class FollowersRetrieverTest {
  private DatastoreDb db;

  @Before
  public void setup() {
    db = new DatastoreDb(TestDatastore.get());
    TestDatastore.clean();
  }

  @Test
  public void testRetrieveFollowers() throws TwitterException {
    FollowersRetriever retriever = new FollowersRetriever(db, mockFriendsFollowers());

    FollowersTracker tracker = new FollowersTracker();
    tracker.id = 1;
    tracker.shouldRetrieveFollowers = true;
    db.save(tracker);
    waitForEventualSave(FollowersTracker.class);

    retriever.retrieveBatch();

    waitForEventualSave(Follower.class);

    System.out.println("stop");

    FollowersTracker updatedTracker = db.findById(FollowersTracker.class, 1L);
    assertEquals(true, updatedTracker.shouldRetrieveFollowers);
    assertEquals(false, updatedTracker.followersRetrieved);
    assertEquals(1001L, updatedTracker.followersCursor);

    List<Follower> followers = db.find(Follower.class, 2);
    followers.sort((f1, f2) -> f1.id().compareTo(f2.id()));
    assertEquals(2, followers.size());

    Follower follower0 = followers.get(0);
    assertEquals("1:2", follower0.id());
    assertEquals(1L, follower0.userId);
    assertEquals(2L, follower0.followerId);
    assertThat(follower0.retrievedAt, greaterThan(0L));

    Follower follower1 = followers.get(1);
    assertEquals("1:3", follower1.id());
    assertEquals(1L, follower1.userId);
    assertEquals(3L, follower1.followerId);
    assertThat(follower1.retrievedAt, greaterThan(0L));
  }

  private FriendsFollowersResources mockFriendsFollowers() throws TwitterException {
    FriendsFollowersResources friendsFollowers = mock(FriendsFollowersResources.class);

    IDs ids = mock(IDs.class);
    when(ids.getIDs()).thenReturn(new long[]{2L, 3L});
    when(ids.hasNext()).thenReturn(true);
    when(ids.getNextCursor()).thenReturn(1001L);

    when(friendsFollowers.getFollowersIDs(1L, -1L)).thenReturn(ids);

    return friendsFollowers;
  }
}
