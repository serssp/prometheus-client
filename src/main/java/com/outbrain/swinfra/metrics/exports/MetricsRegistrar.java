package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.MetricRegistry;

public interface MetricsRegistrar {

    MetricRegistry registerMetricsTo(MetricRegistry registry);
}
