package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/9/16.
 */
@AutoValue
public abstract class FollowersTracker implements Model {
  public abstract long id();
  public abstract boolean retrieveFollowers();
  public abstract boolean retrieveLevel2Followers();
  public abstract boolean followersRetrieved();
  public abstract boolean level2FollowersRetrieved();
  public abstract long followersCursor();

  public static Builder builder() {
    return new AutoValue_FollowersTracker.Builder()
        // Default cursor to -1 as that is the starting Twitter retrieval cursor.
        .followersCursor(-1L)
        .retrieveFollowers(false)
        .retrieveLevel2Followers(false)
        .followersRetrieved(false)
        .level2FollowersRetrieved(false);
  }

  public FollowersTracker withRetrieveFollowers(boolean value) {
    return toBuilder().retrieveFollowers(value).build();
  }

  public FollowersTracker withFollowersRetrieved(boolean value) {
    return toBuilder().followersRetrieved(value).build();
  }

  public FollowersTracker withFollowersCursor(long value) {
    return toBuilder().followersCursor(value).build();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder retrieveFollowers(boolean value);
    public abstract Builder retrieveLevel2Followers(boolean value);
    public abstract Builder followersRetrieved(boolean value);
    public abstract Builder level2FollowersRetrieved(boolean value);
    public abstract Builder followersCursor(long value); // = -1L;
    public abstract FollowersTracker build();
  }
}
