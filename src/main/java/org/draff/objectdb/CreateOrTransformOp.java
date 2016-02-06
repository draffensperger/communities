package org.draff.objectdb;

import com.google.auto.value.AutoValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dave on 2/2/16.
 */
@AutoValue
public abstract class CreateOrTransformOp<T extends Model> {
  public abstract DatastoreDb db();
  public abstract Class<T> modelClass();
  public abstract List<?> namesOrIds();
  public abstract ObjectTransformer<T> transformer();
  public abstract ObjectFromIdCreator<T> creator();

  CreateOrTransformOp() {}

  public static <T extends Model> Builder<T> builder(Class<T> modelClass) {
    return new AutoValue_CreateOrTransformOp.Builder<T>()
        .modelClass(modelClass);
  }

  @AutoValue.Builder
  public abstract static class Builder<T extends Model> {
    abstract Builder<T> modelClass(Class<T> value);
    public abstract Builder<T> db(DatastoreDb value);
    public abstract Builder<T> namesOrIds(List<?> value);
    public abstract Builder<T> transformer(ObjectTransformer<T> value);
    public abstract Builder<T> creator(ObjectFromIdCreator<T> value);

    public abstract CreateOrTransformOp<T> build();

    public void now() {
      build().execute();
    }
  }

  private void execute() {
    List<T> foundModels = db().findByNamesOrIds(modelClass(), namesOrIds());
    Map<Object, T> idsToFound = new HashMap<>(foundModels.size());
    foundModels.forEach(m -> idsToFound.put(db().mapper().getModelId(m), m));
    db().saveAll(
        namesOrIds().stream().map(id -> newOrUpdatedModel(id, idsToFound))
            .collect(Collectors.toList())
    );
  }

  private T newOrUpdatedModel(Object nameOrId, Map<Object, T> idsToFound) {
    T found = idsToFound.get(nameOrId);
    if (found == null) {
      return creator().generateFromId(nameOrId);
    } else {
      return transformer().transform(found);
    }
  }
}
