package com.outbrain.swinfra.metrics.data;

public interface HistogramData {

    long getCount();

    double getSum();

    void consumeBuckets(HistogramBucketsConsumer consumer);
}
