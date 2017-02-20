#Prometheus Client
This library is a java client for Prometheus, it implements all 4 Prometheus metrics - Counter, Gauge, Summary and Histogram.

##Background
When we first started using Prometheus we looked for a client to suite us. Unfortunately Prometheus' own
java client is riddled with *synchronized* methods (See the *CKMSQuantiles* class).

In light of that we decided to write our own client. For most metric implementations we simply wrapped
DropWizard metrics. For the Histogram metric we implemented our own as DropWizard has no support for
a bucket-based histogram.

##Getting Started
Add a maven/gradle dependency:

```xml
<dependency>
    <groupId>com.outbrain.swinfra</groupId>
    <artifactId>prometheus-client</artifactId>
    <version>???</version>
</dependency>
```
```
com.outbrain.swinfra:prometheus-client:???
```

##Measuring
Here are some simple examples for the usage of all four types of metrics 

###Counter
```java
//No labels
Counter counter = new CounterBuilder("name", "help").build();
counter.inc();

//With labels
Counter counter = new CounterBuilder("name", "help").withLabels("label1")
                                                    .build();
counter.inc("value1");
```

###Gauge
The gauge takes after the DropWizard's gauge in the sense that it uses a supplier for its value.
This is a gauge that always returns the value "1".

Note that for gauges the label values are determined statically.
```java
//No labels
Gauge gauge = new GaugeBuilder("name", "help").withValueSupplier(() -> 1d).build();

//With labels
Gauge gauge = new GaugeBuilder("name", "help").withLabels("label1")
                                              .withValueSupplier(() -> 1d, "value1")
                                              .build();
```

###Summary
```java
//No labels
Summary summary = new SummaryBuilder(NAME, HELP).build();
summary.observe(10);

//With labels
Summary summary = new SummaryBuilder(NAME, HELP).withLabels("label1")
                                                .build();
summary.observe(10, "value1");
```

###Histogram
```java
//No labels
Histogram histogram = new HistogramBuilder(NAME, HELP).build()
histogram.observe(10);

//With labels
Histogram histogram = new HistogramBuilder(NAME, HELP).withLabels("label1")
                                                      .build()
histogram.observe(10, "value1");
```

###Timers
```java
//A Summary timer
Summary summaryTimer = new SummaryBuilder(NAME, HELP).build();
Timer timer = summaeryTimer.startTimer();
//run some code
timer.stop();

//A Histogram timer
Summary histTimer = new HistogramBuilder(NAME, HELP).build();
Timer timer = histTimer.startTimer();
//run some code
timer.stop();
```

##Reporting to Prometheus
Prometheus uses the *Collector* class to allow a process to expose its metrics. This client implements
a Prometheus *Collector* that serves exactly this purpose. The *MetricCollector* class is instantiated with
a *MetricRegistry*.

```java
//Register all your metrics in a MetricRegistry
MetricRegistry registry = new MetricRegistry();
Counter counter = registry.getOrRegister(new CounterBuilder("name", "help").build());

//Create a MetricCollector
MetricCollector collector = new MetricCollector(registry);

//The collector may be registered in the Prometheus CollectorRegistry
CollectorRegistry.defaultRegistry.register(collector);

//The default registry can then be exposed using Prometheus' MetricsServlet for example
```

##Advanced Usage
###Gauge
multiple value suppliers

###Summary
Different reservoirs

###Histograms
Specify buckets

###Timers
Different clocks