package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/9/16.
 */
@AutoValue
public abstract class FollowersTracker implements Model {
  public abstract long id();
  public abstract boolean shouldFetchFollowers();
  public abstract boolean shouldFetchLevel2Followers();
  public abstract boolean followersFetched();
  public abstract boolean level2FollowersFetched();
  public abstract long followersCursor();

  FollowersTracker() {}

  public static Builder builder() {
    return new AutoValue_FollowersTracker.Builder()
        // Default cursor to -1 as that is the starting Twitter retrieval cursor.
        .followersCursor(-1L)
        .shouldFetchFollowers(false)
        .shouldFetchLevel2Followers(false)
        .followersFetched(false)
        .level2FollowersFetched(false);
  }

  public FollowersTracker withShouldFetchFollowers(boolean value) {
    return toBuilder().shouldFetchFollowers(value).build();
  }

  public FollowersTracker withFollowersFetched(boolean value) {
    return toBuilder().followersFetched(value).build();
  }

  public FollowersTracker withFollowersCursor(long value) {
    return toBuilder().followersCursor(value).build();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder shouldFetchFollowers(boolean value);
    public abstract Builder shouldFetchLevel2Followers(boolean value);
    public abstract Builder followersFetched(boolean value);
    public abstract Builder level2FollowersFetched(boolean value);
    public abstract Builder followersCursor(long value); // = -1L;
    public abstract FollowersTracker build();
  }
}
