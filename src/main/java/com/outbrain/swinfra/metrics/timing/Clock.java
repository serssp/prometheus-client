package com.outbrain.swinfra.metrics.timing;

public interface Clock {
  Clock DEFAULT_CLOCK = new SystemClock();

  long getTick();

  class SystemClock implements Clock {
    @Override
    public long getTick() {
      return System.nanoTime();
    }
  }
}
