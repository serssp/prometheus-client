package com.outbrain.swinfra.metrics

import com.outbrain.swinfra.metrics.timing.Clock

class TestClock implements Clock {

    private long tick = 0

    long setTick(final long value) {
        this.tick = value
    }

    @Override
    long getTick() {
        return tick
    }
}
