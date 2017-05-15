package com.outbrain.swinfra.metrics.client;

import com.outbrain.swinfra.metrics.Counter;
import com.outbrain.swinfra.metrics.Histogram;
import com.outbrain.swinfra.metrics.MetricCollector;
import com.outbrain.swinfra.metrics.MetricCollectorRegistry;
import com.outbrain.swinfra.metrics.MetricRegistry;
import com.outbrain.swinfra.metrics.exporter.CollectorRegistryExporter;
import com.outbrain.swinfra.metrics.exporter.CollectorRegistryExporterFactory;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Collections.emptyMap;

public class OutbrainClient extends AbstractPerfClient {

    private final CollectorRegistryExporterFactory collectorRegistryExporterFactory;
    private CollectorRegistryExporter formatter;

    public OutbrainClient(final OutputMode mode, final CollectorRegistryExporterFactory collectorRegistryExporterFactory) {
        super(mode);
        this.collectorRegistryExporterFactory = collectorRegistryExporterFactory;
    }

    @Override
    public void setUp() {
        final MetricCollectorRegistry metricCollectorRegistry = new MetricCollectorRegistry();
        metricCollectorRegistry.register(createCollector());
        formatter = collectorRegistryExporterFactory.create(metricCollectorRegistry);
    }

    @Override
    public void executeLogic(final OutputStream outputStream) throws IOException {
        formatter.export(outputStream);
    }

    private MetricCollector createCollector() {
        final MetricRegistry registry = new MetricRegistry();
        final Counter counter = new Counter.CounterBuilder("Counter" + NAME, HELP)
            .withLabels("label1", "label2")
            .build();
        final Histogram histogram = new Histogram.HistogramBuilder("Histogram" + NAME, HELP)
            .withLabels("label1", "label2")
            .withEqualWidthBuckets(0, 1000, 100)
            .build();

        for (int i = 0; i < 100; i++) {
            counter.inc(i, "val", "val" + i);
            histogram.observe((double)i, "val", "val" + i);
        }
        registry.getOrRegister(counter);
        registry.getOrRegister(histogram);

        return new MetricCollector(registry, emptyMap());
    }
}
