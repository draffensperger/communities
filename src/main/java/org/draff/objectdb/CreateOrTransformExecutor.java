package org.draff.objectdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dave on 2/2/16.
 */
public class CreateOrTransformExecutor {
  private CreateOrTransformOp op;
  private DatastoreDb db;

  public CreateOrTransformExecutor(CreateOrTransformOp operation) {
    this.op = operation;
    this.db = op.db();
  }

  public void execute() {
    List<Model> foundModels = db.findByNamesOrIds(op.modelClass(), op.namesOrIds());
    Map<Object, Model> idsToFound = new HashMap<>(foundModels.size());
    foundModels.forEach(m -> idsToFound.put(db.mapper().getModelId(m), m));

    db.saveAll(
        op.namesOrIds().stream().map(id -> newOrUpdatedModel(id, idsToFound))
            .collect(Collectors.toList())
    );
  }

  private Model newOrUpdatedModel(Object nameOrId, Map<Object, Model> idsToFound) {
    Model found = idsToFound.get(nameOrId);
    if (found == null) {
      return op.creator().generateFromId(nameOrId);
    } else {
      return op.transformer().transform(found);
    }
  }
}
