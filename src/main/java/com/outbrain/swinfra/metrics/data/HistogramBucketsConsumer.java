package com.outbrain.swinfra.metrics.data;

@FunctionalInterface
public interface HistogramBucketsConsumer {

    void apply(double upperBound, long count);
}
