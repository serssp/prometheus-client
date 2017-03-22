package com.outbrain.swinfra.metrics.exporter

import com.outbrain.swinfra.metrics.MetricCollector
import com.outbrain.swinfra.metrics.MetricCollectorRegistry
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Function


class CollectorRegistryExporterTest extends Specification {

    @Subject
    CollectorRegistryExporter formatter

    private MetricCollector collector1 = Mock(MetricCollector)
    private MetricCollector collector2 = Mock(MetricCollector)
    private MetricCollector collector3 = Mock(MetricCollector)
    private CollectorExporter collectorFormatter1 = Mock(CollectorExporter)
    private CollectorExporter collectorFormatter2 = Mock(CollectorExporter)
    private CollectorExporter collectorFormatter3 = Mock(CollectorExporter)
    private StringBuilder output
    private MetricCollectorRegistry registry

    def setup() {
        registry = new MetricCollectorRegistry()
        registry.register(collector1)
        registry.register(collector2)
        registry.register(collector3)
        Closure creator = {
            switch (it) {
                case collector1: collectorFormatter1
                    break
                case collector2: collectorFormatter2
                    break
                case collector3: collectorFormatter3
                    break}
        }

        formatter = new CollectorRegistryExporter(this.registry, creator as Function)
        this.output = new StringBuilder()
    }


    def 'should format collectors to the given stream using generated formatters'() {
        when:
            formatter.export(output)
        then:
            1 * collectorFormatter1.exportTo(output)
            1 * collectorFormatter2.exportTo(output)
            1 * collectorFormatter3.exportTo(output)
    }

    def 'should store formatter per collector and reuse them allowing to cache content'() {
        given:
            Function mockCreator = Mock(Function)
            formatter = new CollectorRegistryExporter(registry, mockCreator)
        when:
            formatter.export(output)
            formatter.export(output)
        then:
            1 * mockCreator.apply(collector1) >> collectorFormatter1
            1 * mockCreator.apply(collector2) >> collectorFormatter2
            1 * mockCreator.apply(collector3) >> collectorFormatter3
            2 * collectorFormatter1.exportTo(output)
            2 * collectorFormatter2.exportTo(output)
            2 * collectorFormatter3.exportTo(output)
    }
}
