package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class GarbageCollectorMetric extends MetricRegistrar {

    private final List<GarbageCollectorMXBean> garbageCollectors;

    public GarbageCollectorMetric() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    GarbageCollectorMetric(final List<GarbageCollectorMXBean> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }

    @Override
    public void registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        final Gauge.GaugeBuilder countGaugeBuilder = new Gauge.GaugeBuilder("jvm_gc_collection_count", "Number of JVM garbage collections.").withLabels("gc");
        final Gauge.GaugeBuilder timeGaugeBuilder = new Gauge.GaugeBuilder("jvm_gc_collection_seconds", "Time spent in a given JVM garbage collector in seconds.").withLabels("gc");
        for (final GarbageCollectorMXBean garbageCollector : garbageCollectors) {
            timeGaugeBuilder.withValueSupplier(() -> TimeUnit.MILLISECONDS.toSeconds(garbageCollector.getCollectionTime()), garbageCollector.getName());
            countGaugeBuilder.withValueSupplier(garbageCollector::getCollectionCount, garbageCollector.getName());
        }

        optionallyRegister(countGaugeBuilder.build(), registry, nameFilter);
        optionallyRegister(timeGaugeBuilder.build(), registry, nameFilter);
    }
}
