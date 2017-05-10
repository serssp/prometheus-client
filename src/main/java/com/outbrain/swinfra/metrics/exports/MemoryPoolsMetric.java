package com.outbrain.swinfra.metrics.exports;

import com.outbrain.swinfra.metrics.Gauge;
import com.outbrain.swinfra.metrics.MetricRegistry;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MemoryPoolsMetric extends MetricRegistrar {


    private final MemoryMXBean memoryBean;
    private final List<MemoryPoolMXBean> poolBeans;

    public MemoryPoolsMetric() {
        this(ManagementFactory.getMemoryMXBean(), ManagementFactory.getMemoryPoolMXBeans());
    }

    public MemoryPoolsMetric(final MemoryMXBean memoryBean, final List<MemoryPoolMXBean> poolBeans) {
        this.memoryBean = memoryBean;
        this.poolBeans = poolBeans;
    }

    @Override
    public void registerMetricsTo(final MetricRegistry registry, final Predicate<String> nameFilter) {
        optionallyRegisterMemoryGague(registry, nameFilter, "jvm_memory_bytes_used", "Used bytes of a given JVM memory area.", MemoryUsage::getUsed);
        optionallyRegisterMemoryGague(registry, nameFilter, "jvm_memory_bytes_committed", "Committed (bytes) of a given JVM memory area.", MemoryUsage::getCommitted);
        optionallyRegisterMemoryGague(registry, nameFilter, "jvm_memory_bytes_max", "Maximum (bytes) of a given JVM memory area.", MemoryUsage::getMax);

        optionallyRegisterMemoryPoolGauge(registry, nameFilter, "jvm_memory_pool_bytes_used", "Used bytes of a given JVM memory pool.", MemoryUsage::getUsed);
        optionallyRegisterMemoryPoolGauge(registry, nameFilter, "jvm_memory_pool_bytes_committed", "Committed (bytes) of a given JVM memory pool.", MemoryUsage::getCommitted);
        optionallyRegisterMemoryPoolGauge(registry, nameFilter, "jvm_memory_pool_bytes_max", "Max (bytes) of a given JVM memory pool.", MemoryUsage::getMax);
    }

    private void optionallyRegisterMemoryPoolGauge(final MetricRegistry registry, final Predicate<String> nameFilter, final String name, final String help, final Function<MemoryUsage, Long> function) {
        final Gauge.GaugeBuilder gaugeBuilder = new Gauge.GaugeBuilder(name, help).withLabels("pool");
        for (final MemoryPoolMXBean poolMXBean : poolBeans) {
            gaugeBuilder.withValueSupplier(() -> function.apply(poolMXBean.getUsage()), poolMXBean.getName());
        }
        optionallyRegister(gaugeBuilder.build(), registry, nameFilter);
    }

    private void optionallyRegisterMemoryGague(final MetricRegistry registry, final Predicate<String> nameFilter, final String name, final String help, final Function<MemoryUsage, Long> function)  {
        optionallyRegister(new Gauge.GaugeBuilder(name, help).
            withLabels("area").
            withValueSupplier(() -> function.apply(memoryBean.getHeapMemoryUsage()), "heap").
            withValueSupplier(() -> function.apply(memoryBean.getNonHeapMemoryUsage()), "nonheap").
            build(), registry, nameFilter);
    }
}
