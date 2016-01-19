import org.draff.FollowersBatchFetcher;
import org.draff.models.Follower;
import org.draff.models.FollowersTracker;
import org.draff.models.UserDetailRequest;
import org.draff.objectdb.DatastoreDb;
import org.draff.support.TestDatastore;
import org.junit.Before;
import org.junit.Test;

import twitter4j.IDs;
import twitter4j.TwitterException;
import twitter4j.api.FriendsFollowersResources;

import java.util.Arrays;
import java.util.List;

import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by dave on 1/12/16.
 */
public class FollowersBatchFetcherTest {
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

    new FollowersBatchFetcher(db, mockFriendsFollowers()).fetchFollowersBatch();

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

    Follower follower1 = followers.get(1);
    assertEquals("1:3", follower1.id());
    assertEquals(1L, follower1.userId);
    assertEquals(3L, follower1.followerId);
  }

  @Test
  public void testRetrieveFriends() throws TwitterException {
    FollowersTracker tracker = new FollowersTracker();
    tracker.id = 1L;
    tracker.shouldRetrieveFriends = true;
    db.save(tracker);
    waitForEventualSave(FollowersTracker.class);

    new FollowersBatchFetcher(db, mockFriendsFollowers()).fetchFriendsBatch();

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

    Follower follower1 = followers.get(1);
    assertEquals("5:1", follower1.id());
    assertEquals(5L, follower1.userId);
    assertEquals(1L, follower1.followerId);
  }

  @Test
  public void testAddsLevel2Trackers() throws TwitterException {
    FollowersTracker existingTracker1 = new FollowersTracker();
    existingTracker1.id = 1L;
    existingTracker1.shouldRetrieveFollowers = true;
    existingTracker1.shouldRetrieveLevel2Followers = true;
    existingTracker1.shouldRetrieveLevel2Friends = true;

    FollowersTracker existingTracker2 = new FollowersTracker();
    existingTracker2.id = 2L;
    existingTracker2.shouldRetrieveLevel2Friends = true;

    db.saveAll(Arrays.asList(existingTracker1, existingTracker2));
    waitForEventualSave(FollowersTracker.class);

    UserDetailRequest existingDetailRequest = new UserDetailRequest();
    existingDetailRequest.id = 2L;
    existingDetailRequest.detailRetrieved = true;
    db.save(existingDetailRequest);

    new FollowersBatchFetcher(db, mockFriendsFollowers()).fetchFollowersBatch();
    waitForEventualSave(Follower.class);

    List<Follower> followers = db.find(Follower.class, 3);
    assertEquals(2, followers.size());

    List<UserDetailRequest> detailRequests = db.find(UserDetailRequest.class, 3);
    assertEquals(2, detailRequests.size());
    detailRequests.sort((d1, d2) -> Long.compare(d1.id, d2.id));

    UserDetailRequest detailRequest1 = detailRequests.get(0);
    assertEquals(2L, detailRequest1.id);
    // check that it kept the existing retrieved detail request for user 2 marked as retrieved
    assertTrue(detailRequest1.detailRetrieved);

    UserDetailRequest detailRequest2 = detailRequests.get(1);
    assertEquals(3L, detailRequest2.id);
    // check that the new detail request for user 3 is marked as not retrieved
    assertFalse(detailRequest2.detailRetrieved);

    List<FollowersTracker> trackers = db.find(FollowersTracker.class, 4);
    assertEquals(3, trackers.size());
    trackers.sort((t1, t2) -> Long.compare(t1.id, t2.id));

    FollowersTracker tracker1 = trackers.get(0);
    assertEquals(1L, tracker1.id);

    FollowersTracker tracker2 = trackers.get(1);
    assertEquals(2L, tracker2.id);
    assertTrue(tracker2.shouldRetrieveFriends);
    assertTrue(tracker2.shouldRetrieveFollowers);
    assertFalse(tracker2.shouldRetrieveLevel2Followers);
    // check that it preserved the previously set value for retrieving level 2 friends
    assertTrue(tracker2.shouldRetrieveLevel2Friends);

    FollowersTracker tracker3 = trackers.get(2);
    assertEquals(3L, tracker3.id);
    assertTrue(tracker3.shouldRetrieveFriends);
    assertTrue(tracker3.shouldRetrieveFollowers);
    // check that it defaults level to retrieval fields to false
    assertFalse(tracker3.shouldRetrieveLevel2Followers);
    assertFalse(tracker3.shouldRetrieveLevel2Friends);
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
