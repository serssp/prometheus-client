package com.outbrain.swinfra.metrics.timing;

import java.util.function.LongConsumer;

import static java.lang.Math.log;

public class Timer {

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
}
