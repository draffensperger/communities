package org.draff.support;

import com.jayway.awaitility.Awaitility;

import org.draff.objectdb.DatastoreDb;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * Created by dave on 1/9/16.
 */
public class EventualConsistencyHelper {
  private static DatastoreDb db = new DatastoreDb(TestDatastore.get());

  public static void waitForEventualSave(Class clazz) {
    waitOnEventualConsistency(() -> db.findOne(clazz) != null);
  }

  public static void waitForEventualDelete(Class clazz) {
    waitOnEventualConsistency(() -> db.findOne(clazz) == null);
  }

  public static void waitOnEventualConsistency(Callable<Boolean> condition) {
    Awaitility.await().atMost(1, TimeUnit.SECONDS).until(condition);
  }
}
