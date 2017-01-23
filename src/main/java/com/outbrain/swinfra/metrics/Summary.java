package com.outbrain.swinfra.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.samples.SampleCreator;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels;
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
public class Summary extends AbstractMetricWithQuantiles<Histogram> {

  private final Supplier<Reservoir> reservoirSupplier;

  private Summary(final String name, final String help, final String[] labelNames, final Supplier<Reservoir> reservoirSupplier) {
    super(name, help, labelNames);
    this.reservoirSupplier = reservoirSupplier;
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
  Collector.Type getType() {
    return SUMMARY;
  }

  @Override
  List<Sample> createSamples(final MetricData<Histogram> metricData,
                             final SampleCreator sampleCreator) {
    return createSamplesFromSnapshot(metricData, 1, sampleCreator);
  }

  public static class SummaryBuilder extends AbstractMetricBuilder<Summary, SummaryBuilder> {

    private Supplier<Reservoir> reservoir = ExponentiallyDecayingReservoir::new;

    public SummaryBuilder(final String name, final String help) {
      super(name, help);
    }

    public ReservoirBuilder withReservoir() {
      return new ReservoirBuilder();
    }

    private SummaryBuilder withReservoir(final Supplier<Reservoir> reservoirSupplier) {
      this.reservoir = reservoirSupplier;
      return this;
    }

    @Override
    protected Summary create(final String fullName, final String help, final String[] labelNames) {
      return new Summary(fullName, help, labelNames, reservoir);
    }

    private class ReservoirBuilder {

      /**
       * Create this summary with an exponentially decaying reservoir - a reservoir that gives a lower
       * importance to older measurements.
       *
       * @param size  the size of the reservoir - the number of measurements that will be saved
       * @param alpha the exponential decay factor. The higher this is the more biased the reservoir will
       *              be towards newer measurements.
       * @see <a href="http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf">
       */
      public SummaryBuilder withExponentiallyDecayingReservoir(final int size, final double alpha) {
        return withReservoir(() -> new ExponentiallyDecayingReservoir(size, alpha));
      }

      /**
       * Create this summary with a sliding time window reservoir. This reservoir keeps the measurements made in the
       * last {@code window} seconds (or other time unit).
       *
       * @param window     the window to save
       * @param windowUnit the window's time units
       */
      public SummaryBuilder withSlidingTimeWindowReservoir(final int window, final TimeUnit windowUnit) {
        return withReservoir(() -> new SlidingTimeWindowReservoir(window, windowUnit));
      }

      /**
       * Create this summary with a sliding window reservoir. This reservoir keeps a constant amount of the last
       * measurements and is therefore memory-bound.
       *
       * @param size the number of measurements to save
       */
      public SummaryBuilder withSlidingWindowReservoir(final int size) {
        return withReservoir(() -> new SlidingWindowReservoir(size));
      }


      /**
       * Create this summary with a uniform reservoir - a reservoir that randomally saves the measurements and is
       * statistically representative of all measurements.
       *
       * @param size the size of the reservoir - the number of measurements that will be saved
       * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
       */
      public SummaryBuilder withUniformReservoir(final int size) {
        return withReservoir(() -> new UniformReservoir(size));
      }
    }
  }
}
