#Prometheus Client
This library is a java client for Prometheus, it implements all 4 Prometheus metrics - Counter, Gauge, Summary and Histogram.

##Table of Contents
* [Background](#background)
* [Getting Started](#getting-started)
* [Measuring](#measuring)
    * [Registering a Metric](#registering-a-metric)
    * [Counter](#counter)
    * [Gauge](#gauge)
    * [Summary](#summary)
    * [Histogram](#histogram)
    * [Timer](#timer)
* [Advanced Usage](#advanced-usage)
    * [Gauge](#gauge---advanced)
    * [Summary](#summary---advanced)
    * [Histogram](#histogram---advanced)
    * [Timer](#timer---advanced)

##Background
When we first started using Prometheus we looked for a client to suite us. Unfortunately Prometheus' own
java client is riddled with *synchronized* methods (See the *CKMSQuantiles* class).

In light of that we decided to write our own client. For most metric implementations we simply wrapped
DropWizard metrics. For the Histogram metric we implemented our own as DropWizard has no support for
a bucket-based histogram.

##Getting Started
Add a maven/gradle dependency:

**Maven**
```xml
<dependency>
    <groupId>com.outbrain.swinfra</groupId>
    <artifactId>prometheus-client</artifactId>
    <version>???</version>
</dependency>
```

**Gradle**
```
com.outbrain.swinfra:prometheus-client:???
```

##Measuring

###Registering a Metric
All metrics should be stored inside a MetricRegistry. The registry is a collection that contains all of
your metric objects and performs validation that no two metrics exist with the same name.
```java
MetricRegistry registry = new MetricRegistry();
```

###Counter
```java
//No labels
Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build());
counter.inc();

//With labels
Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").withLabels("label1")
                                                                           .build());
counter.inc("value1");
```

###Gauge
The gauge takes after the DropWizard's gauge in the sense that it uses a supplier for its value.
This is a gauge that always returns the value "1".

Note that for gauges the label values are determined statically.
```java
//No labels
registry.getOrRegister(new GaugeBuilder("name", "help").withValueSupplier(() -> 1d).build());

//With labels
registry.getOrRegister(new GaugeBuilder("name", "help").withLabels("label1")
                                                       .withValueSupplier(() -> 1d, "value1")
                                                       .build());
```

###Summary
```java
//No labels
Summary summary = registry.getOrRegister(new SummaryBuilder(NAME, HELP).build());
summary.observe(10);

//With labels
Summary summary = registry.getOrRegister(new SummaryBuilder(NAME, HELP).withLabels("label1")
                                                                       .build());
summary.observe(10, "value1");
```

###Histogram
```java
//No labels
Histogram histogram = registry.getOrRegister(new HistogramBuilder(NAME, HELP).build());
histogram.observe(10);

//With labels
Histogram histogram = registry.getOrRegister(new HistogramBuilder(NAME, HELP).withLabels("label1")
                                                                             .build());
histogram.observe(10, "value1");
```

###Timer
```java
//A Summary timer
Summary summaryTimer = registry.getOrRegister(new SummaryBuilder(NAME, HELP).build());
Timer timer = summaeryTimer.startTimer();
//run some code
timer.stop();

//A Histogram timer
Summary histTimer = registry.getOrRegister(new HistogramBuilder(NAME, HELP).build());
Timer timer = histTimer.startTimer();
//run some code
timer.stop();
```

###Reporting to Prometheus
Prometheus uses the *Collector* class to allow a process to expose its metrics. This client implements
a Prometheus *Collector* that serves exactly this purpose. The *MetricCollector* class is instantiated with
a *MetricRegistry*.

```java
//Create a MetricCollector and pass your *MetricRegistry* to it
MetricCollector collector = new MetricCollector(registry);

//The collector may be registered in the Prometheus CollectorRegistry
CollectorRegistry.defaultRegistry.register(collector);

//The default registry can then be exposed using Prometheus' MetricsServlet for example
```

##Advanced Usage
###Gauge - Advanced
```java
registry.getOrRegister(new GaugeBuilder("name", "help").withLabels("label1")
                                                       .withValueSupplier(() -> 1, "value1")
                                                       .withValueSupplier(() -> 2, "value2")
                                                       .
                                                       .
                                                       .build());
```

###Summary - Advanced
The *Summary* metric support the different types of reservoirs that DropWizard supports. They can be used like so:

The default reservoir is the exponentially decaying reservoir.
```java
Summary summary = registry.getOrRegister(new SummaryBuilder("name", "help").withReservoir()
                                                 .withUniformReservoir(100)
                                                 .build());
```

###Histogram - Advanced
The *Histogram* can be configured with custom buckets or with equal width buckets at a given range.
```java
//Custom buckets
Histogram histo = registry.getOrRegister(new HistogramBuilder("name", "help")
                                                .withBuckets(0.1, 0.2, 0.5, 1.0)
                                                .build());

//Equal width buckets - creating five buckets with the width 0.2 and starting with 0.1
// 0.1, 0.3, 0.5, 0.7, 0.9
Histogram histo = registry.getOrRegister(new HistogramBuilder("name", "help").withEqualWidthBuckets(0.1, 0.2, 5)
                                                                             .build());
```

###Timer - Advanced
The *Timer* supports custom clocks, with the default being the system clock which measures intervals
according to *System.nanoTime()*.

This example shows a timer over a *Histogram* metric, but it's also applicable to the *Summary* metric
```java
//Using an instance of MyClock instead of the default
Histogram timer = registry.getOrRegister(new HistogramBuilder("name", "help")
                                                .withClock(new MyClock())
                                                .build()); 
```