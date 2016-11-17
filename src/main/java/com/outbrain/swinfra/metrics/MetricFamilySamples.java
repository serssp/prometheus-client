package com.outbrain.swinfra.metrics;

import java.util.List;
import java.util.Objects;

final class MetricFamilySamples {
  private final String name;
  private final MetricType type;
  private final String help;
  private final List<Sample> samples;

  static MetricFamilySamples from(final String name,
                                  final MetricType type,
                                  final String help,
                                  final List<Sample> samples) {
    return new MetricFamilySamples(name, type, help, samples);
  }

  private MetricFamilySamples(final String name, final MetricType type, final String help, final List<Sample> samples) {
    this.name = name;
    this.type = type;
    this.help = help;
    this.samples = samples;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final MetricFamilySamples that = (MetricFamilySamples) o;
    return Objects.equals(name, that.name) &&
        type == that.type &&
        Objects.equals(help, that.help) &&
        Objects.equals(samples, that.samples);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, help, samples);
  }

  @Override
  public String toString() {
    return "MetricFamilySamples{" +
        "name='" + name + '\'' +
        ", type=" + type +
        ", help='" + help + '\'' +
        ", samples=" + samples +
        '}';
  }

  /**
   * A single Sample, with a unique name and set of labels.
   */
  static class Sample {
    final String name;
    final List<String> labelNames;
    final List<String> labelValues;  // Must have same length as withLabels.
    final double value;

    static Sample from(final String name,
                       final List<String> labelNames,
                       final List<String> labelValues,
                       final double value) {
      return new Sample(name, labelNames, labelValues, value);
    }

    private Sample(final String name,
                   final List<String> labelNames,
                   final List<String> labelValues,
                   final double value) {
      this.name = name;
      this.labelNames = labelNames;
      this.labelValues = labelValues;
      this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final Sample sample = (Sample) o;
      return Double.compare(sample.value, value) == 0 &&
          Objects.equals(name, sample.name) &&
          Objects.equals(labelNames, sample.labelNames) &&
          Objects.equals(labelValues, sample.labelValues);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, labelNames, labelValues, value);
    }

    @Override
    public String toString() {
      return "Sample{" +
          "name='" + name + '\'' +
          ", labelNames=" + labelNames +
          ", labelValues=" + labelValues +
          ", value=" + value +
          '}';
    }
  }
}