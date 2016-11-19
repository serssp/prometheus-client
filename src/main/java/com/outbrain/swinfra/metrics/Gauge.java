package com.outbrain.swinfra.metrics;

import com.codahale.metrics.CachedGauge;
import com.outbrain.swinfra.metrics.MetricFamilySamples.Sample;
import com.outbrain.swinfra.metrics.repositories.ChildMetricRepo;
import com.outbrain.swinfra.metrics.repositories.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.repositories.MetricData;
import com.outbrain.swinfra.metrics.repositories.UnlabeledChildRepo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels;
import static com.outbrain.swinfra.metrics.LabelUtils.labelsToCommaDelimitedString;
import static com.outbrain.swinfra.metrics.MetricType.GAUGE;
import static java.util.Collections.singletonList;

//todo document the relation between value suppliers and label values
public class Gauge extends AbstractMetric<CachedGauge<Double>> {

  private final Map<String, MetricData<CachedGauge<Double>>> valueSuppliers;

  private Gauge(final String name,
                final String help,
                final String[] labelNames,
                final Map<String[], DoubleSupplier> valueSuppliers) {
    super(name, help, labelNames);
    this.valueSuppliers = convertToMetricData(valueSuppliers);
  }

  private CachedGauge<Double> createGauge(final DoubleSupplier valueSupplier) {
    return new CachedGauge<Double>(10, TimeUnit.SECONDS) {
      @Override
      protected Double loadValue() {
        return valueSupplier.getAsDouble();
      }
    };
  }

  @Override
  MetricFamilySamples toMetricFamilySamples(final MetricData<CachedGauge<Double>> metricData) {
    final List<Sample> samples = singletonList(
        Sample.from(getName(), getLabelNames(), metricData.getLabelValues(), metricData.getMetric().getValue()));
    return MetricFamilySamples.from(getName(), getType(), getHelp(), samples);
  }

  @Override
  ChildMetricRepo<CachedGauge<Double>> createChildMetricRepo() {
    if (valueSuppliers.size() == 1) {
      final CachedGauge<Double> gauge = valueSuppliers.values().iterator().next().getMetric();
      return new UnlabeledChildRepo<>(new MetricData<>(gauge, new String[]{}));
    } else {
      final ChildMetricRepo<CachedGauge<Double>> result = new LabeledChildrenRepo<>(valueSuppliers::get);
      valueSuppliers.keySet().forEach(metricLabels -> {
        final String[] labelValues = commaDelimitedStringToLabels(metricLabels);
        result.metricForLabels(labelValues);
      });
      return result;
    }
  }

  private Map<String, MetricData<CachedGauge<Double>>> convertToMetricData(final Map<String[], DoubleSupplier> valueSuppliers) {
    final Map<String, MetricData<CachedGauge<Double>>> metricData = new HashMap<>(valueSuppliers.size());
    valueSuppliers.entrySet()
                  .forEach(entry -> metricData.put(
                      labelsToCommaDelimitedString(entry.getKey()),
                      toMetricData(entry.getValue(), entry.getKey())));
    return metricData;
  }

  private MetricData<CachedGauge<Double>> toMetricData(final DoubleSupplier valueSupplier,
                                                       final String[] labelValues) {
    final CachedGauge<Double> gauge = createGauge(valueSupplier);
    return new MetricData<>(gauge, labelValues);
  }

  @Override
  MetricType getType() {
    return GAUGE;
  }

  public static class GaugeBuilder extends AbstractMetricBuilder<Gauge, GaugeBuilder> {

    private final Map<String[], DoubleSupplier> valueSuppliers = new HashMap<>();

    public GaugeBuilder(final String name, final String help) {
      super(name, help);
    }

    /**
     * @see Gauge for more information on what value suppliers are and how they relate to label values
     */
    public GaugeBuilder withValueSupplier(final DoubleSupplier valueSupplier, final String... labelValues) {
      valueSuppliers.put(labelValues, valueSupplier);
      return this;
    }

    @Override
    protected Gauge create(final String fullName, final String help, final String[] labelNames) {
      return new Gauge(fullName, help, labelNames, valueSuppliers);
    }

    @Override
    void validateParams() {
      super.validateParams();
      validateValueSuppliers();
      validateValueSuppliersLabels();
    }

    private void validateValueSuppliers() {
      valueSuppliers.values()
                    .forEach(valueSupplier -> checkNotNull(valueSupplier, "Null value suppliers are not allowed"));
    }

    private void validateValueSuppliersLabels() {
      final int numOfLabels = labelNames.length;
      valueSuppliers.keySet()
                    .forEach(labelValues -> checkArgument(
                        labelValues.length == numOfLabels,
                        "Labels %s does not contain the expected amount %s",
                        Arrays.toString(labelValues),
                        numOfLabels));
    }
  }
}
