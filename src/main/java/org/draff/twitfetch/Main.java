package org.draff.twitfetch;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by dave on 1/2/16.
 */
public class Main {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new TwitFetchModule());
    TwitterGraphFetcher fetcher = injector.getInstance(TwitterGraphFetcher.class);
    fetcher.runFetch();
  }
}
