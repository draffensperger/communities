package org.draff;

import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;

import org.apache.log4j.PropertyConfigurator;
import org.draff.objectdb.DatastoreDb;
import org.draff.objectdb.ObjectDb;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * Created by dave on 1/2/16.
 */
public class Main {
  public static void main(String[] args) {
    setupLogger();
    //GetFollowersWorker worker = new GetFollowersWorker(objectDb(), twitter());
    //worker.run();
  }

  private static ObjectDb objectDb() {
    return new DatastoreDb(datastore());
  }

  private static Datastore datastore() {
    Datastore datastore = null;
    try {
      datastore = DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv().build());
    } catch (GeneralSecurityException exception) {
      System.err.println("Security error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    } catch (IOException exception) {
      System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    }
    return datastore;
  }

  private static Twitter twitter() {
    return new TwitterFactory().getInstance();
  }

  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
