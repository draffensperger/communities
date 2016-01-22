package org.draff;

import org.draff.objectdb.*;

/**
 * Created by dave on 1/21/16.
 */
public class EmbeddedCommunity implements Model {
  String embeddedScreenName;
  String parentScreenName;

  public EmbeddedCommunity() {}

  public EmbeddedCommunity(String embeddedScreenName, String parentScreenName) {
    this.embeddedScreenName = embeddedScreenName;
    this.parentScreenName = parentScreenName;
  }

  String id() {
    return embeddedScreenName.toLowerCase() + ":" + parentScreenName.toLowerCase();
  }
}
