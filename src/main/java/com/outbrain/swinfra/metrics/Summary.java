package com.outbrain.swinfra.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.data.MetricDataConsumer;
import com.outbrain.swinfra.metrics.data.SummaryData;
import com.outbrain.swinfra.metrics.timing.Clock;
import com.outbrain.swinfra.metrics.timing.Timer;
import com.outbrain.swinfra.metrics.timing.TimingMetric;
import com.outbrain.swinfra.metrics.utils.MetricType;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.outbrain.swinfra.metrics.timing.Clock.DEFAULT_CLOCK;
import static com.outbrain.swinfra.metrics.utils.LabelUtils.commaDelimitedStringToLabels;
import static com.outbrain.swinfra.metrics.utils.MetricType.SUMMARY;

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
 * @see <a href="https://prometheus.io/docs/concepts/metric_types/#summary">Prometheus summary metric</a>
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
    if (getLabelNames().isEmpty()) {
      return new UnlabeledChildRepo<>(new MetricData<>(createHistogram()));
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
  public MetricType getType() {
    return SUMMARY;
  }

  @Override
  public void forEachMetricData(final MetricDataConsumer consumer) {
    forEachChild(metricData -> {
      final CodahaleSummaryData snapshot = new CodahaleSummaryData(metricData.getMetric().getCount(), metricData.getMetric().getSnapshot());
      consumer.consumeSummary(this, metricData.getLabelValues(), snapshot);
    });
  }

  @Override
  public Timer startTimer(final String... labelValues) {
    final Histogram histogram = metricForLabels(labelValues);
    return new Timer(clock, histogram::update);
  }

  public static class SummaryBuilder extends AbstractMetricBuilder<Summary, SummaryBuilder> {

    private static final int DEFAULT_SIZE = 1028;
    private static final double DEFAULT_ALPHA = 0.015;

    private Clock clock = DEFAULT_CLOCK;
    private com.codahale.metrics.Clock codahaleClock = toCodahaleClock(DEFAULT_CLOCK);
    private Supplier<Reservoir> reservoirSupplier = () -> new ExponentiallyDecayingReservoir(DEFAULT_SIZE, DEFAULT_ALPHA,
                                                                                             codahaleClock);

    public SummaryBuilder(final String name, final String help) {
      super(name, help);
    }

    public SummaryBuilder withClock(final Clock clock) {
      this.clock = clock;
      this.codahaleClock = toCodahaleClock(clock);
      return this;
    }

    public ReservoirBuilder withReservoir() {
      return new ReservoirBuilder();
    }

    public class ReservoirBuilder {

      /**
       * Create this summary with an exponentially decaying reservoir - a reservoir that gives a lower
       * importance to older measurements with default size and alpha.
       *
       * @see <a href="http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf">
       */
      public SummaryBuilder withExponentiallyDecayingReservoir() {
        return withExponentiallyDecayingReservoir(DEFAULT_SIZE, DEFAULT_ALPHA);
      }

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
        reservoirSupplier = () -> new ExponentiallyDecayingReservoir(size, alpha, codahaleClock);
        return SummaryBuilder.this;
      }

      /**
       * Create this summary with a sliding time window reservoir. This reservoir keeps the measurements made in the
       * last {@code window} seconds (or other time unit).
       *
       * @param window     the window to save
       * @param windowUnit the window's time units
       */
      public SummaryBuilder withSlidingTimeWindowReservoir(final int window, final TimeUnit windowUnit) {
        reservoirSupplier = () -> new SlidingTimeWindowReservoir(window, windowUnit, codahaleClock);
        return SummaryBuilder.this;
      }

      /**
       * Create this summary with a sliding window reservoir. This reservoir keeps a constant amount of the last
       * measurements and is therefore memory-bound.
       *
       * @param size the number of measurements to save
       */
      public SummaryBuilder withSlidingWindowReservoir(final int size) {
        reservoirSupplier = () -> new SlidingWindowReservoir(size);
        return SummaryBuilder.this;
      }


      /**
       * Create this summary with a uniform reservoir - a reservoir that randomally saves the measurements and is
       * statistically representative of all measurements.
       *
       * @param size the size of the reservoir - the number of measurements that will be saved
       * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
       */
      public SummaryBuilder withUniformReservoir(final int size) {
        reservoirSupplier = () -> new UniformReservoir(size);
        return SummaryBuilder.this;
      }
    }

    @Override
    protected Summary create(final String fullName, final String help, final String[] labelNames) {
      return new Summary(fullName, help, labelNames, reservoirSupplier, clock);
    }
  }

  private static com.codahale.metrics.Clock toCodahaleClock(final Clock clock) {
    if (clock instanceof com.codahale.metrics.Clock) {
      return (com.codahale.metrics.Clock) clock;
    }
    return new CodahaleClockAdapter(clock);
  }

  private static class CodahaleClockAdapter extends com.codahale.metrics.Clock {

    private final Clock clock;

    CodahaleClockAdapter(final Clock clock) {
      this.clock = clock;
    }

    @Override
    public long getTick() {
      return clock.getTick();
    }


  }

  private static class CodahaleSummaryData implements SummaryData {

    private final long count;
    private final Snapshot snapshot;
    private long sum;


    private CodahaleSummaryData(final long count, final Snapshot snapshot) {
      this.count = count;
      this.snapshot = snapshot;
      sum = 0;
      for (final long value : snapshot.getValues()) {
        sum += value;
      }
    }

    @Override
    public long getCount() {
      return count;
    }

    @Override
    public double getSum() {
      return sum;
    }

    @Override
    public double getMedian() {
      return snapshot.getMedian();
    }

    @Override
    public double get75thPercentile() {
      return snapshot.get75thPercentile();
    }

    @Override
    public double get95thPercentile() {
      return snapshot.get95thPercentile();
    }

    @Override
    public double get98thPercentile() {
      return snapshot.get98thPercentile();
    }

    @Override
    public double get99thPercentile() {
      return snapshot.get99thPercentile();
    }

    @Override
    public double get999thPercentile() {
      return snapshot.get999thPercentile();
    }
  }
}
