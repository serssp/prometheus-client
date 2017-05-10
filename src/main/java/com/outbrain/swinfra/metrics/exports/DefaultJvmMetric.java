package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.MetricRegistry;

import java.util.function.Predicate;

public class DefaultJvmMetric extends MetricRegistrar {

    @Override
    public void registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        new ClassLoadingMetric().registerMetricsTo(registry, nameFilter);
        new GarbageCollectorMetric().registerMetricsTo(registry, nameFilter);
        new MemoryPoolsMetric().registerMetricsTo(registry, nameFilter);
        new StandardMetric().registerMetricsTo(registry, nameFilter);
        new ThreadMetric().registerMetricsTo(registry, nameFilter);
    }
}
