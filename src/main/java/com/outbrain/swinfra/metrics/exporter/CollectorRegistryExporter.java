package com.outbrain.swinfra.metrics.exporter;

import com.outbrain.swinfra.metrics.MetricCollector;
import com.outbrain.swinfra.metrics.MetricCollectorRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CollectorRegistryExporter {

    private final MetricCollectorRegistry registry;
    private final Map<MetricCollector, CollectorExporter> exporterByCollectorMap = new ConcurrentHashMap<>();
    private final Function<? super MetricCollector, ? extends CollectorExporter> createExporter;

    public CollectorRegistryExporter(final MetricCollectorRegistry registry, final Function<? super MetricCollector, ? extends CollectorExporter> createExporter) {
        this.registry = registry;
        this.createExporter = createExporter;
    }

    public void export(final Appendable appendable) throws IOException {
        for (final MetricCollector collector : registry) {
            final CollectorExporter exporter = exporterByCollectorMap.computeIfAbsent(collector, createExporter);
            exporter.exportTo(appendable);
        }

    }
}
