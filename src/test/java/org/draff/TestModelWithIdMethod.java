package org.draff;

/**
 * Created by dave on 1/3/16.
 */
public class TestModelWithIdMethod {
  String stringProp;
  long longProp;

  public TestModelWithIdMethod() {}

  String id() {
    return stringProp + ":" + longProp;
  }
}

