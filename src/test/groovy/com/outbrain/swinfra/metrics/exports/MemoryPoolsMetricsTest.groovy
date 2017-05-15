package com.outbrain.swinfra.metrics.exports

import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.MetricRegistry
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.lang.management.MemoryMXBean
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage
import java.util.function.Predicate


class MemoryPoolsMetricsTest extends Specification {

    private MemoryMXBean memoryBean = Mock(MemoryMXBean)
    private MemoryPoolMXBean pool1 = Mock(MemoryPoolMXBean)
    private MemoryPoolMXBean pool2 = Mock(MemoryPoolMXBean)


    @Subject
    private MemoryPoolsMetric memoryPoolMetrics = new MemoryPoolsMetric(memoryBean, [pool1, pool2])


    def 'verify metrics registered correctly'() {
        given:
            memoryBean.getHeapMemoryUsage() >> new MemoryUsage(0, 1, 2, 3)
            memoryBean.getNonHeapMemoryUsage() >> new MemoryUsage(0, 4, 5, 6)
            pool1.getUsage() >> new MemoryUsage(0, 11, 12, 13)
            pool2.getUsage() >> new MemoryUsage(0, 21, 22, 23)
            pool1.getName() >> 'pool1'
            pool2.getName() >> 'pool2'
            final MetricRegistry registry = new MetricRegistry()
        when:
            memoryPoolMetrics.registerMetricsTo(registry)
            MetricCollector collector = new MetricCollector(registry)
        then:
            collector.find { it.name == 'jvm_memory_bytes_used' }.getValue('heap') == 1
            collector.find { it.name == 'jvm_memory_bytes_committed' }.getValue('heap') == 2
            collector.find { it.name == 'jvm_memory_bytes_max' }.getValue('heap') == 3
            collector.find { it.name == 'jvm_memory_bytes_used' }.getValue('nonheap') == 4
            collector.find { it.name == 'jvm_memory_bytes_committed' }.getValue('nonheap') == 5
            collector.find { it.name == 'jvm_memory_bytes_max' }.getValue('nonheap') == 6
            collector.find { it.name == 'jvm_memory_pool_bytes_used' }.getValue('pool1') == 11
            collector.find { it.name == 'jvm_memory_pool_bytes_committed' }.getValue('pool1') == 12
            collector.find { it.name == 'jvm_memory_pool_bytes_max' }.getValue('pool1') == 13
            collector.find { it.name == 'jvm_memory_pool_bytes_used' }.getValue('pool2') == 21
            collector.find { it.name == 'jvm_memory_pool_bytes_committed' }.getValue('pool2') == 22
            collector.find { it.name == 'jvm_memory_pool_bytes_max' }.getValue('pool2') == 23
    }

    @Unroll
    def 'filter metrics by name expect #expected'() {
        given:
            final MetricRegistry registry = new MetricRegistry()
        when:
            memoryPoolMetrics.registerMetricsTo(registry, filter as Predicate)
            MetricCollector collector = new MetricCollector(registry)
        then:
            collector.collect {it.name}.sort() == expected
        where:
            filter                                          | expected
            { name -> false }                               | []
            { name -> name == 'jvm_memory_bytes_used' }     | ['jvm_memory_bytes_used']
            { name -> name.startsWith('jvm_memory_bytes') } | ['jvm_memory_bytes_committed', 'jvm_memory_bytes_max', 'jvm_memory_bytes_used']
            { name -> true }                                | ['jvm_memory_bytes_committed', 'jvm_memory_bytes_max', 'jvm_memory_bytes_used', 'jvm_memory_pool_bytes_committed', 'jvm_memory_pool_bytes_max', 'jvm_memory_pool_bytes_used']
    }
}
