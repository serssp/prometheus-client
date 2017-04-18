package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.MetricRegistry;

import java.util.function.Predicate;

public class DefaultJvmMetrics extends MetricsRegistrar {

    @Override
    public MetricRegistry registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        return new ClassLoadingMetrics().registerMetricsTo(
                new GarbageCollectorMetrics().registerMetricsTo(
                    new MemoryPoolsMetrics().registerMetricsTo(
                        new StandardMetrics().registerMetricsTo(
                            new ThreadMetrics().registerMetricsTo(
                                registry, nameFilter), nameFilter), nameFilter), nameFilter), nameFilter);
    }
}
