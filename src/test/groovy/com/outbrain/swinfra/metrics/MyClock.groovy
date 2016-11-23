package com.outbrain.swinfra.metrics

import com.codahale.metrics.Clock

class MyClock extends Clock {

    private long tick;

    void setTick(final long value) {
        this.tick = value
    }

    @Override
    long getTick() {
        return tick
    }

}
