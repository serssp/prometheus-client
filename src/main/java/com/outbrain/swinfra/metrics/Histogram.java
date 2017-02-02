package com.outbrain.swinfra.metrics;

import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import com.outbrain.swinfra.metrics.samples.SampleCreator;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import static com.outbrain.swinfra.metrics.utils.LabelUtils.addLabelToList;
import static com.outbrain.swinfra.metrics.utils.LabelUtils.commaDelimitedStringToLabels;
import static io.prometheus.client.Collector.MetricFamilySamples.Sample;
import static io.prometheus.client.Collector.Type.HISTOGRAM;

//todo document the fact that I favored throughput over consistency
public class Histogram extends AbstractMetric<Histogram.Buckets> {

  private static final String SAMPLE_NAME_BUCKET_SUFFIX = "_bucket";
  private static final String BUCKET_LABEL = "le";
  private final double[] buckets;

  private Histogram(final String name, final String help, final String[] labelNames, final double[] buckets) {
    super(name, help, labelNames);
    this.buckets = buckets;
  }

  @Override
  ChildMetricRepo<Buckets> createChildMetricRepo() {
    if (getLabelNames().size() == 0) {
      return new UnlabeledChildRepo<>(new MetricData<>(new Buckets(buckets), new String[]{}));
    } else {
      return new LabeledChildrenRepo<>(commaDelimitedLabelValues -> {
        final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues);
        return new MetricData<>(new Buckets(buckets), labelValues);
      });
    }
  }

  @Override
  List<Sample> createSamples(final MetricData<Buckets> metricData, final SampleCreator sampleCreator) {
    //todo use Collector.doubleToGoString?
    final BucketValues bucketValues = metricData.getMetric().getValues();
    final List<Sample> samples = new ArrayList<>(bucketValues.getBuckets().length + 2);

    //Add bucket samples
    final double[] bucketBounds = metricData.getMetric().bucketBounds;
    for (int i = 0; i < bucketBounds.length; i++) {
      final String bucketBound = bucketBoundToString(bucketBounds[i]);
      samples.add(sampleCreator.createSample(this.getName() + SAMPLE_NAME_BUCKET_SUFFIX,
                                             addLabelToList(getLabelNames(), BUCKET_LABEL),
                                             addLabelToList(metricData.getLabelValues(), bucketBound),
                                             bucketValues.getBuckets()[i]));
    }

    //Add count and sum samples
    final long lastBucketValue = bucketValues.getBuckets()[bucketValues.getBuckets().length - 1];
    samples.add(sampleCreator.createSample(getName() + "_count", getLabelNames(), metricData.getLabelValues(), lastBucketValue));
    samples.add(sampleCreator.createSample(getName() + "_sum", getLabelNames(), metricData.getLabelValues(), bucketValues.getSum()));

    return samples;
  }

  private String bucketBoundToString(final double bucketBound) {
    return bucketBound == Double.MAX_VALUE ? "+Inf" : String.valueOf(bucketBound);
  }

  @Override
  public Collector.Type getType() {
    return HISTOGRAM;
  }

  public void observe(final double value, final String... labelValues) {
    metricForLabels(labelValues).add(value);
  }

  /**
   * Contains bucket-related logic for the histogram.
   * When adding a value, only a single bucket is incremented, which is the first bucket in the array
   * that is associated with a number that's larger than or equal to that value.
   * <p>
   * When extracting the current values of the buckets, the counts of the buckets are summed so that each
   * bucket will contain its own count as well as the count of the previous bucket.
   * <p>
   * i.e. if the buckets contained the following values:
   * [3, 2, 1, 10]
   * when extracting the values, the following array will be created:
   * [3, 5, 6, 16]
   */
  static class Buckets {

    final double[] bucketBounds;
    final LongAdder[] buckets;
    final DoubleAdder sum = new DoubleAdder();

    Buckets(final double... bucketBounds) {
      this.bucketBounds = Arrays.copyOf(bucketBounds, bucketBounds.length + 1);
      this.bucketBounds[this.bucketBounds.length - 1] = Double.MAX_VALUE;

      this.buckets = new LongAdder[this.bucketBounds.length];
      for (int i = 0; i < buckets.length; i++) {
        buckets[i] = new LongAdder();
      }
    }

    void add(final double value) {
      for (int i = 0; i < buckets.length; i++) {
        if (value <= bucketBounds[i]) {
          buckets[i].add(1);
          break;
        }
      }
      sum.add(value);
    }

    BucketValues getValues() {
      final long[] cummulativeBuckets = new long[this.buckets.length];
      long accumulator = 0;

      for (int i = 0; i < buckets.length; i++) {
        accumulator += buckets[i].sum();
        cummulativeBuckets[i] = accumulator;
      }

      return new BucketValues(this.sum.sum(), cummulativeBuckets);
    }
  }

  static class BucketValues {
    private final double sum;
    private final long[] buckets;

    BucketValues(final double sum, final long[] buckets) {
      this.sum = sum;
      this.buckets = buckets;
    }

    public double getSum() {
      return sum;
    }

    public long[] getBuckets() {
      return buckets;
    }
  }

  public static class HistogramBuilder extends AbstractMetricBuilder<Histogram, HistogramBuilder> {

    private double[] buckets = new double[]{};

    HistogramBuilder(final String name, final String help) {
      super(name, help);
    }

    public HistogramBuilder withBuckets(final double... buckets) {
      this.buckets = buckets;
      return this;
    }

    @Override
    protected Histogram create(final String fullName, final String help, final String[] labelNames) {
      return new Histogram(fullName, help, labelNames, buckets);
    }

  }
}
