package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.Timer.TimerContext
import spock.lang.Specification

import static io.prometheus.client.Collector.MetricFamilySamples
import static io.prometheus.client.Collector.MetricFamilySamples.Sample
import static io.prometheus.client.Collector.Type.SUMMARY
import static java.util.Collections.emptyList
import static java.util.Collections.singletonList

class TimerTest extends Specification {

    private static final int SUM_1_TO_1000 = 500500
    private static final String NAME = "NAME"
    private static final String SUM_NAME = NAME + "_sum"
    private static final String COUNT_NAME = NAME + "_count"
    private static final String HELP = "HELP"
    private static final List<String> QUANTILE_LABEL = singletonList("quantile")

    final MetricRegistry metricRegistry = new MetricRegistry();

    def 'Timer should return correct samples for newly initialized metric'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 0),
                sampleForQuantile("0.75", 0),
                sampleForQuantile("0.95", 0),
                sampleForQuantile("0.98", 0),
                sampleForQuantile("0.99", 0),
                sampleForQuantile("0.999", 0),
                new Sample(COUNT_NAME, emptyList(), emptyList(), 0),
                new Sample(SUM_NAME, emptyList(), emptyList(), 0)
            ]
            final List<MetricFamilySamples> metricFamilySamples = [
                new MetricFamilySamples(NAME,SUMMARY , HELP, samples)
            ]

        when:
            final Timer timer = new Timer.TimerBuilder(NAME, HELP, metricRegistry).register();

        then:
            timer.getSamples().sort() == metricFamilySamples.sort()
    }

    def 'Timer should return correct samples after 1000 measurements'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 500),
                sampleForQuantile("0.75", 750),
                sampleForQuantile("0.95", 950),
                sampleForQuantile("0.98", 980),
                sampleForQuantile("0.99", 990),
                sampleForQuantile("0.999", 999),
                new Sample(COUNT_NAME, emptyList(), emptyList(), 1000),
                new Sample(SUM_NAME, emptyList(), emptyList(), SUM_1_TO_1000)
            ]
            final List<MetricFamilySamples> metricFamilySamples = [
                new MetricFamilySamples(NAME,SUMMARY , HELP, samples)
            ]

            final MyClock testClock = new MyClock()

        when:
            final MyTimer timer = new MyTimer(NAME, HELP, testClock)
            timer.initChildMetricRepo()

            1.upto(1000, {
                //Set the clock to 0 before starting the timer, and set it to the current element before stopping the timer
                //This way the measurements will be 1,2,3,...,1000
                testClock.setTick(0)
                final TimerContext context = timer.startTimer()
                testClock.setTick(it)
                context.stop()
            })

        then:
            timer.getSamples().sort() == metricFamilySamples.sort()
    }

    private static Sample sampleForQuantile(final String quantile, final double value) {
        return new Sample(NAME, QUANTILE_LABEL, [quantile], value)
    }

}
