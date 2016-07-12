package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 7/11/16.
 */
@AutoValue
public abstract class TwitterAccessToken implements Model {
  // id for the user whose token this is
  public abstract long id();

  public abstract String screenName();
  public abstract String token();
  public abstract String tokenSecret();

  public static Builder builder() {
    return new AutoValue_TwitterAccessToken.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder screenName(String value);
    public abstract Builder token(String value);
    public abstract Builder tokenSecret(String value);
    public abstract TwitterAccessToken build();
  }
}
