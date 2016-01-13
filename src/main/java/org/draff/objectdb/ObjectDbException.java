package org.draff.objectdb;

/**
 * Created by dave on 1/12/16.
 */
public class ObjectDbException extends RuntimeException {
  public ObjectDbException(Exception cause) {
    super(cause);
  }

  public ObjectDbException(String message) {
    super(message);
  }
}
