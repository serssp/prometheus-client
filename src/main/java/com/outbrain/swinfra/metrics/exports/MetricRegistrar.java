package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.Metric;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.util.function.Predicate;

public abstract class MetricRegistrar {

    public abstract void registerMetricsTo(MetricRegistry registry, Predicate<String> nameFilter);

    public void registerMetricsTo(final MetricRegistry registry) {
        registerMetricsTo(registry, (name) -> true);
    }

    protected void optionallyRegister(final Metric metric, final MetricRegistry registry, final Predicate<String> nameFilter) {
        if (nameFilter.test(metric.getName())) {
            registry.getOrRegister(metric);
        }
    }
}
