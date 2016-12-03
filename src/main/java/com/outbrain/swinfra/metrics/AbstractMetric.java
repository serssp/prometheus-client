package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Metric;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractMetric<T extends Metric> {

  private final String name;
  private final String help;
  private final List<String> labelNames;
  private ChildMetricRepo<T> childMetricRepo;

  AbstractMetric(final String name,
                 final String help,
                 final String[] labelNames) {
    this.name = name;
    this.help = help;
    this.labelNames = Arrays.asList(labelNames);
  }

  abstract ChildMetricRepo<T> createChildMetricRepo();

  abstract Collector.Type getType();

  abstract List<Sample> createSamples(String metricName, MetricData<T> metricData);

  String getName() {
    return name;
  }

  List<String> getLabelNames() {
    return labelNames;
  }

  private MetricFamilySamples toMetricFamilySamples(final MetricData<T> metricData) {
    return new MetricFamilySamples(name, getType(), help, createSamples(name, metricData));
  }

  void initChildMetricRepo() {
    this.childMetricRepo = createChildMetricRepo();
  }

  void validateLabelValues(final String... labelValues) {
    if (labelNames.size() > 0) {
      Validate.isTrue(labelNames.size() == labelValues.length, "A label value must be supplied for each label name");
    }

    for (final String labelName : labelNames) {
      Validate.notBlank(labelName, "Label names must contain text");
    }
  }

  T metricForLabels(final String... labelValues) {
    return childMetricRepo.metricForLabels(labelValues).getMetric();
  }

  List<MetricFamilySamples> getSamples() {
    return childMetricRepo.all()
                          .stream()
                          .map(this::toMetricFamilySamples)
                          .collect(Collectors.toList());
  }

}
