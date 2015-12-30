package org.draff;


import twitter4j.*;

import java.util.*;

import org.apache.log4j.PropertyConfigurator;

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

    testDatastore("twitter-communities");
    //retrieveFollowers();
  }

  private static void retrieveFollowers() throws TwitterException {
    Twitter twitter = new TwitterFactory().getInstance();
    FollowersRetriever retriever = new FollowersRetriever(twitter.friendsFollowers());
    retriever.retrieveFollowers("praffensperger");
  }

  private static void testDatastore(String datasetId) {
    Datastore datastore = null;
    try {
      // Setup the connection to Google Cloud Datastore and infer credentials
      // from the environment.
      datastore = DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv()
          .dataset(datasetId).build());
    } catch (GeneralSecurityException exception) {
      System.err.println("Security error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    } catch (IOException exception) {
      System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
      exception.printStackTrace();
      System.exit(1);
    }

    try {
      // Create an RPC request to begin a new transaction.
      BeginTransactionRequest.Builder treq = BeginTransactionRequest.newBuilder();
      // Execute the RPC synchronously.
      BeginTransactionResponse tres = datastore.beginTransaction(treq.build());
      // Get the transaction handle from the response.
      ByteString tx = tres.getTransaction();

      // Create an RPC request to get entities by key.
      LookupRequest.Builder lreq = LookupRequest.newBuilder();
      // Set the entity key with only one `path_element`: no parent.
      Key.Builder key = Key.newBuilder().addPathElement(
          Key.PathElement.newBuilder()
          .setKind("Trivia")
          .setName("hgtg"));
      // Add one key to the lookup request.
      lreq.addKey(key);
      // Set the transaction, so we get a consistent snapshot of the
      // entity at the time the transaction started.
      lreq.getReadOptionsBuilder().setTransaction(tx);
      // Execute the RPC and get the response.
      LookupResponse lresp = datastore.lookup(lreq.build());
      // Create an RPC request to commit the transaction.
      CommitRequest.Builder creq = CommitRequest.newBuilder();
      // Set the transaction to commit.
      creq.setTransaction(tx);
      Entity entity;
      if (lresp.getFoundCount() > 0) {
        entity = lresp.getFound(0).getEntity();
      } else {
        // If no entity was found, create a new one.
        Entity.Builder entityBuilder = Entity.newBuilder();
        // Set the entity key.
        entityBuilder.setKey(key);
        // Add two entity properties:
        // - a utf-8 string: `question`
        entityBuilder.addProperty(Property.newBuilder()
            .setName("question")
            .setValue(Value.newBuilder().setStringValue("Meaning of Life?")));
        // - a 64bit integer: `answer`
        entityBuilder.addProperty(Property.newBuilder()
            .setName("answer")
            .setValue(Value.newBuilder().setIntegerValue(42)));
        // Build the entity.
        entity = entityBuilder.build();
        // Insert the entity in the commit request mutation.
        creq.getMutationBuilder().addInsert(entity);
      }
      // Execute the Commit RPC synchronously and ignore the response.
      // Apply the insert mutation if the entity was not found and close
      // the transaction.
      datastore.commit(creq.build());
      // Get `question` property value.
      String question = entity.getProperty(0).getValue().getStringValue();
      // Get `answer` property value.
      Long answer = entity.getProperty(1).getValue().getIntegerValue();
      System.out.println(question);
      //String result = System.console().readLine("> ");
      String result = "hi";
      if (result.equals(answer.toString())) {
        System.out.println("fascinating, extraordinary and, " +
            "when you think hard about it, completely obvious.");
      } else {
        System.out.println("Don't Panic!");
      }
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
