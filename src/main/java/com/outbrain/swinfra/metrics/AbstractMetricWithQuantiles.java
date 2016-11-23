package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractMetricWithQuantiles<T extends Counting & Sampling & Metric> extends AbstractMetric<T> {

  private static final String QUANTILE_LABEL = "quantile";

  AbstractMetricWithQuantiles(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  List<Sample> createSamplesFromSnapshot(final T metric, final List<String> labelValues) {
    final Snapshot snapshot = metric.getSnapshot();

    final List<String> labels = addToList(getLabelNames(), QUANTILE_LABEL);

    long sum = 0;
    for (final long value : snapshot.getValues()) {
      sum += value;
    }

    return Lists.newArrayList(
        new Sample(getName(), labels, addToList(labelValues, "0.5"), snapshot.getMedian()),
        new Sample(getName(), labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile()),
        new Sample(getName(), labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile()),
        new Sample(getName(), labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile()),
        new Sample(getName(), labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile()),
        new Sample(getName(), labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile()),
        new Sample(getName() + "_count", getLabelNames(), labelValues, metric.getCount()),
        new Sample(getName() + "_sum", getLabelNames(), labelValues, sum)
    );
  }

  private <L> List<L> addToList(final List<L> source, final L element) {
    final List<L> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }
}
