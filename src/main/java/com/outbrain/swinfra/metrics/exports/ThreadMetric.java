package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.function.Predicate;

public class ThreadMetric extends MetricRegistrar {

    private final ThreadMXBean threadBean;

    public ThreadMetric() {
        this(ManagementFactory.getThreadMXBean());
    }

    ThreadMetric(final ThreadMXBean threadBean) {
        this.threadBean = threadBean;
    }

    @Override
    public void registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_current", "Current thread count of a JVM").
                withValueSupplier(threadBean::getThreadCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_daemon", "Daemon thread count of a JVM").
                withValueSupplier(threadBean::getDaemonThreadCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_peak", "Peak thread count of a JVM").
                withValueSupplier(threadBean::getPeakThreadCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_started_total", "Started thread count of a JVM").
                withValueSupplier(threadBean::getTotalStartedThreadCount).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_deadlocked", "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers").
                withValueSupplier(() -> safeArrayLength(threadBean.findDeadlockedThreads())).build(), registry, nameFilter);
        optionallyRegister(
            new Gauge.GaugeBuilder("jvm_threads_deadlocked_monitor", "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors"    ).
                withValueSupplier(() -> safeArrayLength(threadBean.findMonitorDeadlockedThreads())).build(), registry, nameFilter);
    }

    private static int safeArrayLength(final long[] array) {
        return (array == null) ? 0 : array.length;
    }

}
