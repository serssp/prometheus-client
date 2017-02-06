package com.outbrain.swinfra.metrics.timing

import com.outbrain.swinfra.metrics.TestClock
import spock.lang.Specification

import java.util.function.LongConsumer

class TimerTest extends Specification {

    private final TestClock clock = new TestClock()
    private final SummingLongConsumer consumer = new SummingLongConsumer()

    def 'Timer should emit one value when started and stopped'() {
        final long tick = 1

        given:
            final Timer timer = new Timer(clock, consumer)
            clock.setTick(tick)

        when:
            timer.stop()

        then:
            consumer.sum == tick
    }

    private static class SummingLongConsumer implements LongConsumer {
        private long sum = 0

        long getSum() {
            return sum
        }

        @Override
        void accept(final long value) {
            sum += value
        }
    }
}
