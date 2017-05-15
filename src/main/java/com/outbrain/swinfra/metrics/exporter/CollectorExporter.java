package com.outbrain.swinfra.metrics.exporter;

import java.io.IOException;
import java.io.OutputStream;

public interface CollectorExporter {

    void exportTo(OutputStream stream) throws IOException;
}
