package com.outbrain.swinfra.metrics

import com.codahale.metrics.ExponentiallyDecayingReservoir
import com.codahale.metrics.Reservoir
import com.outbrain.swinfra.metrics.children.ChildMetricRepo
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo
import com.outbrain.swinfra.metrics.children.MetricData
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo

import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.Supplier

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels

/**
 * A custom timer implementation in order to be able to provide a custom clock which facilitates testing the timer
 */
class MyTimer extends Timer {

    private final MyClock clock

    MyTimer(final String name,
                   final String help,
                   final MyClock myClock,
                   final List<String> labelNames = [],
                   final TimeUnit measureIn = TimeUnit.NANOSECONDS) {
        super(name, help, labelNames as String[], measureIn, {new ExponentiallyDecayingReservoir()} as Supplier)
        this.clock = myClock
    }

    @Override
    ChildMetricRepo<com.codahale.metrics.Timer> createChildMetricRepo() {
        if (getLabelNames().empty) {
            return new UnlabeledChildRepo<>(new MetricData<>(createTimer(), [] as String[]))
        } else {
            return new LabeledChildrenRepo<>({ final commaDelimitedLabelValues ->
                final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues)
                return new MetricData<>(createTimer(), labelValues)
            } as Function)
        }
    }

    private com.codahale.metrics.Timer createTimer() {
        new com.codahale.metrics.Timer(new ExponentiallyDecayingReservoir(), clock)
    }
}