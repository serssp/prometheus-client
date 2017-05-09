package com.outbrain.swinfra.metrics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface PerfTestClient {

    void setUp();

    byte[] simulateEndpoint();

    void executeLogic(OutputStream outputStream) throws IOException;

    ByteArrayOutputStream createStreamForTest();

    void verify(byte[] result);

}

