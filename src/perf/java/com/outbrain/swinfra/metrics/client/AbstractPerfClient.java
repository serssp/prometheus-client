package com.outbrain.swinfra.metrics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.outbrain.swinfra.metrics.client.OutputMode.MAX_OUTPUT_BUFFER_LENGTH;

abstract class AbstractPerfClient implements PerfTestClient {

    static final String NAME = "NAME";
    static final String HELP = "HELP";

    private final OutputMode mode;

    protected AbstractPerfClient(final OutputMode mode) {
        this.mode = mode;
    }

    @Override
    public byte[] simulateEndpoint() {
        try (final ByteArrayOutputStream outputStream = createStreamForTest()) {
            executeLogic(outputStream);
            return outputStream.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteArrayOutputStream createStreamForTest() {
        return new ByteArrayOutputStream(MAX_OUTPUT_BUFFER_LENGTH);
    }

    @Override
    public void verify(final byte[] actual) {
        mode.verify(actual);
    }
}
