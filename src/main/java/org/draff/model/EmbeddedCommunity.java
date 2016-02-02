package org.draff.model;

import org.draff.objectdb.*;

/**
 * Created by dave on 1/21/16.
 */
public class EmbeddedCommunity implements Model {
  public String embeddedScreenName;
  public String parentScreenName;

  public EmbeddedCommunity() {}

  public EmbeddedCommunity(String embeddedScreenName, String parentScreenName) {
    this.embeddedScreenName = embeddedScreenName;
    this.parentScreenName = parentScreenName;
  }

  public String id() {
    return embeddedScreenName.toLowerCase() + ":" + parentScreenName.toLowerCase();
  }
}
