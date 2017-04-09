package com.outbrain.swinfra.metrics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface PerfTestClient {

    String createExpectedResult();

    void setUp();

    String simulateEndpoint();

    void executeLogic(OutputStream outputStream) throws IOException;

    ByteArrayOutputStream createStreamForTest();

    void verify(String result);
}
