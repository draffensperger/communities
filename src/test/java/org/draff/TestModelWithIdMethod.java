package org.draff;

/**
 * Created by dave on 1/2/16.
 */
public class TestModelWithIdMethod {
  String stringProp;
  long longProp;
  String id() {
    return stringProp + ":" + longProp;
  }
}
