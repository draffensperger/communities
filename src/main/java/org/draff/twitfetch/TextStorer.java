package org.draff.twitfetch;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.google.inject.name.Named;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

/**
 * Created by dave on 7/7/16.
 */
public class TextStorer {
  private Storage storage;
  private String bucket;

  @Inject
  public TextStorer(Storage storage, @Named("storage_bucket") String bucket) {
    this.storage = storage;
    this.bucket = bucket;
  }

  public void store(String path, String content) {
    StorageObject metadata = new StorageObject().setName(path);
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    ByteArrayContent contentBytes = new ByteArrayContent("text/plain", bytes);
    try {
      storage.objects().insert(bucket, metadata, contentBytes).execute();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
