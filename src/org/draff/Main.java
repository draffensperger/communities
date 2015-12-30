package org.draff;


import twitter4j.*;

import java.util.*;

import org.apache.log4j.PropertyConfigurator;

public class Main {
  public static void main(String[] args) throws TwitterException {
    setupLogger();

    Twitter twitter = new TwitterFactory().getInstance();

    FollowersRetriever retriever = new FollowersRetriever(twitter.friendsFollowers());
    retriever.retrieveFollowers("praffensperger");
  }

  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
