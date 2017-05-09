package com.outbrain.swinfra.metrics.exporter.protobuf;

import com.outbrain.swinfra.metrics.Histogram;
import com.outbrain.swinfra.metrics.Metric;
import com.outbrain.swinfra.metrics.MetricCollector;
import com.outbrain.swinfra.metrics.Sample;
import com.outbrain.swinfra.metrics.Summary;
import com.outbrain.swinfra.metrics.exporter.CollectorExporter;
import com.outbrain.swinfra.metrics.timing.TimingMetric;
import io.prometheus.client.Metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

      final Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
      // registering all default labelValues
      staticLabels.forEach((n, v) ->
          metricBuilder.addLabel(createLabel(n, v)));

      // apply samples into metric family
      consumeByType(metric, familyBuilder, metricBuilder);

      // flush metric family into output stream
      familyBuilder.build().writeDelimitedTo(stream);
    }
  }

  private void consumeByType(final Metric metric,
                             final Metrics.MetricFamily.Builder familyBuilder,
                             final Metrics.Metric.Builder metricBuilder) {
    switch (metric.getType()) {
      case COUNTER:
        familyBuilder.setType(Metrics.MetricType.COUNTER);
        consumeCounterSample(metric, familyBuilder, metricBuilder);
        break;

      case GAUGE:
        familyBuilder.setType(Metrics.MetricType.GAUGE);
        consumeGaugeSample(metric, familyBuilder, metricBuilder);
        break;

      case SUMMARY:
        familyBuilder.setType(Metrics.MetricType.SUMMARY);
        consumeSummarySample(metric, familyBuilder, metricBuilder);
        break;

      case HISTOGRAM:
        familyBuilder.setType(Metrics.MetricType.HISTOGRAM);
        consumeHistogramSample(metric, familyBuilder, metricBuilder);
        break;

      default:
        throw new UnsupportedOperationException("not implemented for metric type: " + metric.getType().getName());
    }
  }

  private void consumeGaugeSample(final Metric metricData,
                                  final Metrics.MetricFamily.Builder familyBuilder,
                                  final Metrics.Metric.Builder metricBuilder) {
    metricData.forEachSample(sample -> {
      final Metrics.Gauge gauge = Metrics.Gauge.newBuilder().
          setValue(sample.getValue()).
          build();

      setSampleLabels(metricBuilder, metricData.getLabelNames(), sample.getLabelValues());

      final Metrics.Metric metric = metricBuilder.setGauge(gauge).build();

      familyBuilder.addMetric(metric);
    });
  }

  private void consumeCounterSample(final Metric metricData,
                                    final Metrics.MetricFamily.Builder familyBuilder,
                                    final Metrics.Metric.Builder metricBuilder) {
    metricData.forEachSample(sample -> {
      final Metrics.Counter counter = Metrics.Counter.newBuilder().
        setValue(sample.getValue()).
        build();

      setSampleLabels(metricBuilder, metricData.getLabelNames(), sample.getLabelValues());

      final Metrics.Metric metric = metricBuilder.setCounter(counter).build();

      familyBuilder.addMetric(metric);
    });
  }

  private void consumeSummarySample(final Metric metricData,
                                    final Metrics.MetricFamily.Builder familyBuilder,
                                    final Metrics.Metric.Builder metricBuilder) {

    final Map<List<String>, Metrics.Summary.Builder> summaryBuilderByLabelValues = new HashMap<>();

    metricData.forEachSample(sample -> {

      final Metrics.Summary.Builder summaryBuilder =
          summaryBuilderByLabelValues.computeIfAbsent(sample.getLabelValues(), (labelValues) -> Metrics.Summary.newBuilder());

      if (Summary.QUANTILE_LABEL.equals(sample.getExtraLabelName())) {
        final Metrics.Quantile quantile = Metrics.Quantile.newBuilder().
          setQuantile(Double.parseDouble(sample.getExtraLabelValue())).
          setValue(sample.getValue()).
          build();

        summaryBuilder.addQuantile(quantile);
      }
      else if (isCountSample(sample)) {
        summaryBuilder.setSampleCount((long) sample.getValue());
      }
      else if (isSumSample(sample)) {
        summaryBuilder.setSampleSum(sample.getValue());
      }
    });
    summaryBuilderByLabelValues.forEach((labelValues, summaryBuilder) -> {

      final int currentCount = metricBuilder.getLabelCount();
      setSampleLabels(metricBuilder, metricData.getLabelNames(), labelValues);
      final Metrics.Metric metric = metricBuilder.setSummary(summaryBuilder.build()).build();
      resetMetricBuilderLabels(metricBuilder, labelValues, currentCount);

      familyBuilder.addMetric(metric);
    });
  }

  private void consumeHistogramSample(final Metric metricData,
                                      final Metrics.MetricFamily.Builder familyBuilder,
                                      final Metrics.Metric.Builder metricBuilder) {

    final Map<List<String>, Metrics.Histogram.Builder> histogramBuilderByLabelValues = new HashMap<>();

    metricData.forEachSample(sample -> {

      final Metrics.Histogram.Builder histogramBuilder =
          histogramBuilderByLabelValues.computeIfAbsent(sample.getLabelValues(), (labelValues) -> Metrics.Histogram.newBuilder());

      if (Histogram.BUCKET_LABEL.equals(sample.getExtraLabelName())) {
        final Metrics.Bucket bucket = Metrics.Bucket.newBuilder().
          setCumulativeCount((long) sample.getValue()).
          setUpperBound("+Inf".equals(sample.getExtraLabelValue()) ?
            Double.POSITIVE_INFINITY :
            Double.parseDouble(sample.getExtraLabelValue())).
          build();

        histogramBuilder.addBucket(bucket);
      }
      else if (isSumSample(sample)) {
        histogramBuilder.setSampleSum(sample.getValue());
      }
      else if (isCountSample(sample)) {
        histogramBuilder.setSampleCount((long) sample.getValue());
      }
    });

    histogramBuilderByLabelValues.forEach((labelValues, histogramBuilder) -> {

      final int currentCount = metricBuilder.getLabelCount();

      setSampleLabels(metricBuilder, metricData.getLabelNames(), labelValues);
      final Metrics.Metric metric = metricBuilder.setHistogram(histogramBuilder.build()).build();
      resetMetricBuilderLabels(metricBuilder, labelValues, currentCount);

      familyBuilder.addMetric(metric);
    });
  }

  private boolean isSumSample(final Sample sample) {
    return sample.getName().endsWith(TimingMetric.SUM_SUFFIX);
  }

  private boolean isCountSample(final Sample sample) {
    return sample.getName().endsWith(TimingMetric.COUNT_SUFFIX);
  }


  private void setSampleLabels(final Metrics.Metric.Builder metricBuilder, final List<String> labelNames, final List<String> labelValues) {
    for (int i = 0; i < labelValues.size(); i++) {
      metricBuilder.addLabel(createLabel(labelNames.get(i), labelValues.get(i)));
    }
  }

  private void resetMetricBuilderLabels(final Metrics.Metric.Builder metricBuilder, final List<String> labelValues, final int currentCount) {
    for (int i = labelValues.size() - 1; i >= 0; i--) {
      metricBuilder.removeLabel(currentCount + i);
    }
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