package com.outbrain.swinfra.metrics.timing;

import java.util.function.LongConsumer;

/**
 * The timer class is used to report intervals.
 * <p>
 * When created the timer acquires its start tick from the given clock. When stopped another tick is acquired
 * and the difference between those two is provided to the given consumer.
 * </p>
 * <p>
 * It's important to note that the timer is a single-use object, its stop method should not be called more than once.
 * </p>
 */
public class Timer implements AutoCloseable {

  private final Clock clock;
  private final long startTime;
  private final LongConsumer elapsedIntervalConsumer;

  public Timer(final Clock clock, final LongConsumer elapsedIntervalConsumer) {
    this.clock = clock;
    this.elapsedIntervalConsumer = elapsedIntervalConsumer;
    this.startTime = clock.getTick();
  }

  public void stop() {
    final long elapsed = clock.getTick() - startTime;
    elapsedIntervalConsumer.accept(elapsed);
  }

  @Override
  public void close() throws Exception {
    stop();
  }
}
