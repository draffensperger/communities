package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/21/16.
 */

@AutoValue
public abstract class EmbeddedCommunity implements Model {
  public abstract String embeddedScreenName();
  public abstract String parentScreenName();

  EmbeddedCommunity() {}

  public static Builder builder() {
    return new AutoValue_EmbeddedCommunity.Builder();
  }

  public String id() {
    return embeddedScreenName().toLowerCase() + ":" + parentScreenName().toLowerCase();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder embeddedScreenName(String s);
    public abstract Builder parentScreenName(String s);
    public abstract EmbeddedCommunity build();
  }
}
