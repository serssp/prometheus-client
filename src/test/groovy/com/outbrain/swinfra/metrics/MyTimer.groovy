package com.outbrain.swinfra.metrics

import com.codahale.metrics.ExponentiallyDecayingReservoir
import com.outbrain.swinfra.metrics.children.ChildMetricRepo
import com.outbrain.swinfra.metrics.children.LabeledChildrenRepo
import com.outbrain.swinfra.metrics.children.MetricData
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo

import java.util.function.Function

import static com.outbrain.swinfra.metrics.LabelUtils.commaDelimitedStringToLabels

class MyTimer extends Timer {

    private final MyClock clock;

    public MyTimer(final String name, final String help, final MyClock myClock, final List<String> labelNames = []) {
        super(name, help, labelNames as String[])
        this.clock = myClock
    }

    @Override
    ChildMetricRepo<com.codahale.metrics.Timer> createChildMetricRepo() {
        if (getLabelNames().size() == 0) {
            return new UnlabeledChildRepo<>(new MetricData<>(createTimer(), [] as String[]))
        } else {
            return new LabeledChildrenRepo<>({ final commaDelimitedLabelValues ->
                final String[] labelValues = commaDelimitedStringToLabels(commaDelimitedLabelValues)
                return new MetricData<>(createTimer(), labelValues)
            } as Function);
        }
    }

    private com.codahale.metrics.Timer createTimer() {
        new com.codahale.metrics.Timer(new ExponentiallyDecayingReservoir(), clock)
    }
}