package org.draff;

import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by dave on 1/2/16.
 */
public class Main {
  public static void main(String[] args) {
    setupLogger();
  }

  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
