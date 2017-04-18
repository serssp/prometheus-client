package com.outbrain.swinfra.metrics.exports

import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.MetricRegistry
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.lang.management.ClassLoadingMXBean
import java.util.function.Predicate

class ClassLoadingMetricsTest extends Specification {


    private ClassLoadingMXBean classLoadingBean = Mock(ClassLoadingMXBean)

    @Subject
    private ClassLoadingMetrics classLoadingMetrics = new ClassLoadingMetrics(classLoadingBean)

    def 'verify metrics registered correctly'() {
        given:
            classLoadingBean.getLoadedClassCount() >> 1
            classLoadingBean.getTotalLoadedClassCount() >> 2
            classLoadingBean.getUnloadedClassCount() >> 3
        when:
            MetricCollector collector = new MetricCollector(classLoadingMetrics.registerMetricsTo(new MetricRegistry()))
        then:
            collector.find { it.name == 'jvm_classes_loaded' }.getValue() == 1
            collector.find { it.name == 'jvm_classes_loaded_total' }.getValue() == 2
            collector.find { it.name == 'jvm_classes_unloaded_total' }.getValue() == 3
    }

    @Unroll
    def 'filter metrics by name expect #expected'() {
        when:
            MetricCollector collector = new MetricCollector(classLoadingMetrics.registerMetricsTo(new MetricRegistry(), filter as Predicate))
        then:
            collector.collect {it.name }.sort() == expected
        where:
            filter                                           | expected
                    { name -> false }                        | []
                    { name -> name == 'jvm_classes_loaded' } | ['jvm_classes_loaded']
                    { name -> true }                         | ['jvm_classes_loaded', 'jvm_classes_loaded_total', 'jvm_classes_unloaded_total']
    }
}
