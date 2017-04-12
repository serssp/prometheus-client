package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ThreadMetrics implements MetricsRegistrar {

    private final ThreadMXBean threadBean;

    public ThreadMetrics() {
        this(ManagementFactory.getThreadMXBean());
    }

    ThreadMetrics(final ThreadMXBean threadBean) {
        this.threadBean = threadBean;
    }

    @Override
    public MetricRegistry registerMetricsTo(final MetricRegistry registry) {
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_current", "Current thread count of a JVM").
                withValueSupplier(threadBean::getThreadCount).build());
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_daemon", "Daemon thread count of a JVM").
                withValueSupplier(threadBean::getDaemonThreadCount).build());
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_peak", "Peak thread count of a JVM").
                withValueSupplier(threadBean::getPeakThreadCount).build());
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_started_total", "Started thread count of a JVM").
                withValueSupplier(threadBean::getTotalStartedThreadCount).build());
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_deadlocked", "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers").
                withValueSupplier(() -> safeArrayLength(threadBean.findDeadlockedThreads())).build());
        registry.getOrRegister(
            new Gauge.GaugeBuilder("jvm_threads_deadlocked_monitor", "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors"    ).
                withValueSupplier(() -> safeArrayLength(threadBean.findMonitorDeadlockedThreads())).build());
        return registry;
    }

    private static int safeArrayLength(final long[] array) {
        return (array == null) ? 0 : array.length;
    }

}
