package org.draff.twitfetch;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreOptions;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

/**
 * Created by dave on 2/6/16.
 */
public class TwitFetchModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  ObjectDb provideObjectDb(Config conf) {
    return DbWithMappers.create(datastoreFromConf(conf));
  }

  @Provides
  Config provideConfig() {
    String configFile = System.getProperty("config.file");
    if (configFile == null) {
      configFile = System.getenv("CONFIG_FILE");
    }
    if (configFile == null) {
      configFile = "application";
    }
    Config conf = ConfigFactory.load(configFile);
    return conf;
  }

  @Provides
  Twitter provideTwitter(Config conf) {
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

  private static Datastore datastoreFromConf(Config conf) {
    String keyStr = conf.getString("datastore_private_key_pkcs12_base64");
    String serviceAccount = conf.getString("datastore_service_account");
    Credential credential;
    if (serviceAccount.equals("")) {
      credential = null;
    } else {
      credential = credentialFromAccountAndKey(serviceAccount, keyStr);
    }

    DatastoreOptions options = new DatastoreOptions.Builder()
        .host(conf.getString("datastore_host"))
        .dataset(conf.getString("datastore_dataset"))
        .credential(credential)
        .build();
    return DatastoreFactory.get().create(options);
  }

  private static Credential credentialFromAccountAndKey(String serviceAccount, String keyStr) {
    PrivateKey key = keyFromString(keyStr);
    Credential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccount)
        .setServiceAccountPrivateKey(key)
        .setServiceAccountScopes(DatastoreOptions.SCOPES)
        .setTransport(newTrustedTransport())
        .setJsonFactory(new JacksonFactory())
        .build();
    return credential;
  }

  private static NetHttpTransport newTrustedTransport() {
    try {
      return GoogleNetHttpTransport.newTrustedTransport();
    } catch(GeneralSecurityException|IOException e) {
      throw new ProvisionException("Error provisioning datastore", e);
    }
  }

  private static PrivateKey keyFromString(String keyStr) {
    try {
      byte[] keyPKCS12Bytes = Base64.getDecoder().decode(keyStr);
      InputStream stream = new ByteArrayInputStream(keyPKCS12Bytes);

      KeyStore keystore = KeyStore.getInstance("PKCS12");
      char[] password = "notasecret".toCharArray();
      keystore.load(stream, password);
      return (PrivateKey) keystore.getKey("privatekey", password);
    } catch (NoSuchAlgorithmException|KeyStoreException|IOException|CertificateException|
        UnrecoverableKeyException e) {
      throw new ProvisionException("Cannot load datastore private key", e);
    }
  }
}
