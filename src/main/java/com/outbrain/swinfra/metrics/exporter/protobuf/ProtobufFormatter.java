package com.outbrain.swinfra.metrics.exporter.protobuf;

import com.outbrain.swinfra.metrics.Histogram;
import com.outbrain.swinfra.metrics.Metric;
import com.outbrain.swinfra.metrics.MetricCollector;
import com.outbrain.swinfra.metrics.Summary;
import com.outbrain.swinfra.metrics.exporter.CollectorExporter;
import com.outbrain.swinfra.metrics.timing.TimingMetric;
import io.prometheus.client.Metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Protocol buffers formatter
 *
 * @author marenzon
 * @see <a href="https://github.com/prometheus/client_model/blob/master/metrics.proto">metrics.proto</a>
 */
public class ProtobufFormatter implements CollectorExporter {

  public static final String CONTENT_TYPE_PROTOBUF = "application/vnd.google.protobuf; " +
    "proto=io.prometheus.client.MetricFamily; " +
    "encoding=delimited";

  private final MetricCollector metricCollector;

  public ProtobufFormatter(final MetricCollector metricCollector) {
    this.metricCollector = requireNonNull(metricCollector, "metricCollector may not be null");
  }

  @Override
  public void exportTo(final OutputStream stream) throws IOException {
    final Map<String, String> staticLabels = metricCollector.getStaticLabels();

    for (final Metric metric : metricCollector) {
      // creating protobuf metric family
      final Metrics.MetricFamily.Builder familyBuilder = createMetricFamily(metric);

      // defined labels
      final List<String> labelNames = metric.getLabelNames();

      // metric creator
      final Function<List<String>, Metrics.Metric.Builder> createMetric = metricCreator(staticLabels, labelNames);

      // apply samples into metric family
      consumeByType(metric, familyBuilder, createMetric);

      // flush metric family into output stream
      familyBuilder.build().writeDelimitedTo(stream);
    }
  }

  private void consumeByType(final Metric metric,
                             final Metrics.MetricFamily.Builder familyBuilder,
                             final Function<List<String>, Metrics.Metric.Builder> createMetric) {
    switch (metric.getType()) {
      case COUNTER:
        familyBuilder.setType(Metrics.MetricType.COUNTER);
        consumeCounterSample(metric, familyBuilder, createMetric);
        break;

      case GAUGE:
        familyBuilder.setType(Metrics.MetricType.GAUGE);
        consumeGaugeSample(metric, familyBuilder, createMetric);
        break;

      case SUMMARY:
        familyBuilder.setType(Metrics.MetricType.SUMMARY);
        consumeSummarySample(metric, familyBuilder, createMetric);
        break;

      case HISTOGRAM:
        familyBuilder.setType(Metrics.MetricType.HISTOGRAM);
        consumeHistogramSample(metric, familyBuilder, createMetric);
        break;

      default:
        throw new UnsupportedOperationException("not implemented for metric type: " + metric.getType().getName());
    }
  }

  private void consumeGaugeSample(final Metric metricData,
                                  final Metrics.MetricFamily.Builder familyBuilder,
                                  final Function<List<String>, Metrics.Metric.Builder> createMetric) {
    metricData.forEachSample(sample -> {
      final Metrics.Gauge gauge = Metrics.Gauge.newBuilder().
        setValue(sample.getValue()).
        build();

      final Metrics.Metric metric = createMetric.apply(sample.getLabelValues()).
        setGauge(gauge).
        build();

      familyBuilder.addMetric(metric);
    });
  }

  private void consumeCounterSample(final Metric metricData,
                                    final Metrics.MetricFamily.Builder familyBuilder,
                                    final Function<List<String>, Metrics.Metric.Builder> createMetric) {
    metricData.forEachSample(sample -> {
      final Metrics.Counter counter = Metrics.Counter.newBuilder().
        setValue(sample.getValue()).
        build();

      final Metrics.Metric metric = createMetric.apply(sample.getLabelValues()).
        setCounter(counter).
        build();

      familyBuilder.addMetric(metric);
    });
  }

