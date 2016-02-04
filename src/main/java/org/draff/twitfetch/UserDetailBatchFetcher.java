package org.draff.twitfetch;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;

import org.draff.model.UserDetail;
import org.draff.model.UserDetailRequestById;
import org.draff.model.UserDetailRequestByName;
import org.draff.objectdb.ObjectDb;

import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dave on 1/9/16.
 */
public class UserDetailBatchFetcher {
  private ObjectDb db;
  private UsersResources twitterUsers;
  private static final int BATCH_SIZE = 100;

  private final static Map<String, Object> DETAIL_NOT_RETRIEVED =
      new ImmutableMap.Builder<String, Object>().put("detailRetrieved", false).build();

  public UserDetailBatchFetcher(ObjectDb db, UsersResources users) {
    this.db = db;
    this.twitterUsers = users;
  }

  public void fetchUserDetailsBatch() throws TwitterException {
    String[] names = neededUserNamesBatch();
    if (names.length > 0) {
      fetchUsersByNames(names);
    } else {
      long[] ids = neededUserIdsBatch();
      if (ids.length > 0) {
        fetchUsersByIds(ids);
      }
    }
  }

  private void fetchUsersByNames(String[] names) throws TwitterException {
    System.out.println("Fetching user details for " + names.length + " user names.");
    saveUserDetails(twitterUsers.lookupUsers(names));

    db.createOrTransform(UserDetailRequestByName.class)
        .namesOrIds(Arrays.asList(names))
        .transformer(request -> ((UserDetailRequestByName)request).withDetailRetrieved(true))
        .creator(id -> UserDetailRequestByName.create((String)id, true))
        .now();
  }

  private void fetchUsersByIds(long[] ids) throws TwitterException {
    System.out.println("Fetching user details for " + ids.length + " user ids.");
    saveUserDetails(twitterUsers.lookupUsers(ids));

    db.createOrTransform(UserDetailRequestById.class)
        .namesOrIds(Longs.asList(ids))
        .transformer(request -> ((UserDetailRequestById)request).withDetailRetrieved(true))
        .creator(id -> UserDetailRequestById.create((Long)id, true))
        .now();
  }

  private String[] neededUserNamesBatch() {
    return db.find(UserDetailRequestByName.class, DETAIL_NOT_RETRIEVED, BATCH_SIZE)
        .stream().map(request -> request.id()).toArray(String[]::new);
  }

  private long[] neededUserIdsBatch() {
    List<Long> userIds = new ArrayList<>();

    Collection<Long> requestIds = requestIdsBatch(Long.MIN_VALUE);
    while (!requestIds.isEmpty() && userIds.size() < BATCH_SIZE) {
      fillUpToLimit(userIds, requestIds, BATCH_SIZE);
      if (userIds.size() < BATCH_SIZE) {
        requestIds = requestIdsBatch(Collections.max(userIds) + 1);
      }
    }

    return Longs.toArray(userIds);
  }

  private Collection<Long> requestIdsBatch(long minId) {
    List<Long> requestIdsList =
        db.findOrderedById(UserDetailRequestById.class, BATCH_SIZE, minId, DETAIL_NOT_RETRIEVED)
            .stream().map(request -> request.id()).collect(Collectors.toList());
    HashSet<Long> requestIds = new HashSet<>(requestIdsList);

    List<Long> existingIds = db.findByIds(UserDetail.class, requestIds).stream()
        .map(detail -> detail.id()).collect(Collectors.toList());

    // Since there are already UserDetail records for those request ids, mark those as retrieved.
    db.createOrTransform(UserDetailRequestById.class)
        .namesOrIds(existingIds)
        .transformer(request -> ((UserDetailRequestById)request).withDetailRetrieved(true))
        .creator(id -> UserDetailRequestById.create((Long)id, true))
        .now();

    requestIds.removeAll(existingIds);

    return requestIds;
  }

  private void fillUpToLimit(List<Long> dest, Collection<Long> source, int limit) {
    for (Long item : source) {
      if (dest.size() < limit) {
        dest.add(item);
      } else {
        return;
      }
    }
  }

  private void saveUserDetails(List<User> users) {
    db.saveAll(users.stream().map(u -> UserDetail.createFrom(u)).collect(Collectors.toList()));
  }
}
