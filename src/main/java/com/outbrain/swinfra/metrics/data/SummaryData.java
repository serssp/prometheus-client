package com.outbrain.swinfra.metrics.data;

public interface SummaryData {

    long getCount();

    double getSum();

    double getMedian();

    double get75thPercentile();

    double get95thPercentile();

    double get98thPercentile();

    double get99thPercentile();

    double get999thPercentile();
}
