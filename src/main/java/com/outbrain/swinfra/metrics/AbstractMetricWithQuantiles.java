package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AbstractMetricWithQuantiles<T extends Counting & Sampling & Metric> extends AbstractMetric<T> {

  private static final String QUANTILE_LABEL = "quantile";

  AbstractMetricWithQuantiles(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  /**
   * Extracts prometheus samples from a Codahale {@link Snapshot} object. The snapshot object contains all measurements
   * for a Codahale {@link Sampling} class.
   *
   * @param metric the metric from which to extract the samples
   * @param labelValues label values to add to all the samples
   * @param measurementFactor a factor to apply on each sample's value (except for <i>_count</i>)
   */
  List<Sample> createSamplesFromSnapshot(final T metric, final List<String> labelValues, final double measurementFactor) {
    final Snapshot snapshot = metric.getSnapshot();

    final List<String> labels = addToList(getLabelNames(), QUANTILE_LABEL);

    long sum = 0;
    for (final long value : snapshot.getValues()) {
      sum += value;
    }

    return Arrays.asList(
        new Sample(getName(), labels, addToList(labelValues, "0.5"), snapshot.getMedian() * measurementFactor),
        new Sample(getName(), labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile() * measurementFactor),
        new Sample(getName(), labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile() * measurementFactor),
        new Sample(getName(), labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile() * measurementFactor),
        new Sample(getName(), labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile() * measurementFactor),
        new Sample(getName(), labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile() * measurementFactor),
        new Sample(getName() + "_count", getLabelNames(), labelValues, metric.getCount()),
        new Sample(getName() + "_sum", getLabelNames(), labelValues, sum  * measurementFactor)
    );
  }

  private <L> List<L> addToList(final List<L> source, final L element) {
    final List<L> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }
}
