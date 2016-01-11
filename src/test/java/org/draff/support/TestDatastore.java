package org.draff.support;

import com.google.api.services.datastore.DatastoreV1.CommitRequest;
import com.google.api.services.datastore.DatastoreV1.EntityResult;
import com.google.api.services.datastore.DatastoreV1.Mutation;
import com.google.api.services.datastore.DatastoreV1.Query;
import com.google.api.services.datastore.DatastoreV1.RunQueryRequest;
import com.google.api.services.datastore.DatastoreV1.RunQueryResponse;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.api.services.datastore.client.DatastoreOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
/**
 * Created by dave on 1/3/16.
 */
public class TestDatastore {
  private static Datastore datastore = null;
  private static int CLEAN_BATCH_SIZE = 100;

  public static Datastore get() {
    if (datastore != null) {
      return datastore;
    }

    setupDatastore();
    return datastore;
  }

  public static void clean() {
    List<EntityResult> results = findBatch();
    while(!results.isEmpty()) {
      Mutation.Builder mutation = Mutation.newBuilder();
      results.forEach(r -> mutation.addDelete(r.getEntity().getKey()));
      saveMutation(mutation);
      results = findBatch();
    }
  }

  private static List<EntityResult> findBatch() {
    Query.Builder q = Query.newBuilder();
    q.setLimit(CLEAN_BATCH_SIZE);
    RunQueryRequest request = RunQueryRequest.newBuilder().setQuery(q.build()).build();
    try {
      RunQueryResponse response = datastore.runQuery(request);
      return response.getBatch().getEntityResultList();
    } catch(DatastoreException e) {
      throw new RuntimeException(e);
    }
  }

  public static void saveMutation(Mutation.Builder mutation) {
    CommitRequest request = CommitRequest.newBuilder()
        .setMode(CommitRequest.Mode.NON_TRANSACTIONAL)
        .setMutation(mutation)
        .build();

    try {
      datastore.commit(request);
    } catch (DatastoreException e) {
      e.printStackTrace();
    }
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
