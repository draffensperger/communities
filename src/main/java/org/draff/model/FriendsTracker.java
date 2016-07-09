package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/9/16.
 */
@AutoValue
public abstract class FriendsTracker implements Model {
  public abstract long id();
  public abstract boolean friendsFetched();
  public abstract boolean shouldFetchFriends();
  public abstract long friendsCursor();

  FriendsTracker() {}

  public static Builder builder() {
    return new AutoValue_FriendsTracker.Builder()
        // Default cursor to -1 as that is the starting Twitter retrieval cursor.
        .friendsCursor(-1L)
        .shouldFetchFriends(false)
        .friendsFetched(false);
  }

  public FriendsTracker withShouldFetchFriends(boolean value) {
    return toBuilder().shouldFetchFriends(value).build();
  }

  public FriendsTracker withFriendsFetched(boolean value) {
    return toBuilder().friendsFetched(value).build();
  }

  public FriendsTracker withFriendsCursor(long value) {
    return toBuilder().friendsCursor(value).build();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder shouldFetchFriends(boolean value);
    public abstract Builder friendsFetched(boolean value);
    public abstract Builder friendsCursor(long value); // = -1L;
    public abstract FriendsTracker build();
  }
}
