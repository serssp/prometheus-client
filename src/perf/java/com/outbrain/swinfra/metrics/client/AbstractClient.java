package com.outbrain.swinfra.metrics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

abstract class AbstractClient implements PerfTestClient {

    static final String NAME = "NAME";
    static final String HELP = "HELP";

    private String expected;

    @Override
    public void setUp() {
        expected = createExpectedResult();
    }

    @Override
    public String createExpectedResult() {
        try {
            return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("PublishMetricsTestOutput.txt").toURI())), "UTF-8");
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to read expected file", e);
        }
    }

    @Override
    public String simulateEndpoint() {
        try (final ByteArrayOutputStream outputStream = createStreamForTest()) {
            executeLogic(outputStream);
            return outputStream.toString("UTF-8");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteArrayOutputStream createStreamForTest() {
        return new ByteArrayOutputStream(expected.length() + 1);
    }

    @Override
    public void verify(final String actual) {
        final String[] expectedLines = expected.split("\n");
        final String[] actualLines = actual.split("\n");
        Arrays.sort(expectedLines);
        Arrays.sort(actualLines);
        if (!Arrays.equals(expectedLines, actualLines)) {
            System.out.println("=============");
            for (int i = 0; i < Math.max(expectedLines.length, actualLines.length); i++) {
                if (i < expectedLines.length && i < actualLines.length) {
                    if (!expectedLines[i].equals(actualLines[i])) {
                        System.out.println("expected: " + expectedLines[i]);
                        System.out.println("output  : " + actualLines[i]);
                    }
                }
                else if (i < expectedLines.length) {
                    System.out.println("expected: " + expectedLines[i]);
                    System.out.println("output  : <EOF>");
                }
                else if (i < actualLines.length) {
                    System.out.println("expected: <EOF>");
                    System.out.println("output  : " + actualLines[i]);
                }
            }
            System.out.println("=============");
            throw new RuntimeException("Unexpected output");
        }
    }
}
