package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.Timer.TimerContext
import com.outbrain.swinfra.metrics.samples.SampleCreator
import com.outbrain.swinfra.metrics.samples.StaticLablesSampleCreator
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static io.prometheus.client.Collector.MetricFamilySamples
import static io.prometheus.client.Collector.MetricFamilySamples.Sample
import static io.prometheus.client.Collector.Type.SUMMARY

class TimerTest extends Specification {

    private static final int SUM_1_TO_1000 = 500500
    private static final SampleCreator sampleCreator = new StaticLablesSampleCreator([:])
    private static final String NAME = "NAME"
    private static final String SUM_NAME = NAME + "_sum"
    private static final String COUNT_NAME = NAME + "_count"
    private static final String HELP = "HELP"
    private static final List<String> QUANTILE_LABEL = ["quantile"]

    def 'Timer should return correct samples for newly initialized metric'() {
        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 0),
                sampleForQuantile("0.75", 0),
                sampleForQuantile("0.95", 0),
                sampleForQuantile("0.98", 0),
                sampleForQuantile("0.99", 0),
                sampleForQuantile("0.999", 0),
                new Sample(COUNT_NAME, [], [], 0),
                new Sample(SUM_NAME, [], [], 0)
            ]
            final MetricFamilySamples metricFamilySamples = new MetricFamilySamples(NAME, SUMMARY, HELP, samples)

        when:
            final Timer timer = new Timer.TimerBuilder(NAME, HELP).build()

        then:
            timer.getSample(sampleCreator) == metricFamilySamples
    }

    def 'Timer should return correct samples for newly initialized metric with labels'() {
        final List<String> labelNames = ["label1", "label2"]
        final List<String> labelValues = ["value1", "value2"]

        given:
            final List<Sample> samples = [
                sampleForQuantile("0.5", 500, labelNames, labelValues),
                sampleForQuantile("0.75", 750, labelNames, labelValues),
                sampleForQuantile("0.95", 950, labelNames, labelValues),
                sampleForQuantile("0.98", 980, labelNames, labelValues),
                sampleForQuantile("0.99", 990, labelNames, labelValues),
                sampleForQuantile("0.999", 999, labelNames, labelValues),
                new Sample(COUNT_NAME, labelNames, labelValues, 1000),
                new Sample(SUM_NAME, labelNames, labelValues, SUM_1_TO_1000)
            ]
            final MetricFamilySamples metricFamilySamples = new MetricFamilySamples(NAME, SUMMARY, HELP, samples)

            final MyClock testClock = new MyClock()
        when:
            final MyTimer timer = new MyTimer(NAME, HELP, testClock, labelNames)
            timer.initChildMetricRepo()

            (1..1000).each {
                //Set the clock to 0 before starting the timer, and set it to the current element before stopping the timer
                //This way the measurements will be 1,2,3,...,1000
                testClock.setTick(0)
                final TimerContext context = timer.startTimer(labelValues as String[])
                testClock.setTick(it)
                context.stop()
            }

        then:
            timer.getSample(sampleCreator) == metricFamilySamples
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
                new Sample(COUNT_NAME, [], [], 1000),
                new Sample(SUM_NAME, [], [], SUM_1_TO_1000)
            ]
            final MetricFamilySamples metricFamilySamples = new MetricFamilySamples(NAME, SUMMARY, HELP, samples)

            final MyClock testClock = new MyClock()

        when:
            final MyTimer timer = new MyTimer(NAME, HELP, testClock)
            timer.initChildMetricRepo()

            (1..1000).each {
                //Set the clock to 0 before starting the timer, and set it to the current element before stopping the timer
                //This way the measurements will be 1,2,3,...,1000
                testClock.setTick(0)
                final TimerContext context = timer.startTimer()
                testClock.setTick(it)
                context.stop()
            }

        then:
            timer.getSample(sampleCreator) == metricFamilySamples
    }

    def 'Timer should return convert sample to requested units'() {
        final int measurement = 10

        given:
            final MyClock testClock = new MyClock()

        when:
            final MyTimer timer = new MyTimer(NAME, HELP, testClock, [], TimeUnit.MILLISECONDS)
            timer.initChildMetricRepo()
            testClock.setTick(0)
            final TimerContext context = timer.startTimer()
            testClock.setTick(TimeUnit.MILLISECONDS.toNanos(measurement)) //Measure 10 milliseconds
            context.stop()

        then:
            final Sample sumSample = timer.getSample(sampleCreator).samples.find {it.name.endsWith("sum")}
            sumSample.value == measurement
    }

    private static Sample sampleForQuantile(final String quantile,
                                            final double value,
                                            final List<String> extraLabelNames = [],
                                            final List<String> extraLabelValues = []) {
        return new Sample(
            NAME,
            (extraLabelNames + QUANTILE_LABEL) as List,
            (extraLabelValues + [quantile]) as List,
            value)
    }

}
