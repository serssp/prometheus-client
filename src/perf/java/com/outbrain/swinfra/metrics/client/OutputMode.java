package com.outbrain.swinfra.metrics.client;

import io.prometheus.client.Metrics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public enum OutputMode {

    TEXT() {
        @Override
        public void verify(final byte[] actual) {
            try {
                final byte[] expected = getExpectedText();
                final String[] expectedLines = new String(expected, "UTF-8").split("\n");
                final String[] actualLines = new String(actual, "UTF-8").split("\n");
                Arrays.sort(expectedLines);
                Arrays.sort(actualLines);
                if (!Arrays.equals(expectedLines, actualLines)) {
                    printDebugInfo(expectedLines, actualLines);
                    throw new RuntimeException("Unexpected output");
                }
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException("world gone mad", e);
            }
        }

        private byte[] getExpectedText() {
            try {
                return  Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("PublishMetricsTestOutput.txt").toURI()));
            } catch (IOException | URISyntaxException e) {
                throw new IllegalStateException("Failed to read expected file", e);
            }
        }
    },
    PROTOBUF() {
        @Override
        public void verify(final byte[] actual) {
            try {
                final List<Metrics.MetricFamily> actualFamilies = new ArrayList<>();
                final ByteArrayInputStream serialized = new ByteArrayInputStream(actual);
                while (serialized.available() > 0) {
                    actualFamilies.add(Metrics.MetricFamily.parseDelimitedFrom(serialized));
                }
                final List<Metrics.MetricFamily> expectedFamilies = getExpectedProtobufObjects();

                if (expectedFamilies.size() == actualFamilies.size()) {
                    for (final Metrics.MetricFamily expectedFamily : expectedFamilies) {
                        final Metrics.MetricFamily actualFamily = getActualFamily(actualFamilies, expectedFamily.getName());
                        compare(expectedFamily.getType(), actualFamily.getType());
                        compare(expectedFamily.getHelp(), actualFamily.getHelp());
                        compareMetricList(expectedFamily.getMetricList(), actualFamily.getMetricList(), expectedFamily.getType());
                    }
                }
                else {
                    throw new RuntimeException("Unexpected output " + expectedFamilies.size());
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void compareMetricList(final List<Metrics.Metric> expected, final List<Metrics.Metric> actual, final Metrics.MetricType type) {
            if (expected.size() != actual.size()) {
                throw new RuntimeException("Unexpected output" + actual);
            }
            for (final Metrics.Metric expectedMetric : expected) {
                final List<Metrics.LabelPair> expectedLabels = expectedMetric.getLabelList();
                final Metrics.Metric actualMetric = getActualMetric(actual, expectedLabels);
                switch (type) {
                    case COUNTER:
                        compare(expectedMetric.getCounter(), actualMetric.getCounter());
                        break;
                    case HISTOGRAM:
                        compare(expectedMetric.getHistogram(), actualMetric.getHistogram());
                        break;
                     default:
                         throw new RuntimeException("Unexpected output " + type);
                }
            }
        }

        private void compare(final Metrics.Histogram expected, final Metrics.Histogram actual) {
            if (expected.getSampleCount() != actual.getSampleCount()) {
                throw new RuntimeException("Unexpected output " + actual);
            }
            if (expected.getSampleSum() != actual.getSampleSum()) {
                throw new RuntimeException("Unexpected output " + actual);
            }
            compareBucketList(expected.getBucketList(), actual.getBucketList());
        }

        private void compareBucketList(final List<Metrics.Bucket> expected, final List<Metrics.Bucket> actual) {
            if (!equalListElements(expected, actual)) {
                throw new RuntimeException("Unexpected output " + actual);
            }
        }

        private void compare(final Metrics.Counter expected, final Metrics.Counter actual) {
            if (expected.getValue() != actual.getValue()) {
                throw new RuntimeException("Unexpected output " + actual);
            }
        }

        private Metrics.Metric getActualMetric(final List<Metrics.Metric> actual, final List<Metrics.LabelPair> expectedLabels) {
            final Optional<Metrics.Metric> metricOptional = actual.stream().filter((m) -> equalListElements(expectedLabels, m.getLabelList())).findFirst();
            return metricOptional.orElseThrow(() -> new RuntimeException("Unexpected Output. Failed to find metric " + expectedLabels));
        }

        private <T> boolean equalListElements(final List<T> expected, final List<T> actual) {
            return new HashSet<>(expected).equals(new HashSet<>(actual));
        }

        private void compare(final Metrics.MetricType expected, final Metrics.MetricType actual) {
            if (expected != actual) {
                throw new RuntimeException("Unexpected output " + actual);
            }
        }

        private void compare(final String expected, final String actual) {
            if (!expected.equals(actual)) {
                throw new RuntimeException("Unexpected output " + actual);
            }
        }

        private Metrics.MetricFamily getActualFamily(final List<Metrics.MetricFamily> actualFamilies, final String name) {
            final Optional<Metrics.MetricFamily> actualOptional = actualFamilies.stream().filter((f) -> name.equals(f.getName())).findFirst();
            return actualOptional.orElseThrow(() -> new RuntimeException("Unexpected output. Failed to find " + name));
        }

        private List<Metrics.MetricFamily> getExpectedProtobufObjects() {
            final List<Metrics.MetricFamily> families = new ArrayList<>();
            final Metrics.MetricFamily.Builder counterFamilyBuilder = Metrics.MetricFamily.newBuilder().setName("CounterNAME").setHelp("HELP").setType(Metrics.MetricType.COUNTER);
            final Metrics.MetricFamily.Builder histogramFamilyBuilder = Metrics.MetricFamily.newBuilder().setName("HistogramNAME").setHelp("HELP").setType(Metrics.MetricType.HISTOGRAM);
            for (int i = 0; i < 100; i++) {
                counterFamilyBuilder.addMetric(
                    Metrics.Metric.newBuilder().addLabel(Metrics.LabelPair.newBuilder().setName("label1").setValue("val")).addLabel(Metrics.LabelPair.newBuilder().setName("label2").setValue("val" + i)).
                        setCounter(Metrics.Counter.newBuilder().setValue(i)));
                final Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder().setSampleCount(1).setSampleSum(i);
                histogramBuilder.addBucket(Metrics.Bucket.newBuilder().setCumulativeCount((i == 0) ? 1 : 0).setUpperBound(0.0));
                for (int j = 1; j <= 99; j++) {
                    histogramBuilder.addBucket(Metrics.Bucket.newBuilder().setCumulativeCount(1).setUpperBound(j * 1000.0));
                }
                histogramBuilder.addBucket(Metrics.Bucket.newBuilder().setCumulativeCount(1).setUpperBound(Double.POSITIVE_INFINITY));
                histogramFamilyBuilder.addMetric(
                    Metrics.Metric.newBuilder().addLabel(Metrics.LabelPair.newBuilder().setName("label1").setValue("val")).addLabel(Metrics.LabelPair.newBuilder().setName("label2").setValue("val" + i)).
                        setHistogram(histogramBuilder));
            }
            families.add(counterFamilyBuilder.build());
            families.add(histogramFamilyBuilder.build());
            return families;
        }
    };

    public abstract void verify(final byte[] actual);

    private static void printDebugInfo(final String[] expectedLines, final String[] actualLines) {
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
    }

    public static int MAX_OUTPUT_BUFFER_LENGTH = 699649; // length of PublishMetricsTestOutput.txt + 1
}
