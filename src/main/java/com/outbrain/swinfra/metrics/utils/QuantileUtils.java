package com.outbrain.swinfra.metrics.utils;

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

public class QuantileUtils {

  private static final String QUANTILE_LABEL = "quantile";

  public static <T extends Counting & Sampling & Metric> List<Sample> createSamplesFromSnapshot(final MetricData<T> metricData,
                                                                                                final String name,
                                                                                                final List<String> labelNames,
                                                                                                final double measurementFactor,
                                                                                                final SampleCreator sampleCreator) {
    final Snapshot snapshot = metricData.getMetric().getSnapshot();
    final List<String> labelValues = metricData.getLabelValues();

    final List<String> labels = addToList(labelNames, QUANTILE_LABEL);

    long sum = 0;
    for (final long value : snapshot.getValues()) {
      sum += value;
    }

    return Arrays.asList(
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.5"), snapshot.getMedian() * measurementFactor),
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile() * measurementFactor),
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile() * measurementFactor),
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile() * measurementFactor),
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile() * measurementFactor),
      sampleCreator.createSample(name, labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile() * measurementFactor),
      sampleCreator.createSample(name + "_count", labelNames, labelValues, metricData.getMetric().getCount()),
      sampleCreator.createSample(name + "_sum", labelNames, labelValues, sum * measurementFactor)
    );
  }

  private static <L> List<L> addToList(final List<L> source, final L element) {
    final List<L> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }

}
