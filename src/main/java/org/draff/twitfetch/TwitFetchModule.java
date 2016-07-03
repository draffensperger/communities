package org.draff.twitfetch;

import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.draff.mapper.DbWithMappers;
import org.draff.objectdb.ObjectDb;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by dave on 2/6/16.
 */
public class TwitFetchModule extends AbstractModule {
  @Override
  protected void configure() {
    // interface to implementation
    //bind(TransactionLog.class).to(DatabaseTransactionLog.class);
  }

  @Provides
  ObjectDb provideObjectDb() {
    return DbWithMappers.create(datastoreFromEnv());
  }

  @Provides
  Twitter provideTwitter() {
    Config conf = ConfigFactory.load();

    System.setProperty("twitter4j.loggerFactory", "twitter4j.NullLoggerFactory");

    // This will get a Twitter instance with config parameters from environment variables.
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(conf.getBoolean("twitter_debug_enabled"))
        .setOAuthConsumerKey(conf.getString("twitter_consumer_key"))
        .setOAuthConsumerSecret(conf.getString("twitter_consumer_secret"))
        .setOAuthAccessToken(conf.getString("twitter_access_token"))
        .setOAuthAccessTokenSecret(conf.getString("twitter_access_token_secret"));
    return new TwitterFactory(cb.build()).getInstance();
  }

  private static Datastore datastoreFromEnv() {
    try {
      return DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv().build());
    } catch (GeneralSecurityException e) {
      throw new ProvisionException("Security issue connecting to datastore", e);
    } catch (IOException e) {
      throw new ProvisionException("Unable to connect to datastore", e);
    }
  }
}
