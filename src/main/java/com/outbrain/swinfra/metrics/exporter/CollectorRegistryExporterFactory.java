package com.outbrain.swinfra.metrics.exporter;

import com.outbrain.swinfra.metrics.MetricCollectorRegistry;
import com.outbrain.swinfra.metrics.exporter.text.TextFormatter;

public enum CollectorRegistryExporterFactory {

    TEXT_004 {
        @Override
        public CollectorRegistryExporter create(final MetricCollectorRegistry registry) {
            return new CollectorRegistryExporter(registry, TextFormatter::new);
        }
    },
    PROTOBUF {
        @Override
        public CollectorRegistryExporter create(final MetricCollectorRegistry registry) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    };

    public abstract CollectorRegistryExporter create(MetricCollectorRegistry registry);
}
