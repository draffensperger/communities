package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/10/16.
 */
@AutoValue
public abstract class UserDetailRequestByName implements Model {
  public abstract String id();
  public abstract boolean detailRetrieved();

  UserDetailRequestByName() {}

  public static UserDetailRequestByName create(String id, boolean detailRetrieved) {
    return builder().id(id).detailRetrieved(detailRetrieved).build();
  }

  public static UserDetailRequestByName create(String id) {
    return create(id, false);
  }

  public static Builder builder() {
    return new AutoValue_UserDetailRequestByName.Builder()
        .detailRetrieved(false);
  }

  public abstract Builder toBuilder();

  public UserDetailRequestByName withDetailRetrieved(boolean value) {
    return toBuilder().detailRetrieved(true).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(String value);
    public abstract Builder detailRetrieved(boolean value);
    public abstract UserDetailRequestByName build();
  }
}

