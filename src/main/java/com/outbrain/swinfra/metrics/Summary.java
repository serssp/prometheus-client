package com.outbrain.swinfra.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import com.outbrain.swinfra.metrics.MetricFamilySamples.Sample;
import com.outbrain.swinfra.metrics.repositories.ChildMetricRepo;
import com.outbrain.swinfra.metrics.repositories.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.repositories.MetricData;
import com.outbrain.swinfra.metrics.repositories.UnlabeledChildRepo;

import java.util.ArrayList;
import java.util.List;

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels;
import static com.outbrain.swinfra.metrics.MetricType.SUMMARY;

public class Summary extends AbstractMetric<Histogram> {

  private static final String QUANTILE_LABEL = "quantile";

  private Summary(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  public void observe(final int value, final String... labelValues) {
    metricForLabels(labelValues).update(value);
  }

  @Override
  ChildMetricRepo<Histogram> createChildMetricRepo() {
    if (labelNames.size() == 0) {
      return new UnlabeledChildRepo<>(new MetricData<>(createHistogram(), new String[]{}));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(createHistogram(), labelValues);
      });
    }
  }

  private Histogram createHistogram() {
    return new Histogram(new ExponentiallyDecayingReservoir());
  }

  @Override
  MetricType getType() {
    return SUMMARY;
  }

  @Override
  MetricFamilySamples toMetricFamilySamples(final MetricData<Histogram> metricData) {
    final List<Sample> samples = createQuantileSamples(metricData);
    return MetricFamilySamples.from(name, SUMMARY, help, samples);
  }

  private List<Sample> createQuantileSamples(final MetricData<Histogram> metricData) {
    final Snapshot snapshot = metricData.getMetric().getSnapshot();
    final List<String> labels = addToList(labelNames, QUANTILE_LABEL);
    final List<String> labelValues = metricData.getLabelValues();
    return Lists.newArrayList(
      Sample.from(name, labels, addToList(labelValues, "0.5"), snapshot.getMedian()),
      Sample.from(name, labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile()),
      Sample.from(name, labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile()),
      Sample.from(name, labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile()),
      Sample.from(name, labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile()),
      Sample.from(name, labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile())
    );
  }

  private <T> List<T> addToList(final List<T> source, final T element) {
    final List<T> result = new ArrayList<>(source);
    result.add(element);
    return result;
  }

  public static class SummaryBuilder extends AbstractMetricBuilder<Summary, SummaryBuilder> {

    SummaryBuilder(final String name, final String help) {
      super(name, help);
    }

    @Override
    protected Summary create(final String fullName, final String help, final String[] labelNames) {
      return new Summary(fullName, help, labelNames);
    }
  }
}
