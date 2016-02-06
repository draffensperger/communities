package org.draff.objectdb;

/**
 * Created by dave on 2/2/16.
 */
public class CreateOrTransformExecutor<T extends Model> {
  private CreateOrTransformOp<T> op;
  private DatastoreDb db;
  private Class<T> modelClass;

  public CreateOrTransformExecutor(Class<T> modelClass,
                                   CreateOrTransformOp<T> operation) {
    this.modelClass = modelClass;
    this.op = operation;
    this.db = op.db();
  }

}
