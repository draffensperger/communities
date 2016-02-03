package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/10/16.
 */
@AutoValue
public abstract class UserDetailRequestById implements Model {
  public abstract long id();
  public abstract boolean detailRetrieved();

  UserDetailRequestById() {}

  public static Builder builder() {
    return new AutoValue_UserDetailRequestById.Builder()
        .detailRetrieved(false);
  }

  public abstract Builder toBuilder();

  public UserDetailRequestById withDetailRetrieved(boolean value) {
    return toBuilder().detailRetrieved(true).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder detailRetrieved(boolean value);
    public abstract UserDetailRequestById build();
  }
}
