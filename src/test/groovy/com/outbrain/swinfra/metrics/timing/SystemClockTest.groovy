package com.outbrain.swinfra.metrics.timing

import spock.lang.Specification
import spock.lang.Unroll

import static java.util.concurrent.TimeUnit.*

class SystemClockTest extends Specification {

    @Unroll
    def 'A system clock measuring in #measurementUnit should return 0 when subtracting tick after tick'() {
        given:
            final Clock clock = new Clock.SystemClock(measurementUnit)

        when:
            final start = clock.getTick()

        then:
            clock.getTick() - start == 0l

        where:
            measurementUnit << [DAYS, HOURS, MINUTES]
    }

    def 'A system clock measuring in nanoseconds should return some value when subtracting tick after tick'() {
        given:
            final Clock clock = new Clock.SystemClock()

        when:
            final start = clock.getTick()

        then:
            clock.getTick() - start > 0l
    }
}
