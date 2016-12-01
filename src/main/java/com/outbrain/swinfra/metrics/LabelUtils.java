package com.outbrain.swinfra.metrics;

import org.apache.commons.lang3.Validate;

public class LabelUtils {

  public static String labelsToCommaDelimitedString(final String... labels) {
    return String.join(",", (CharSequence[]) labels);
  }

  public static String[] commaDelimitedStringToLabels(final String commaDelimitedLabels) {
    Validate.notBlank(commaDelimitedLabels, "commaDelimitedLabels must not be empty");
    return commaDelimitedLabels.split(",");
  }

}
