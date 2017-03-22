package com.outbrain.swinfra.metrics.client;

import com.outbrain.swinfra.metrics.Counter;
import com.outbrain.swinfra.metrics.Histogram;
import com.outbrain.swinfra.metrics.MetricCollector;
import com.outbrain.swinfra.metrics.MetricCollectorRegistry;
import com.outbrain.swinfra.metrics.MetricRegistry;
import com.outbrain.swinfra.metrics.exporter.CollectorRegistryExporter;
import com.outbrain.swinfra.metrics.exporter.CollectorRegistryExporterFactory;

import java.io.IOException;
import java.io.Writer;

import static java.util.Collections.emptyMap;

public class OutbrainClient extends AbstractClient {

    private CollectorRegistryExporter formatter;

    @Override
    public void setUp() {
        super.setUp();
        final MetricCollectorRegistry metricCollectorRegistry = new MetricCollectorRegistry();
        metricCollectorRegistry.register(createCollector());
        formatter = CollectorRegistryExporterFactory.TEXT_004.create(metricCollectorRegistry);
    }

    @Override
    public void executeLogic(final Writer writer) throws IOException {
        formatter.export(writer);
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
