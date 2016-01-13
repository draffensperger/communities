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
    FollowersTracker tracker = new FollowersTracker();
    tracker.id = 1L;
    tracker.shouldRetrieveFollowers = true;
    db.save(tracker);
    waitForEventualSave(FollowersTracker.class);

    new FollowersRetriever(db, mockFriendsFollowers()).retrieveFollowersBatch();

    waitForEventualSave(Follower.class);

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

  @Test
  public void testRetrieveFriends() throws TwitterException {
    FollowersTracker tracker = new FollowersTracker();
    tracker.id = 1L;
    tracker.shouldRetrieveFriends = true;
    db.save(tracker);
    waitForEventualSave(FollowersTracker.class);

    new FollowersRetriever(db, mockFriendsFollowers()).retrieveFriendsBatch();

    waitForEventualSave(Follower.class);

    FollowersTracker updatedTracker = db.findById(FollowersTracker.class, 1L);
    assertEquals(true, updatedTracker.shouldRetrieveFriends);
    assertEquals(false, updatedTracker.friendsRetrieved);
    assertEquals(2002L, updatedTracker.friendsCursor);

    List<Follower> followers = db.find(Follower.class, 2);
    followers.sort((f1, f2) -> f1.id().compareTo(f2.id()));
    assertEquals(2, followers.size());

    Follower follower0 = followers.get(0);
    assertEquals("4:1", follower0.id());
    assertEquals(4L, follower0.userId);
    assertEquals(1L, follower0.followerId);
    assertThat(follower0.retrievedAt, greaterThan(0L));

    Follower follower1 = followers.get(1);
    assertEquals("5:1", follower1.id());
    assertEquals(5L, follower1.userId);
    assertEquals(1L, follower1.followerId);
    assertThat(follower1.retrievedAt, greaterThan(0L));
  }

  private FriendsFollowersResources mockFriendsFollowers() throws TwitterException {
    FriendsFollowersResources friendsFollowers = mock(FriendsFollowersResources.class);

    IDs followerIds = mock(IDs.class);
    when(followerIds.getIDs()).thenReturn(new long[]{2L, 3L});
    when(followerIds.hasNext()).thenReturn(true);
    when(followerIds.getNextCursor()).thenReturn(1001L);

    when(friendsFollowers.getFollowersIDs(1L, -1L)).thenReturn(followerIds);

    IDs friendsIds = mock(IDs.class);
    when(friendsIds.getIDs()).thenReturn(new long[]{4L, 5L});
    when(friendsIds.hasNext()).thenReturn(true);
    when(friendsIds.getNextCursor()).thenReturn(2002L);

    when(friendsFollowers.getFriendsIDs(1L, -1L)).thenReturn(friendsIds);

    return friendsFollowers;
  }
}
