package org.draff;

import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * Created by dave on 1/2/16.
 */
public class Main {
  public static void main(String[] args) {
    setupLogger();
    try {
      TwitterGraphFetcher.configureFromEnv().runFetch();
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

  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
