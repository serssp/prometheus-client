package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.samples.SampleCreator;
import com.outbrain.swinfra.metrics.timing.*;
import com.outbrain.swinfra.metrics.timing.Timer;
import com.outbrain.swinfra.metrics.utils.QuantileUtils;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.List;
import java.util.function.Supplier;

import static com.outbrain.swinfra.metrics.timing.Clock.DEFAULT_CLOCK;
import static com.outbrain.swinfra.metrics.utils.LabelUtils.commaDelimitedStringToLabels;
import static io.prometheus.client.Collector.Type.SUMMARY;

/**
 * An implementation of a Summary metric. A summary is a histogram that samples its measurements and has no predefined
 * buckets. The summary calculates several quantiles over its observed measurements.
 * <p>
 * The summary exposes several time series:
 * <ul>
 * <li>
 * Sum - the sum of all its measurements.
 * The name of this metric will consist of the original metric name with a '_sum' suffix
 * </li>
 * <li>
 * Count - the number of measurements taken.
 * The name of this metric will consist of the original metric name with a '_count' suffix
 * <li>
 * Quantiles - the 0.5, 0.75, 0.95, 0.98, 0.99 and 0.999 quantiles.
 * Each of these will have the same name as the original metric, but with a 'quantile' label added
 * </li>
 * </ul>
 * </p>
 *
 * @see <a href="https://prometheus.io/docs/concepts/metric_types/#counter">Prometheus summary metric</a>
 * @see <a href="https://prometheus.io/docs/practices/histograms/">Prometheus summary vs. histogram</a>
 */
public class Summary extends AbstractMetric<Histogram> implements TimingMetric {

  private final Supplier<Reservoir> reservoirSupplier;
  private final Clock clock;

  private Summary(final String name,
                  final String help,
                  final String[] labelNames,
                  final Supplier<Reservoir> reservoirSupplier,
                  final Clock clock) {
    super(name, help, labelNames);
    this.reservoirSupplier = reservoirSupplier;
    this.clock = clock;
  }

  public void observe(final int value, final String... labelValues) {
    validateLabelValues(labelValues);
    metricForLabels(labelValues).update(value);
  }

  @Override
  ChildMetricRepo<Histogram> createChildMetricRepo() {
    if (getLabelNames().size() == 0) {
      return new UnlabeledChildRepo<>(new MetricData<>(createHistogram(), new String[]{}));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(createHistogram(), labelValues);
      });
    }
  }

  private Histogram createHistogram() {
    return new Histogram(reservoirSupplier.get());
  }

  @Override
  public Collector.Type getType() {
    return SUMMARY;
  }

  @Override
  List<Sample> createSamples(final MetricData<Histogram> metricData,
                             final SampleCreator sampleCreator) {
    return QuantileUtils.createSamplesFromSnapshot(metricData, getName(), getLabelNames(), 1, sampleCreator);
  }

  @Override
  public Timer startTimer(final String... labelValues) {
    final Histogram histogram = metricForLabels(labelValues);
    return new Timer(clock, histogram::update);
  }

  public static class SummaryBuilder extends AbstractMetricBuilderWithReservoirs<Summary, SummaryBuilder> {

    private Clock clock = DEFAULT_CLOCK;

    public SummaryBuilder(final String name, final String help) {
      super(name, help);
    }

    public SummaryBuilder withClock(final Clock clock) {
      this.clock = clock;
      return this;
    }

    @Override
    protected Summary create(final String fullName, final String help, final String[] labelNames) {
      return new Summary(fullName, help, labelNames, getReservoirSupplier(), clock);
    }
  }
}
