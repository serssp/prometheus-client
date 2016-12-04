package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.samples.SampleCreator;
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
   *  @param metricData the metric from which to extract the samples
   * @param measurementFactor a factor to apply on each sample's value (except for <i>_count</i>)
   * @param sampleCreator a {@link SampleCreator} with which to create the samples
   */
  List<Sample> createSamplesFromSnapshot(final MetricData<T> metricData,
                                         final double measurementFactor,
                                         final SampleCreator sampleCreator) {
    final Snapshot snapshot = metricData.getMetric().getSnapshot();
    final List<String> labelValues = metricData.getLabelValues();

    final List<String> labels = addToList(getLabelNames(), QUANTILE_LABEL);

    long sum = 0;
    for (final long value : snapshot.getValues()) {
      sum += value;
    }

    return Arrays.asList(
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.5"), snapshot.getMedian() * measurementFactor),
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile() * measurementFactor),
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile() * measurementFactor),
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile() * measurementFactor),
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile() * measurementFactor),
        sampleCreator.createSample(getName(), labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile() * measurementFactor),
        sampleCreator.createSample(getName() + "_count", getLabelNames(), labelValues, metricData.getMetric().getCount()),
        sampleCreator.createSample(getName() + "_sum", getLabelNames(), labelValues, sum  * measurementFactor)
    );
  }

  private <L> List<L> addToList(final List<L> source, final L element) {
    final List<L> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }
}
