package org.draff.objectdb;

import com.google.auto.value.AutoValue;

import java.util.*;

/**
 * Created by dave on 2/2/16.
 */
@AutoValue
public abstract class CreateOrTransformOp {
  public abstract DatastoreDb db();
  public abstract Class modelClass();
  public abstract List<?> namesOrIds();
  public abstract ObjectTransformer transformer();
  public abstract ObjectFromIdCreator creator();

  CreateOrTransformOp() {}

  public static Builder builder() {
    return new AutoValue_CreateOrTransformOp.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder modelClass(Class value);
    public abstract Builder db(DatastoreDb value);
    public abstract Builder namesOrIds(List<?> value);
    public abstract Builder transformer(ObjectTransformer value);
    public abstract Builder creator(ObjectFromIdCreator value);

    public abstract CreateOrTransformOp build();

    public void now() {
      new CreateOrTransformExecutor(build()).execute();
    }
  }
}