  private void consumeSummarySample(final Metric metricData,
                                    final Metrics.MetricFamily.Builder familyBuilder,
                                    final Function<List<String>, Metrics.Metric.Builder> createMetric) {
    final AtomicReference<List<String>> labelValues = new AtomicReference<>(Collections.emptyList());
    final Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();

    metricData.forEachSample(sample -> {
      labelValues.set(sample.getLabelValues());

      if (Summary.QUANTILE_LABEL.equals(sample.getExtraLabelName())) {
        final Metrics.Quantile quantile = Metrics.Quantile.newBuilder().
          setQuantile(Double.parseDouble(sample.getExtraLabelValue())).
          setValue(sample.getValue()).
          build();

        summaryBuilder.addQuantile(quantile);
        return;
      }

      if (sample.getName().endsWith(TimingMetric.COUNT_SUFFIX)) {
        summaryBuilder.setSampleCount((long) sample.getValue());
        return;
      }

      if (sample.getName().endsWith(TimingMetric.SUM_SUFFIX)) {
        summaryBuilder.setSampleSum(sample.getValue());
        return;
      }

      throw new RuntimeException("invalid sample");
    });

    final Metrics.Summary summary = summaryBuilder.build();
    final Metrics.Metric metric = createMetric.apply(labelValues.get()).
      setSummary(summary).
      build();

    familyBuilder.addMetric(metric);
  }

  private void consumeHistogramSample(final Metric metricData,
                                      final Metrics.MetricFamily.Builder familyBuilder,
                                      final Function<List<String>, Metrics.Metric.Builder> createMetric) {
    final AtomicReference<List<String>> labelValues = new AtomicReference<>(Collections.emptyList());
    final Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();

    metricData.forEachSample(sample -> {
      labelValues.set(sample.getLabelValues());

      if (Histogram.BUCKET_LABEL.equals(sample.getExtraLabelName())) {
        final Metrics.Bucket bucket = Metrics.Bucket.newBuilder().
          setCumulativeCount((long) sample.getValue()).
          setUpperBound(Double.parseDouble(sample.getExtraLabelValue())).
          build();

        histogramBuilder.addBucket(bucket);
      }

      if (sample.getName().endsWith(TimingMetric.COUNT_SUFFIX)) {
        histogramBuilder.setSampleCount((long) sample.getValue());
      }

      if (sample.getName().endsWith(TimingMetric.SUM_SUFFIX)) {
        histogramBuilder.setSampleSum(sample.getValue());
        return;
      }

      throw new RuntimeException("invalid sample");
    });

    final Metrics.Histogram histogram = histogramBuilder.build();
    final Metrics.Metric metric = createMetric.apply(labelValues.get()).
      setHistogram(histogram).
      build();

    familyBuilder.addMetric(metric);
  }

  private Function<List<String>, Metrics.Metric.Builder> metricCreator(final Map<String, String> staticLabels,
                                                                       final List<String> labelNames) {
    return (labelValues) -> {
      final Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();

      // registering all default labelValues
      staticLabels.forEach((n, v) ->
        metricBuilder.addLabel(createLabel(n, v)));

      // setting current sample labelValues
      for (int i = 0; i < labelValues.size(); i++) {
        metricBuilder.addLabel(createLabel(labelNames.get(i), labelValues.get(i)));
      }

      return metricBuilder;
    };
  }

  private Metrics.MetricFamily.Builder createMetricFamily(final Metric metric) {
    return Metrics.MetricFamily.newBuilder().
      setName(metric.getName()).
      setHelp(metric.getHelp());
  }

  private Metrics.LabelPair createLabel(final String n, final String v) {
    return Metrics.LabelPair.newBuilder().
      setName(n).
      setValue(v).
      build();
  }
}