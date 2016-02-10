package org.draff.twitfetch;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Created by dave on 1/2/16.
 */
public class Main {
  public static void main(String[] args) {
//    Datastore datastore = datastoreFromEnv();
//    ObjectDb db = new DatastoreDb(datastore);
//
//    setupLogger();
//    TwitterGraphFetcher fetcher = new TwitterGraphFetcher(db, twitterFromEnv());
//    fetcher.runFetch();

    Injector injector = Guice.createInjector(new TwitFetchModule());
    TwitterGraphFetcher fetcher = injector.getInstance(TwitterGraphFetcher.class);
    fetcher.runFetch();
  }

  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
