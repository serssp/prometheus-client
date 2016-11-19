package com.outbrain.swinfra.metrics;


import com.outbrain.swinfra.metrics.MetricFamilySamples.Sample;
import com.outbrain.swinfra.metrics.repositories.ChildMetricRepo;
import com.outbrain.swinfra.metrics.repositories.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.repositories.MetricData;
import com.outbrain.swinfra.metrics.repositories.UnlabeledChildRepo;

import java.util.List;

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels;
import static com.outbrain.swinfra.metrics.MetricType.COUNTER;
import static java.util.Collections.singletonList;

public class Counter extends AbstractMetric<com.codahale.metrics.Counter> {

  private Counter(final String name, final String help, final String[] labelNames) {
    super(name, help, labelNames);
  }

  public void inc(final String... labelValues) {
    validateLabelValues(labelValues);
    inc(1, labelValues);
  }

  public void inc(final long n, final String... labelValues) {
    validateLabelValues(labelValues);
    metricForLabels(labelValues).inc(n);
  }

  public long getValue(final String... labelValues) {
    return metricForLabels(labelValues).getCount();
  }

  @Override
  ChildMetricRepo<com.codahale.metrics.Counter> createChildMetricRepo() {
    if (getLabelNames().size() == 0) {
      return new UnlabeledChildRepo<>(new MetricData<>(new com.codahale.metrics.Counter(), new String[]{}));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(new com.codahale.metrics.Counter(), labelValues);
      });
    }
  }

  @Override
  MetricType getType() {
    return COUNTER;
  }

  @Override
  MetricFamilySamples toMetricFamilySamples(final MetricData<com.codahale.metrics.Counter> metricData) {
    final List<Sample> samples = singletonList(
        Sample.from(getName(), getLabelNames(), metricData.getLabelValues(), metricData.getMetric().getCount()));
    return MetricFamilySamples.from(getName(), COUNTER, getHelp(), samples);
  }

  public static class CounterBuilder extends AbstractMetricBuilder<Counter, CounterBuilder> {

    CounterBuilder(final String name, final String help) {
      super(name, help);
    }

    @Override
    protected Counter create(final String fullName, final String help, final String[] labelNames) {
      return new Counter(fullName, help, labelNames);
    }
  }

}
