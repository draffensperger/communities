package org.draff;

import com.google.api.services.datastore.client.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dave on 1/3/16.
 */
public class TestDatastore {
  private static Datastore datastore = null;

  public static Datastore get() {
    if (datastore != null) {
      return datastore;
    }

    setupDatastore();
    return datastore;
  }

  public static void clean() {
    new DatastoreCleaner(datastore).clean();
  }

  private static void setupDatastore() {
    try {
      DatastoreOptions options = DatastoreHelper.getOptionsFromEnv().build();
      if (options.getHost().startsWith("http://localhost")) {
        datastore = DatastoreFactory.get().create(options);
      } else {
        System.err.println("Tried to run tests on non-localhost datastore: " + options.getHost());
        System.exit(1);
      }
    } catch (GeneralSecurityException exception) {
      System.err.println("Security error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    } catch (IOException exception) {
      System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    }
  }
}
