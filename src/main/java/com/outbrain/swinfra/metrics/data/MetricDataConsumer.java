package com.outbrain.swinfra.metrics.data;

import com.outbrain.swinfra.metrics.Metric;

import java.util.List;

public interface MetricDataConsumer {

    void consumeCounter(Metric metric, List<String> labelValues, double value);

    void consumeGauge(Metric metric, List<String> labelValues, double value);

    void consumeSummary(Metric metric, List<String> labelValues, SummaryData data);

    void consumeHistogram(Metric metric, List<String> labelValues, HistogramData data);
}
