package org.draff;

import org.draff.model.EmbeddedCommunity;
import org.draff.model.UserDetailRequestByName;
import org.draff.objectdb.ObjectDb;
import java.util.*;

/**
 * Created by dave on 1/21/16.
 */
public class EmbeddedCommunityDetailRequester {
  private static final int MAX_COMMUNITIES = 100;

  private ObjectDb db;
  public EmbeddedCommunityDetailRequester(ObjectDb db) {
    this.db = db;
  }

  public void requestCommunityUserDetails() {
    List<EmbeddedCommunity> communities = db.find(EmbeddedCommunity.class, MAX_COMMUNITIES);
    List<UserDetailRequestByName> requests = new ArrayList<>();
    communities.forEach(community -> {
      requests.add(new UserDetailRequestByName(community.embeddedScreenName));
      requests.add(new UserDetailRequestByName(community.parentScreenName));
    });
    db.saveAll(requests);
  }
}
