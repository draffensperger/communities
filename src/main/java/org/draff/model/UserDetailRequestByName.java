package org.draff.model;

import org.draff.objectdb.Model;

/**
 * Created by dave on 1/10/16.
 */
public class UserDetailRequestByName implements Model {
  public String id;
  public boolean detailRetrieved = false;

  public UserDetailRequestByName() {}
  public UserDetailRequestByName(String screenName) {
    this.id = screenName;
  }
}

