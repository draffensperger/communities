package org.draff.twitfetch;

import com.google.common.hash.Hashing;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.inject.Inject;

/**
 * Created by dave on 7/8/16.
 */
public class FollowersStorer {
  private TextStorer textStorer;

  @Inject
  public FollowersStorer(TextStorer textStorer) {
    this.textStorer = textStorer;
  }

  public void storeFollowers(long userId, String relationshipType, long[] followerIds) {
    String content = longsContent(followerIds);

    byte[] hash = Hashing.sha1().hashString(content).asBytes();
    String filename = base64EncodedForPath(hash) + ".txt";

    String path = "twitter_data/" + userId + "/" + relationshipType + "/" + filename;

    textStorer.store(path, content);
  }

  private String longsContent(long[] ids) {
    StringBuilder builder = new StringBuilder();
    for (long id : ids) {
      builder.append(id);
      builder.append("\n");
    }
    return builder.toString();
  }

  private String base64EncodedForPath(byte[] bytes) {
    return Base64.encode(bytes).replace('/', '-').replace('+', '_');
  }
}
