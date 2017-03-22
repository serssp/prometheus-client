package com.outbrain.swinfra.metrics.exporter;

import java.io.IOException;

public interface CollectorExporter {

    void exportTo(Appendable appendable) throws IOException;
}
