package com.outbrain.swinfra.metrics

import com.codahale.metrics.ExponentiallyDecayingReservoir
import com.outbrain.swinfra.metrics.children.ChildMetricRepo
import com.outbrain.swinfra.metrics.children.MetricData
import com.outbrain.swinfra.metrics.children.UnlabeledChildRepo

class MyTimer extends Timer {

    private final MyClock clock;

    public MyTimer(final String name, final String help, final MyClock myClock) {
        super(name, help, [])
        this.clock = myClock
    }

    @Override
    ChildMetricRepo<com.codahale.metrics.Timer> createChildMetricRepo() {
        return new UnlabeledChildRepo<>(
            new MetricData<>(new com.codahale.metrics.Timer(new ExponentiallyDecayingReservoir(), clock), [] as String[]))
    }
}