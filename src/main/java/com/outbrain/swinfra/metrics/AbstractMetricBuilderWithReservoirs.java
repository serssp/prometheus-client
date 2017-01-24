package com.outbrain.swinfra.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * An abstract builder for metrics that can use different types of reservoirs for saving the measurements.
 * <p>
 *   Reservoirs are a method for saving measurements of possibly infinite streams without consuming an infinite amount of memory
 * </p>
 */
public abstract class AbstractMetricBuilderWithReservoirs<T extends AbstractMetric, B extends AbstractMetricBuilderWithReservoirs<T, B>> extends AbstractMetricBuilder<T, B> {

  private Supplier<Reservoir> reservoirSupplier = ExponentiallyDecayingReservoir::new;

  AbstractMetricBuilderWithReservoirs(final String name, final String help) {
    super(name, help);
  }

  Supplier<Reservoir> getReservoirSupplier() {
    return reservoirSupplier;
  }

  public ReservoirBuilder withReservoir() {
    return new ReservoirBuilder();
  }

  private B withReservoir(final Supplier<Reservoir> reservoirSupplier) {
    this.reservoirSupplier = reservoirSupplier;
    return getThis();
  }

  public class ReservoirBuilder {

    /**
     * Create this summary with an exponentially decaying reservoir - a reservoir that gives a lower
     * importance to older measurements.
     *
     * @param size  the size of the reservoir - the number of measurements that will be saved
     * @param alpha the exponential decay factor. The higher this is the more biased the reservoir will
     *              be towards newer measurements.
     * @see <a href="http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf">
     */
    public B withExponentiallyDecayingReservoir(final int size, final double alpha) {
      return withReservoir(() -> new ExponentiallyDecayingReservoir(size, alpha));
    }

    /**
     * Create this summary with a sliding time window reservoir. This reservoir keeps the measurements made in the
     * last {@code window} seconds (or other time unit).
     *
     * @param window     the window to save
     * @param windowUnit the window's time units
     */
    public B withSlidingTimeWindowReservoir(final int window, final TimeUnit windowUnit) {
      return withReservoir(() -> new SlidingTimeWindowReservoir(window, windowUnit));
    }

    /**
     * Create this summary with a sliding window reservoir. This reservoir keeps a constant amount of the last
     * measurements and is therefore memory-bound.
     *
     * @param size the number of measurements to save
     */
    public B withSlidingWindowReservoir(final int size) {
      return withReservoir(() -> new SlidingWindowReservoir(size));
    }


    /**
     * Create this summary with a uniform reservoir - a reservoir that randomally saves the measurements and is
     * statistically representative of all measurements.
     *
     * @param size the size of the reservoir - the number of measurements that will be saved
     * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
     */
    public B withUniformReservoir(final int size) {
      return withReservoir(() -> new UniformReservoir(size));
    }
  }

}
