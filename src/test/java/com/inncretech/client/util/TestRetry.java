package com.inncretech.client.util;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;

/**
 * Shared retry helper for tests that race asynchronous backend provisioning (e.g. a table's
 * underlying storage isn't ready the instant its metadata is). Retries {@code assertion} until
 * it stops throwing or the timeout elapses, instead of failing on the first transient error.
 */
public final class TestRetry {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);

  private TestRetry() {}

  public static void untilSucceeds(ThrowingRunnable assertion) {
    untilSucceeds(DEFAULT_TIMEOUT, DEFAULT_POLL_INTERVAL, assertion);
  }

  public static void untilSucceeds(Duration timeout, Duration pollInterval, ThrowingRunnable assertion) {
    Awaitility.await()
        .atMost(timeout)
        .pollInterval(pollInterval)
        .ignoreExceptions()
        .untilAsserted(assertion);
  }
}
