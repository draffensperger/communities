package org.draff;


import twitter4j.*;

import java.util.*;

import org.apache.log4j.PropertyConfigurator;
import org.omg.CORBA.DATA_CONVERSION;

import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
  public static void main(String[] args) throws TwitterException {
    setupLogger();

    //testDatastore();
    retrieveFollowers();
  }

  private static void retrieveFollowers() throws TwitterException {
    Twitter twitter = new TwitterFactory().getInstance();
    Datastore datastore = getDatastore();
    FollowersRetriever retriever = new FollowersRetriever(twitter.friendsFollowers(), datastore);
    retriever.retrieveFollowers(341522259);
  }

  private static Datastore getDatastore() {
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

  private static void testDatastore() {
    Datastore datastore = getDatastore();
    try {
      BeginTransactionRequest.Builder treq = BeginTransactionRequest.newBuilder();
      BeginTransactionResponse tres = datastore.beginTransaction(treq.build());
      ByteString tx = tres.getTransaction();

      LookupRequest.Builder lreq = LookupRequest.newBuilder();
      Key.Builder key = Key.newBuilder().addPathElement(
          Key.PathElement.newBuilder()
          .setKind("Trivia")
          .setName("hgtg"));
      lreq.addKey(key);
      lreq.getReadOptionsBuilder().setTransaction(tx);
      LookupResponse lresp = datastore.lookup(lreq.build());

      CommitRequest.Builder creq = CommitRequest.newBuilder();
      creq.setTransaction(tx);
      Entity entity;
      if (lresp.getFoundCount() > 0) {
        entity = lresp.getFound(0).getEntity();
      } else {
        Entity.Builder entityBuilder = Entity.newBuilder();
        entityBuilder.setKey(key);
        entityBuilder.addProperty(Property.newBuilder()
            .setName("question")
            .setValue(Value.newBuilder().setStringValue("Meaning of Life?")));
        entityBuilder.addProperty(Property.newBuilder()
            .setName("answer")
            .setValue(Value.newBuilder().setIntegerValue(42)));
        entity = entityBuilder.build();
        creq.getMutationBuilder().addInsert(entity);
      }
      datastore.commit(creq.build());
      String question = entity.getProperty(0).getValue().getStringValue();
      Long answer = entity.getProperty(1).getValue().getIntegerValue();
      System.out.println(question);
      System.out.println(answer.toString());
    } catch (DatastoreException exception) {
      // Catch all Datastore rpc errors.
      System.err.println("Error while doing datastore operation");
      // Log the exception, the name of the method called and the error code.
      System.err.println(String.format("DatastoreException(%s): %s %s",
              exception.getMessage(),
              exception.getMethodName(),
              exception.getCode()));
      exception.printStackTrace();
      System.exit(1);
    }  }


  private static void setupLogger() {
    Properties props = new Properties();
    props.setProperty("log4j.rootLogger", "DEBUG, A1");
    props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
    PropertyConfigurator.configure(props);
  }
}
