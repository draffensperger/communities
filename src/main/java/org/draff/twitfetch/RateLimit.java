package org.draff.twitfetch;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dave on 1/18/16.
 */
public class RateLimit {
  private static final Logger log = Logger.getLogger(TwitterGraphFetcher.class.getName());

  private static final long RATE_LIMIT_MARGIN = 5 * 1000;

  private int remaining;
  private long timeOfNextReset;
  private Twitter twitter;
  private String rateLimitId;

  public RateLimit(Twitter twitter, String rateLimitId) {
    this.twitter = twitter;
    this.rateLimitId = rateLimitId;
  }

  public boolean hasRemaining() {
    if (remaining == 0) {
      retrieveRateLimitInfo();
    }
    log.finest("Remaining for " + rateLimitId + " " + remaining);
    return remaining > 0;
  }

  public void decrement() {
    if (hasRemaining()) {
      remaining--;
      log.finest("Remaining for " + rateLimitId + " decremented to " + remaining);
    } else {
      throw new IllegalStateException("Cannot have performed task as rate limit would be hit");
    }
  }

  public long timeUntilNextReset() {
    return Math.max(0L, timeOfNextReset - System.currentTimeMillis()) + RATE_LIMIT_MARGIN;
  }

  private void retrieveRateLimitInfo() {
    try {
      Map<String, RateLimitStatus> statusMap = twitter.getRateLimitStatus();
      RateLimitStatus status = statusMap.get(rateLimitId);
      this.remaining = status.getRemaining();
      this.timeOfNextReset = System.currentTimeMillis() + status.getSecondsUntilReset() * 1000L;
      log.finest("Remaining reset to " + remaining);
    } catch(TwitterException e) {
      log.log(Level.SEVERE, "Error getting rate limit", e);
      return;
    }
  }
}
