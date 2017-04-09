package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.SettableGauge.SettableDoubleSupplier;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.utils.MetricType;

import java.io.IOException;
import java.util.function.DoubleSupplier;

import static com.outbrain.swinfra.metrics.utils.LabelUtils.commaDelimitedStringToLabels;

/**
 * An implementation of a Gauge metric. A gauge is a decimal value that can increase or decrease.
 * <p>
 * The gauge exposes a single time-series with its value and labels.
 * </p>
 * <p>
 * The SettableGauge is meant to be set from the outside as opposed to the regular {@link Gauge}. This is an important
 * distinction because consecutive calls to the method <i>set</i> will not all show when sampling this metric, rather
 * the last value set will show.
 * </p>
 * <p>
 * So the following calls:
 * </p>
 * @see <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Prometheus gauge metric</a>
 */
public class SettableGauge extends AbstractMetric<SettableDoubleSupplier> {

  SettableGauge(String name, String help, String[] labelNames) {
    super(name, help, labelNames);
  }

  public double getValue(final String... labelValues) {
    return metricForLabels(labelValues).getAsDouble();
  }

  @Override
  ChildMetricRepo<SettableDoubleSupplier> createChildMetricRepo() {
    if (getLabelNames().isEmpty()) {
      return new UnlabeledChildRepo<>(new MetricData<>(new SettableDoubleSupplier()));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(new SettableDoubleSupplier(), labelValues);
      });
    }
  }


  @Override
  public MetricType getType() {
    return MetricType.GAUGE;
  }

  @Override
  public void forEachSample(final SampleConsumer sampleConsumer) throws IOException {
    for (final MetricData<SettableDoubleSupplier> metricData : allMetricData()) {
      sampleConsumer.apply(getName(), metricData.getMetric().getAsDouble(), metricData.getLabelValues(), null, null);
    }
  }

  public void set(double value, final String... labelValues) {
    metricForLabels(labelValues).set(value);
  }

  public static class SettableGaugeBuilder extends AbstractMetricBuilder<SettableGauge, SettableGauge.SettableGaugeBuilder> {

    public SettableGaugeBuilder(final String name, final String help) {
      super(name, help);
    }

    @Override
    protected SettableGauge create(final String fullName, final String help, final String[] labelNames) {
      return new SettableGauge(fullName, help, labelNames);
    }

  }

  static class SettableDoubleSupplier implements DoubleSupplier {

    private volatile double value = 0;

    void set(final double value) {
      this.value = value;
    }

    @Override
    public double getAsDouble() {
      return value;
    }
  }

}
