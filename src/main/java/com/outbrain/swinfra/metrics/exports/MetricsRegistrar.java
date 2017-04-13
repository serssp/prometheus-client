package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.util.function.Predicate;

public abstract class MetricsRegistrar {

    public abstract MetricRegistry registerMetricsTo(MetricRegistry registry, Predicate<String> nameFilter);

    public MetricRegistry registerMetricsTo(final MetricRegistry registry) {
        return registerMetricsTo(registry, (name) -> true);
    }

    protected void optionallyRegister(final Gauge metric, final MetricRegistry registry, final Predicate<String> nameFilter) {
        if (nameFilter.test(metric.getName())) {
            registry.getOrRegister(metric);
        }
    }
}
