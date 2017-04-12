package com.outbrain.swinfra.metrics.exports

import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.MetricRegistry
import spock.lang.Specification
import spock.lang.Subject

import java.lang.management.ThreadMXBean

class ThreadMetricsTest extends Specification {

    private ThreadMXBean threadsBean = Mock(ThreadMXBean)

    @Subject
    private ThreadMetrics threadMetrics;

    def 'verify metrics registered correctly'() {
        given:
            threadMetrics = new ThreadMetrics(threadsBean)
            threadsBean.getThreadCount() >> 300
            threadsBean.getDaemonThreadCount() >> 200
            threadsBean.getPeakThreadCount() >> 301
            threadsBean.getTotalStartedThreadCount() >> 503L
            threadsBean.findDeadlockedThreads() >> ([11L, 22L, 33L] as long[])
            threadsBean.findMonitorDeadlockedThreads() >> ([12L, 13L, 14L] as long[])
        when:
            MetricCollector collector = new MetricCollector(threadMetrics.registerMetricsTo(new MetricRegistry()))
        then:
            collector.find { it.name == 'jvm_threads_current' }.getValue() == 300
            collector.find { it.name == 'jvm_threads_daemon' }.getValue() == 200
            collector.find { it.name == 'jvm_threads_peak' }.getValue() == 301
            collector.find { it.name == 'jvm_threads_started_total' }.getValue() == 503
            collector.find { it.name == 'jvm_threads_deadlocked' }.getValue() == 3
            collector.find { it.name == 'jvm_threads_deadlocked_monitor' }.getValue() == 3
    }
}
