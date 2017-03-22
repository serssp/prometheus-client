package com.outbrain.swinfra.metrics.client;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public interface PerfTestClient {

    String createExpectedResult();

    void setUp();

    String simulateEndpoint();

    void executeLogic(Writer writer) throws IOException;

    StringWriter createStringWriterForTest();

    void verify(String result);
}
