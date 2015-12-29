package org.draff;

/*

https://google.github.io/styleguide/javaguide.html#s4.1-braces

Basic code:

- q
Queue:ction PreviousTab

https://cloud.google.com/pubsub/overview

Datastore
https://cloud.google.com/datastore/docs/concepts/overview

Cloud Pub/Sub


Google App Engine back-end node

Premptable VM

https://cloud.google.com/container-engine/


Docker Hub for this thing.

Google container service with a premptable VM

maybe use a premptable vm?


Research question:
- To what extent are people in Cru a part of other non-Cru communities?
- Someone is in Cru if they follow the Cru user

Phase 1:
- lookup everyone in that Cru group and lookup all of their followers
- question: what percentage of their followers / friends are also in Cru?
-



https://api.twitter.com/1.1/followers/ids.json
https://dev.twitter.com/rest/reference/get/followers/ids
https://api.twitter.com/1.1/friends/ids.json


tweets to #cruboston

northeastern
https://twitter.com/nuagape
https://twitter.com/Northeastern

https://twitter.com/cru_unh
https://twitter.com/UofNH

https://twitter.com/cruatuconn


https://twitter.com/pennstatecru
https://twitter.com/cruatunc
https://twitter.com/cruatucf
https://twitter.com/cruatnu
https://twitter.com/louisvillecru
https://twitter.com/webstercru
https://twitter.com/keancru
https://twitter.com/msureallife
https://twitter.com/crumsu
https://twitter.com/cruatksu
https://twitter.com/ballstatecru
https://twitter.com/txstatecru
https://twitter.com/gsu_cru
https://twitter.com/appstatecru
https://twitter.com/zzucru
https://twitter.com/fsucru


https://twitter.com/crutweets

https://twitter.com/hashtag/Cru15?src=hash
https://twitter.com/hashtag/cruboston?src=hash


 */

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
