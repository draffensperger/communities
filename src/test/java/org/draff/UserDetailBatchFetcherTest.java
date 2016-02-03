package org.draff;

import org.draff.mapper.DbWithMappers;
import org.draff.model.*;
import org.draff.objectdb.DatastoreDb;
import org.draff.support.TestDatastore;
import org.draff.twitfetch.UserDetailBatchFetcher;
import org.junit.Before;
import org.junit.Test;

import twitter4j.*;
import twitter4j.api.UsersResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.draff.support.EventualConsistencyHelper.waitForEventualSave;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

/**
 * Created by dave on 1/9/16.
 */
public class UserDetailBatchFetcherTest {
  private DatastoreDb db;

  @Before
  public void setup() {
    db = DbWithMappers.create(TestDatastore.get());
    TestDatastore.clean();
  }

  @Test
  public void testRetrieveUserDetailsById() throws TwitterException {
    db.saveAll(Arrays.asList(
        UserDetailRequestById.builder().id(10L).build(),
        UserDetailRequestById.builder().id(20L).build(),
        UserDetailRequestById.builder().id(30L).build()
    ));
    waitForEventualSave(UserDetailRequestById.class);

    UserDetail existingDetail = new UserDetail();
    existingDetail.id = 30;
    existingDetail.screenName = "user3";
    db.save(existingDetail);
    waitForEventualSave(UserDetail.class);

    new UserDetailBatchFetcher(db, mockUserResources()).fetchUserDetailsBatch();

    List<UserDetailRequestById> requests = db.find(UserDetailRequestById.class, 4);
    assertEquals(3, requests.size());
    requests.forEach(request -> assertTrue(request.detailRetrieved()));

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

  @Test
  public void testRetrieveUserDetailsByName() throws TwitterException {
    db.saveAll(Arrays.asList(
        new UserDetailRequestByName("user1"), new UserDetailRequestByName("user2")
    ));
    waitForEventualSave(UserDetailRequestByName.class);

    new UserDetailBatchFetcher(db, mockUserResources()).fetchUserDetailsBatch();
    waitForEventualSave(UserDetail.class);

    assertEquals("user1", db.findById(UserDetail.class, 10L).screenName);
    assertEquals("user2", db.findById(UserDetail.class, 20L).screenName);

    List<UserDetailRequestByName> requests = db.find(UserDetailRequestByName.class, 4);
    assertEquals(2, requests.size());
    requests.forEach(request -> assertTrue(request.detailRetrieved));
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
