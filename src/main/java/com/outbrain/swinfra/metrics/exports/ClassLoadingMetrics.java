package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.function.Predicate;

public class ClassLoadingMetrics extends MetricsRegistrar {

    private final ClassLoadingMXBean classLoadingMXBean;

    public ClassLoadingMetrics() {
        this(ManagementFactory.getClassLoadingMXBean());
    }

    ClassLoadingMetrics(final ClassLoadingMXBean classLoadingMXBean) {
        this.classLoadingMXBean = classLoadingMXBean;
    }

    @Override
    public MetricRegistry registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_classes_loaded", "The number of classes that are currently loaded in the JVM").
                withValueSupplier(classLoadingMXBean::getLoadedClassCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_classes_loaded_total", "The total number of classes that have been loaded since the JVM has started execution").
                withValueSupplier(classLoadingMXBean::getTotalLoadedClassCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_classes_unloaded_total", "The total number of classes that have been unloaded since the JVM has started execution").
                withValueSupplier(classLoadingMXBean::getUnloadedClassCount).build(), registry, nameFilter);
        return registry;
    }
}
