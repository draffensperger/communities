package org.draff;

import org.draff.mapper.DbWithMappers;
import org.draff.model.*;
import org.draff.objectdb.DatastoreDb;
import org.draff.support.TestDatastore;
import org.draff.twitfetch.FollowersBatchFetcher;
import org.draff.twitfetch.FollowersStorer;
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
    db = DbWithMappers.create(TestDatastore.get());
    TestDatastore.clean();
  }

  @Test
  public void testRetrieveFollowers() throws TwitterException {
    FollowersTracker tracker = FollowersTracker.builder().id(1L).shouldFetchFollowers(true).build();
    db.save(tracker);
    waitForEventualSave(FollowersTracker.class);

    FollowersStorer mockStorer = mock(FollowersStorer.class);
    new FollowersBatchFetcher(db, mockFriendsFollowers(), mockStorer).fetchFollowersBatch();
    verify(mockStorer, times(1)).storeFollowers(1L, "followers", new long[] {2L, 3L});

    FollowersTracker updatedTracker = db.findById(FollowersTracker.class, 1L);
    assertEquals(true, updatedTracker.shouldFetchFollowers());
    assertEquals(false, updatedTracker.followersFetched());
    assertEquals(1001L, updatedTracker.followersCursor());
  }

  @Test
  public void testAddsLevel2Trackers() throws TwitterException {
    FollowersTracker existingTracker1 = FollowersTracker.builder().id(1L)
        .shouldFetchFollowers(true).shouldFetchLevel2Followers(true).build();

    FollowersTracker existingTracker2 = FollowersTracker.builder().id(2L).build();

    db.saveAll(Arrays.asList(existingTracker1, existingTracker2));
    waitForEventualSave(FollowersTracker.class);

    // Add existing request that is marked as retrieved
    db.save(UserDetailRequestById.builder().id(2L).detailRetrieved(true).build());

    FollowersStorer mockStorer = mock(FollowersStorer.class);
    new FollowersBatchFetcher(db, mockFriendsFollowers(), mockStorer).fetchFollowersBatch();
    verify(mockStorer, times(1)).storeFollowers(1L, "followers", new long[] {2L, 3L});

    List<UserDetailRequestById> detailRequests = db.find(UserDetailRequestById.class, 3);
    assertEquals(2, detailRequests.size());
    detailRequests.sort((d1, d2) -> Long.compare(d1.id(), d2.id()));

    UserDetailRequestById detailRequest1 = detailRequests.get(0);
    assertEquals(2L, detailRequest1.id());
    // check that it kept the existing retrieved detail request for user 2 marked as retrieved
    assertTrue(detailRequest1.detailRetrieved());

    UserDetailRequestById detailRequest2 = detailRequests.get(1);
    assertEquals(3L, detailRequest2.id());
    // check that the new detail request for user 3 is marked as not retrieved
    assertFalse(detailRequest2.detailRetrieved());

    List<FollowersTracker> trackers = db.find(FollowersTracker.class, 4);
    assertEquals(3, trackers.size());
    trackers.sort((t1, t2) -> Long.compare(t1.id(), t2.id()));

    FollowersTracker tracker1 = trackers.get(0);
    assertEquals(1L, tracker1.id());
    // check that it stored the previous value
    assertTrue(tracker1.shouldFetchLevel2Followers());

    FollowersTracker tracker2 = trackers.get(1);
    assertEquals(2L, tracker2.id());
    assertTrue(tracker2.shouldFetchFollowers());

    FollowersTracker tracker3 = trackers.get(2);
    assertEquals(3L, tracker3.id());
    assertTrue(tracker3.shouldFetchFollowers());
    // check that it defaults level to retrieval fields to false
    assertFalse(tracker3.shouldFetchLevel2Followers());
  }

  private FriendsFollowersResources mockFriendsFollowers() throws TwitterException {
    FriendsFollowersResources friendsFollowers = mock(FriendsFollowersResources.class);

    IDs followerIds = mock(IDs.class);
    when(followerIds.getIDs()).thenReturn(new long[]{2L, 3L});
    when(followerIds.hasNext()).thenReturn(true);
    when(followerIds.getNextCursor()).thenReturn(1001L);

    when(friendsFollowers.getFollowersIDs(1L, -1L)).thenReturn(followerIds);

    return friendsFollowers;
  }
}
