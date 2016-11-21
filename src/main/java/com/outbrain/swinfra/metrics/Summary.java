package com.outbrain.swinfra.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.ArrayList;
import java.util.List;

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels;
import static io.prometheus.client.Collector.Type.SUMMARY;

/**
 * An implementation of a Summary metric. A summary is a histogram that samples its measurements and has no predefined
 * buckets. The summary calculates several quantiles over its observed measurements.
 * <p>
 *   The summary exposes several time series:
 *   <ul>
 *     <li>
 *          Sum - the sum of all its measurements.
 *          The name of this metric will consist of the original metric name with a '_sum' suffix
 *     </li>
 *     <li>
 *          Count - the number of measurements taken.
 *          The name of this metric will consist of the original metric name with a '_count' suffix
 *     <li>
 *          Quantiles - the 0.5, 0.75, 0.95, 0.98, 0.99 and 0.999 quantiles.
 *          Each of these will have the same name as the original metric, but with a 'quantile' label added
 *     </li>
 *   </ul>
 * </p>
 *
 * @see <a href="https://prometheus.io/docs/concepts/metric_types/#counter">Prometheus summary metric</a>
 * @see <a href="https://prometheus.io/docs/practices/histograms/">Prometheus summary vs. histogram</a>
 */
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
    return new Histogram(new ExponentiallyDecayingReservoir());
  }

  @Override
  Collector.Type getType() {
    return SUMMARY;
  }

  @Override
  MetricFamilySamples toMetricFamilySamples(final MetricData<Histogram> metricData) {
    final List<Sample> samples = createQuantileSamples(metricData);
    return new MetricFamilySamples(getName(), SUMMARY, getHelp(), samples);
  }

  private List<Sample> createQuantileSamples(final MetricData<Histogram> metricData) {
    final Snapshot snapshot = metricData.getMetric().getSnapshot();

    final List<String> labels = addToList(getLabelNames(), QUANTILE_LABEL);
    final List<String> labelValues = metricData.getLabelValues();

    long sum = 0;
    for (final long value : snapshot.getValues()) {
      sum += value;
    }

    return Lists.newArrayList(
      new Sample(getName(), labels, addToList(labelValues, "0.5"), snapshot.getMedian()),
      new Sample(getName(), labels, addToList(labelValues, "0.75"), snapshot.get75thPercentile()),
      new Sample(getName(), labels, addToList(labelValues, "0.95"), snapshot.get95thPercentile()),
      new Sample(getName(), labels, addToList(labelValues, "0.98"), snapshot.get98thPercentile()),
      new Sample(getName(), labels, addToList(labelValues, "0.99"), snapshot.get99thPercentile()),
      new Sample(getName(), labels, addToList(labelValues, "0.999"), snapshot.get999thPercentile()),
      new Sample(getName() + "_count", getLabelNames(), labelValues, metricData.getMetric().getCount()),
      new Sample(getName() + "_sum", getLabelNames(), labelValues, sum)
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
