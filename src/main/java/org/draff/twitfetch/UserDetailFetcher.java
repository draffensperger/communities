package org.draff.twitfetch;

/**
 * Created by dave on 1/13/16.
 */
public class UserDetailFetcher implements Runnable {
  private UserDetailBatchFetcher batchFetcher;

  // Twitter rate limits reset every 15 minutes
  private final long RATE_LIMIT_INTERVAL_MS = 15 * 60 * 1000;

  // With user authentication you can retrieve up to 180 user lookups per 15 minutes.
  // However, we will use a value of 178 to permit some user lookups for adding new followers goals.
  private final long BATCHES_PER_INTERVAL = 60;

  private final long MIN_MS_BETWEEN_BATCHES = RATE_LIMIT_INTERVAL_MS / BATCHES_PER_INTERVAL;

  // Assume tht the last batch just started so that we will wait the full minimum interval for the
  // initial retrieval. That could be helpful if the process started up quickly after have been
  // shut down.
  private long lastBatchStartedAt = System.currentTimeMillis();

  public UserDetailFetcher(UserDetailBatchFetcher batchFetcher) {
    this.batchFetcher = batchFetcher;
  }

  public void run() {
    try {
      while(true) {
        sleepUtilReadyForBatch();
        try {
          batchFetcher.fetchUserDetailsBatch();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    } catch(InterruptedException e) {}
  }

  private void sleepUtilReadyForBatch() throws InterruptedException {
    long msSinceLastBatch = System.currentTimeMillis() - lastBatchStartedAt;
    Thread.sleep(Math.max(0L, MIN_MS_BETWEEN_BATCHES - msSinceLastBatch));
    lastBatchStartedAt = System.currentTimeMillis();
  }
}
