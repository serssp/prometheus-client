package com.outbrain.swinfra.metrics.exports

import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.MetricRegistry
import com.sun.management.UnixOperatingSystemMXBean
import spock.lang.Specification
import spock.lang.Subject

import java.lang.management.RuntimeMXBean

class StandardMetricsTest extends Specification {


    @Subject
    StandardMetrics standardMetrics

    StandardMetrics.StatusReader statusReader = Mock(StandardMetrics.StatusReader)
    UnixOperatingSystemMXBean osBean = Mock(UnixOperatingSystemMXBean)
    RuntimeMXBean runtimeBean = Mock(RuntimeMXBean)

    def setup() {
        osBean.getName() >> 'Linux'
        standardMetrics = new StandardMetrics(statusReader, osBean, runtimeBean)
    }

    def 'verify metrics registered correctly'() {
        given:
            osBean.getProcessCpuTime() >> 123L * 1.0E9
            osBean.getOpenFileDescriptorCount() >> 10L
            osBean.getMaxFileDescriptorCount() >> 20L
            runtimeBean.getStartTime() >> 456L * 1.0E3
            statusReader.readProcSelfStatus() >> new StandardMetrics.StatusParser('Name:   cat\nVmSize:\t5900 kB\nVmRSS:\t   360 kB\n')
        when:
            MetricCollector collector = new MetricCollector(standardMetrics.registerMetricsTo(new MetricRegistry()))
        then:
            collector.find { it.name == 'process_cpu_seconds_total' }.getValue() == 123
            collector.find { it.name == 'process_open_fds' }.getValue() == 10
            collector.find { it.name == 'process_max_fds' }.getValue() == 20
            collector.find { it.name == 'process_start_time_seconds' }.getValue() == 456
            collector.find { it.name == 'process_virtual_memory_bytes' }.getValue() == 5900 * 1024
            collector.find { it.name == 'process_resident_memory_bytes' }.getValue() == 360 * 1024
    }

    def 'broken proc status fails gracefully'() {
        given:
            osBean.getProcessCpuTime() >> 123L * 1.0E9
            statusReader.readProcSelfStatus() >> new StandardMetrics.StatusParser('Name:   cat\nVmSize:\n')
        when:
            MetricCollector collector = new MetricCollector(standardMetrics.registerMetricsTo(new MetricRegistry()))
        then:
            collector.find { it.name == 'process_cpu_seconds_total' }.getValue() == 123
            collector.find { it.name == 'process_virtual_memory_bytes' }.getValue() == 0
            collector.find { it.name == 'process_resident_memory_bytes' }.getValue() == 0

    }
}
