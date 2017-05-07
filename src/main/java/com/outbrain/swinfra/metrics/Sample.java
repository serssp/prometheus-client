package com.outbrain.swinfra.metrics;

import java.util.List;
import java.util.Objects;

/**
 * @author marenzon
 */
public class Sample {

  private final String name;
  private final double value;
  private final List<String> labelValues;
  private final String extraLabelName;
  private final String extraLabelValue;

  public Sample(final String name,
                final double value,
                final List<String> labelValues,
                final String extraLabelName,
                final String extraLabelValue) {
    this.name = name;
    this.value = value;
    this.labelValues = labelValues;
    this.extraLabelName = extraLabelName;
    this.extraLabelValue = extraLabelValue;
  }

  public String getName() {
    return name;
  }

  public double getValue() {
    return value;
  }

  public List<String> getLabelValues() {
    return labelValues;
  }

  public String getExtraLabelName() {
    return extraLabelName;
  }

  public String getExtraLabelValue() {
    return extraLabelValue;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Sample)) return false;
    final Sample sample = (Sample) o;
    return Double.compare(sample.value, value) == 0 &&
      Objects.equals(name, sample.name) &&
      Objects.equals(labelValues, sample.labelValues) &&
      Objects.equals(extraLabelName, sample.extraLabelName) &&
      Objects.equals(extraLabelValue, sample.extraLabelValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, labelValues, extraLabelName, extraLabelValue);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Sample{");
    sb.append("name='").append(name).append('\'');
    sb.append(", value=").append(value);
    sb.append(", labelValues=").append(labelValues);
    sb.append(", extraLabelName='").append(extraLabelName).append('\'');
    sb.append(", extraLabelValue='").append(extraLabelValue).append('\'');
    sb.append('}');
    return sb.toString();
  }
}