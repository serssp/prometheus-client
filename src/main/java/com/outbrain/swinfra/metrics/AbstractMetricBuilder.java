package com.outbrain.swinfra.metrics;

import org.apache.commons.lang3.Validate;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractMetricBuilder<T extends AbstractMetric, B extends AbstractMetricBuilder<T, B>> {

  public static final Pattern METRIC_NAME_PATTERN = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");
  public static final Pattern LABEL_NAME_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

  private static final String PROMETHEUS_NAMING_URL = "https://prometheus.io/docs/practices/naming/";

  private final String name;
  private final String help;

  private String namespace = "";
  private String subsystem = "";
  String[] labelNames = new String[] {};

  AbstractMetricBuilder(final String name, final String help) {
    this.name = name;
    this.help = help;
  }

  public B withSubsystem(final String subsystem) {
    this.subsystem = subsystem;
    return getThis();
  }

  public B withNamespace(final String namespace) {
    this.namespace = namespace;
    return getThis();
  }

  public B withLabels(final String... labelNames) {
    this.labelNames = labelNames;
    return getThis();
  }

  protected abstract T create(final String fullName, final String help, final String[] labelNames);

  public T build() {
    validateParams();
    final T metric = create(createFullName(), help, labelNames);
    metric.initChildMetricRepo();
    return metric;
  }

  void validateParams() {
    Validate.notBlank(help, "The metric's help must contain text");
    validateName(name);
    validateLabels();
  }

  private void validateLabels() {
    for (final String labelName : labelNames) {
      Validate.notBlank(labelName, "Label names must contain text");
      Validate.isTrue(LABEL_NAME_PATTERN.matcher(labelName).matches(), "The label name '" + labelName + "' is invalid. See " + PROMETHEUS_NAMING_URL);
    }
  }

  private void validateName(final String name) {
    Validate.notBlank(name, "The metric's name must contain text");
    Validate.isTrue(METRIC_NAME_PATTERN.matcher(name).matches(), "The metric name '" + name + "' is invalid. See " + PROMETHEUS_NAMING_URL);
  }

  private String createFullName() {
    final StringBuilder sb = new StringBuilder(name);
    if (isNotBlank(subsystem)) sb.insert(0, subsystem + "_");
    if (isNotBlank(namespace)) sb.insert(0, namespace + "_");
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  protected B getThis() {
    return (B) this;
  }
}
